package org.dnttr.zephyr;

import org.dnttr.zephyr.bridge.ipc.ClientConnectionManager;
import org.dnttr.zephyr.bridge.ipc.IpcBridgeEventHandler;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.management.managers.FileManager;

import java.io.IOException;

public class Application {

    public static void start() throws IOException {
        FileManager fileManager = new FileManager();
        EventBus eventBus = new EventBus();
        ClientConnectionManager connectionManager = new ClientConnectionManager(eventBus);
        IpcBridgeEventHandler ipcBridgeHandler = new IpcBridgeEventHandler(eventBus);

        eventBus.register(connectionManager);
        eventBus.register(ipcBridgeHandler);
        eventBus.register(fileManager);

        ClientIPC.start(eventBus);

        try {
            if (ClientIPC.inboundThread != null) {
                ClientIPC.inboundThread.join();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}