package org.dnttr.zephyr.bridge.ipc;

import io.netty.channel.Channel;
import io.netty.channel.MultithreadEventLoopGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author dnttr
 */

public class InterProcessClient {

    private Channel channel;
    private MultithreadEventLoopGroup child;
    private BlockingQueue<String> messages = new LinkedBlockingQueue<>();
    private PrintWriter out;
    private BufferedReader in;

    private boolean running = true;

    private InterProcessClient() {
        setup();
        start();
    }

    public static void main(String[] args) {
        new InterProcessClient();
    }

    private void start() {
        Thread ipcListenerThread = new Thread(() -> {
            String line;
            try {
                out.println("[JavaDaemon] Daemon ready. Waiting for commands.");
                while (running && (line = in.readLine()) != null) {
                    System.out.println("[JavaDaemon] Received command from C++: '" + line.trim() + "'");
                    if ("SHUTDOWN".equals(line.trim())) {
                        System.out.println("[JavaDaemon] SHUTDOWN command received. Initiating graceful exit...");
                        running = false;
                        System.exit(0);
                    }
                }
            } catch (IOException e) {
                System.err.println("[JavaDaemon ERROR] IPC communication channel closed or error: " + e.getMessage());
            } finally {
                try { in.close(); } catch (IOException _) {}
                out.close();
                System.out.println("[JavaDaemon] IPC Listener thread finished.");
            }
        }, "Daemon-IPC-Listener");
        ipcListenerThread.setDaemon(true);
        ipcListenerThread.start();
    }

    private void setup() {
        out = new PrintWriter(System.out, true);
        in = new BufferedReader(new InputStreamReader(System.in));
    }
}
