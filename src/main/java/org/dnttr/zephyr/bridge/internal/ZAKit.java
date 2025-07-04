package org.dnttr.zephyr.bridge.internal;

import org.dnttr.zephyr.network.loader.api.client.Client;
import org.jetbrains.annotations.ApiStatus;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dnttr
 * Temporary, subject to chage
 */

@ApiStatus.Experimental
public final class ZAKit {

    private static final List<String> libraries = new ArrayList<>();
    private static boolean shouldLoad = true;

    private static Client activeClient = null;
    private static Thread clientOperationThread = null;

    public static native int ffi_zm_push_shader(String name, String buffer);

    public static native int ffi_zm_push_texture(String name, ByteBuffer buffer, int width, int height);

    public static native int ffi_zm_push_font(String name, byte[] buffer);

    public static native int ffi_zm_finish_loading();

    public static void loadNative(String path) {
        libraries.add(path);
    }

    private static volatile boolean shutdownHookRegistered = false;

    public static void connect() {
        if (clientOperationThread != null && clientOperationThread.isAlive()) {
            System.out.println("[ZAKit] Client thread already running.");
            return;
        }

        if (!shutdownHookRegistered) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
                ThreadInfo[] threadInfos = threadBean.getThreadInfo(threadBean.getAllThreadIds());

                System.out.println("=== ALL THREADS ===");
                for (ThreadInfo info : threadInfos) {
                    if (info != null) {
                        Thread.State state = info.getThreadState();
                        System.out.println("Thread: " + info.getThreadName() +
                                " | State: " + state +
                                " | Daemon: " + threadBean.getThreadInfo(info.getThreadId()).getThreadName());
                    }
                }
            }, "ZKSH"));
            shutdownHookRegistered = true;
        }


        clientOperationThread = new Thread(() -> {
            try {
                if (libraries.isEmpty()) {
                    throw new IllegalStateException("No native libraries loaded");
                } else if (shouldLoad) {
                    for (String library : libraries) {
                        System.load(library);
                    }
                    shouldLoad = false;
                }

                InetSocketAddress address = new InetSocketAddress("127.0.0.1", 12345);
                System.out.println(address.getHostName() + ":" + address.getPort());

                activeClient = new Client(address);
            } catch (Exception e) {
                System.err.println("[ZAKit Thread ERROR] An exception occurred in the client operation thread: " + e.getMessage());
                e.printStackTrace();
            } finally {
                System.out.println("[ZAKit Thread] Client operation thread attempting to exit.");
            }
        });

        clientOperationThread.setDaemon(true);
        clientOperationThread.start();
    }
}
