package org.dnttr.zephyr;

import org.dnttr.zephyr.bridge.ipc.ClientConnectionManager;
import org.dnttr.zephyr.bridge.ipc.IpcBridgeEventHandler;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.ipc.ClientIPC;
import org.dnttr.zephyr.management.managers.FileManager;
import org.dnttr.zephyr.network.communication.api.client.ClientPacketSender;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class ApplicationLoader {

    public static void main(String[] args) throws IOException {
        FileManager fileManager = new FileManager();
        EventBus eventBus = new EventBus();
        ClientConnectionManager connectionManager = new ClientConnectionManager(eventBus);
        IpcBridgeEventHandler ipcBridgeHandler = new IpcBridgeEventHandler(eventBus);

        ClientPacketSender packetSender = new ClientPacketSender(connectionManager, eventBus);

        eventBus.register(connectionManager);
        eventBus.register(ipcBridgeHandler);
        eventBus.register(fileManager);

        ClientIPC.start(eventBus);

        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.err.println("[JVM] ApplicationLoader main thread exiting.");
    }
}