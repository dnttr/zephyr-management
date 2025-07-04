package org.dnttr.zephyr.bridge.ipc;

import org.dnttr.zephyr.management.managers.FileManager;
import org.dnttr.zephyr.network.loader.api.client.Client;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class InterProcessClient {

    private static final BlockingQueue<String> outboundQueue = new LinkedBlockingQueue<>();

    private static volatile boolean running = true;
    private static Client activeClient = null;

    private static DataOutputStream dataOut;
    private static DataInputStream dataIn;

    private static final int MAX = 10; //in mb

    public static void sendMessage(String message) {
        if (dataOut != null) {
            try {
                outboundQueue.put(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[FATAL] Interrupted while queuing message: " + e.getMessage());
            }
        }
    }

    public static void pushShader(String name, String source) {
        sendMessage("PUSH_SHADER:" + name + ":" + source);
    }

    public static void finishLoading() {
        sendMessage("FINISH_LOADING");
    }

    public static void pushTexture(String name, byte[] buffer, int width, int height) {
        String encodedBuffer = Base64.getEncoder().encodeToString(buffer);
        sendMessage("PUSH_TEXTURE:" + name + ":" + encodedBuffer + ":" + width + ":" + height);
    }

    public static void pushFont(String name, byte[] bytes) {
        String encodedBytes = Base64.getEncoder().encodeToString(bytes);
        sendMessage("PUSH_FONT:" + name + ":" + encodedBytes);
    }

    public static void main(String[] args) {
        try {
            dataIn = new DataInputStream(System.in);
            dataOut = new DataOutputStream(System.out);
        } catch (Exception e) {
            System.err.println("[FATAL] Failed to initialize DataInput/OutputStream: " + e.getMessage());

            System.exit(1);
        }

        Thread outboundSenderThread = getOutboundSenderThread();
        Thread inboundListenerThread = getInboundListenerThread();

        outboundSenderThread.start();
        inboundListenerThread.start();
    }

    private static @NotNull Thread getInboundListenerThread() {
        Thread thread = new Thread(() -> {
            try {
                sendMessage("DAEMON_READY");

                try {
                    new FileManager().push();
                } catch (Exception e) {
                    System.err.println("[FATAL] Failed during initial resource push: " + e.getMessage());
                    sendMessage("RESOURCE_PUSH_FAIL:" + e.getMessage());
                }

                while (running) {
                    int length = dataIn.readInt();

                    if (length <= 0) {
                        running = false;
                        break;
                    }
                    if (length > MAX * 1024 * 1024) {
                        System.err.println("[FATAL] Received excessively large message length: " + length);
                        running = false;
                        break;
                    }

                    byte[] buffer = new byte[length];

                    dataIn.readFully(buffer);
                    String command = new String(buffer, StandardCharsets.UTF_8).trim();

                    if ("SHUTDOWN".equals(command)) {
                        running = false;

                        if (activeClient != null) {
                            activeClient.destroy();
                        }

                        sendMessage("DAEMON_SHUTDOWN_ACK");

                        try {
                            Thread.sleep(50);
                            System.exit(0);
                        } catch (InterruptedException _) {
                        }

                    } else if (command.startsWith("CONNECT_NETTY:")) {
                        handleNettyConnection(command);
                    }
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("[FATAL] IPC input stream error: " + e.getMessage());
                }
            } finally {
                running = false;
            }
        }, "Daemon-Inbound-Listener");

        thread.setDaemon(true);
        
        return thread;
    }

    private static @NotNull Thread getOutboundSenderThread() {
        Thread thread = new Thread(() -> {
            while (running) {
                try {
                    String message = outboundQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (message != null) {
                        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
                        lengthBuffer.order(ByteOrder.BIG_ENDIAN);
                        
                        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                      
                        lengthBuffer.putInt(messageBytes.length);
                      
                        dataOut.write(lengthBuffer.array());
                        dataOut.write(messageBytes);
                        
                        dataOut.flush();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                 
                    running = false;
                } catch (IOException e) {
                    System.err.println("[FATAL] IO Exception in outbound sender: " + e.getMessage());
                  
                    running = false;
                }
            }
        }, "Daemon-Outbound-Sender");
        
        thread.setDaemon(true);
        
        return thread;
    }

    private static void handleNettyConnection(String command) {
        String[] parts = command.substring("CONNECT_NETTY:".length()).split(":");

        if (parts.length == 2) {
            try {
                final String ip = parts[0];
                final int port = Integer.parseInt(parts[1]);

                InetSocketAddress address = new InetSocketAddress(ip, port);

                if (activeClient != null) {
                    activeClient.destroy();
                }

                activeClient = new Client(address);
                sendMessage("CONNECT_SUCCESS");
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to connect Netty client: " + e.getMessage());

                sendMessage("CONNECT_FAIL:" + e.getMessage());
            }
        } else {
            sendMessage("CONNECT_FAIL:Malformed command");
        }
    }
}