package org.dnttr.zephyr;

import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.network.communication.core.flow.events.ipc.recv.RequestResourcesEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.ipc.send.*;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientIPC {

    private static final BlockingQueue<String> outboundQueue = new LinkedBlockingQueue<>();
    private static final AtomicBoolean running = new AtomicBoolean(false);

    private static PrintWriter writer;
    private static InputStream readerStream;
    private static EventBus eventBus;

    public static Thread inboundThread;
    public static Thread outboundThread;

    private static ServerSocket serverSocket;
    private static Socket clientSocket;

    public static void start(EventBus bus) {
        if (!running.compareAndSet(false, true)) {
            System.err.println("[IPC] Already running, skipping start");
            return;
        }

        eventBus = bus;

        try {
            serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
            System.out.println("IPC_PORT:" + serverSocket.getLocalPort());

            clientSocket = serverSocket.accept();

            readerStream = clientSocket.getInputStream();
            writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), false);

        } catch (IOException e) {
            System.err.println("[IPC-FATAL] Failed to establish IPC socket: " + e.getMessage());

            shutdown();

            return;
        }

        inboundThread = new Thread(ClientIPC::inboundLoop, "Daemon-Inbound-Listener");
        outboundThread = new Thread(ClientIPC::outboundLoop, "Daemon-Outbound-Sender");

        inboundThread.setUncaughtExceptionHandler((t, e) -> {
            System.err.println("[IPC] Uncaught exception in inbound thread:");
            e.printStackTrace(System.err);
        });
        outboundThread.setUncaughtExceptionHandler((t, e) -> {
            System.err.println("[IPC] Uncaught exception in outbound thread:");
            e.printStackTrace(System.err);
        });

        inboundThread.start();
        outboundThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(ClientIPC::shutdown, "IPC-Shutdown-Hook"));

        sendMessage("DAEMON_READY");
    }

    private static void inboundLoop() {
        BufferedReader lineReader = new BufferedReader(new InputStreamReader(readerStream));

        while (running.get()) {
            try {
                String command = lineReader.readLine();

                if (command == null) {
                    System.err.println("[IPC] End of stream detected. C++ side closed connection.");
                    break;
                }

                if (!command.isEmpty()) {
                    handleCppCommand(command);
                }

            } catch (IOException e) {
                if (running.get()) {
                    System.err.println("[IPC] IO exception in inbound loop (connection likely closed): " + e.getMessage());
                }
                break;
            }
        }

        if (running.get()) {
            shutdown();
        }
    }

    private static void outboundLoop() {
        while (running.get()) {
            try {
                String message = outboundQueue.poll(100L, TimeUnit.MILLISECONDS);
                if (message != null) {
                    writer.println(message);
                    writer.flush();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                if (writer.checkError()) {
                    System.err.println("[IPC] Broken pipe detected in outbound loop. Shutting down.");
                    break;
                }
                System.err.println("[IPC] Exception in outbound loop:");
                e.printStackTrace(System.err);
            }
        }
    }


    public static void shutdown() {
        if (running.compareAndSet(true, false)) {
            System.err.println("[IPC] Shutting down ClientIPC...");
            if (inboundThread != null) {
                inboundThread.interrupt();
            }

            if (outboundThread != null) {
                outboundThread.interrupt();
            }

            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }

                if (serverSocket != null) {
                    serverSocket.close();
                }

            } catch (IOException _) {
            }

            System.err.println("[IPC] ClientIPC shutdown complete");
        }
    }

    public static void sendMessage(String message) {
        if (running.get()) {
            boolean added = outboundQueue.offer(message);

            if (!added) {
                System.err.println("[IPC] WARNING: Failed to add message to outbound queue: " + message);
            }
        } else {
            System.err.println("[IPC] WARNING: Attempted to send message while not running: " + message);
        }
    }

    private static void handleCppCommand(String command) {
        if (eventBus == null || command == null || command.isEmpty()) {
            System.err.println("[IPC] Invalid command or eventBus is null");
            return;
        }

        try {
            int colonIndex = command.indexOf(':');

            String cmd = (colonIndex == -1) ? command : command.substring(0, colonIndex);
            String payload = (colonIndex == -1) ? "" : command.substring(colonIndex + 1);

            switch (cmd) {
                case "REQUEST_RESOURCES":
                    eventBus.call(new RequestResourcesEvent());
                    break;
                case "CONNECT_NETTY":
                    String[] addr = payload.split(":", 2);
                    if (addr.length == 2) {
                        eventBus.call(new ConnectCommand(addr[0], Integer.parseInt(addr[1])));
                    }
                    break;
                case "DISCONNECT_NETTY":
                    eventBus.call( new DisconnectCommand());
                    break;
                case "IDENTIFY":
                    eventBus.call(new IdentifyCommand(payload));
                    break;
                case "GET_USERS":
                    eventBus.call(new GetUserListCommand());
                    break;
                case "REQUEST_RELAY":
                    eventBus.call(new RequestRelayCommand(payload));
                    break;
                case "ANSWER_RELAY":
                    eventBus.call(new AnswerRelayCommand("accept".equals(payload)));
                    break;
                case "SEND_CHAT":
                    eventBus.call(new SendChatMessageCommand(payload));
                    break;
                case "SEND_STATUS":
                    eventBus.call(new SendUserStatusCommand(Integer.parseInt(payload)));
                    break;
                case "SEND_DESCRIPTION":
                    eventBus.call(new SendUserDescriptionCommand(payload));
                    break;
                case "SHUTDOWN":
                    shutdown();
                    break;
                default:
                    System.err.println("[IPC] Unknown command: " + cmd);
                    break;
            }
        } catch (Exception e) {
            System.err.println("[IPC] Exception while handling command: " + command);
            e.printStackTrace(System.err);
        }
    }
}