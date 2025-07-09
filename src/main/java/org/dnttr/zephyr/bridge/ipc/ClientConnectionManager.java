package org.dnttr.zephyr.bridge.ipc;

import org.dnttr.zephyr.ClientIPC;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.communication.core.Consumer;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.channel.ConnectionFatalEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.internal.management.ManagerTerminationEvent;
import org.dnttr.zephyr.network.communication.core.flow.events.ipc.send.ConnectCommand;
import org.dnttr.zephyr.network.communication.core.flow.events.ipc.send.DisconnectCommand;
import org.dnttr.zephyr.network.communication.core.managers.ObserverManager;
import org.dnttr.zephyr.network.loader.api.client.Client;

import java.util.function.Supplier;

public class ClientConnectionManager implements Supplier<Consumer> {

    private Client activeClient;
    private final EventBus eventBus;

    public ClientConnectionManager(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Listens for the command to connect to the server, which is
     * triggered by the C++ application.
     * @param command The connection command event.
     */
    @EventSubscriber
    public void onConnect(ConnectCommand command) {
        if (activeClient != null) {
            activeClient.destroy();
            activeClient = null;
        }
        try {

            new Thread(() -> {
                activeClient = new Client(command.getAddress(), eventBus, new ObserverManager());
            }, "Netty-Client-Starter").start();

        } catch (Exception e) {
            System.err.println("Failed to initiate connection: " + e.getMessage());
        }
    }

    @EventSubscriber
    public void onTerminationEvent(final ManagerTerminationEvent event) {
        ClientIPC.sendMessage("DISCONNECTED");
        activeClient = null;
    }

    @EventSubscriber
    public void onFatalEvent(final ConnectionFatalEvent event) {
        ClientIPC.sendMessage("CONNECTION_FAILED:" + event.getReason());
        if (activeClient != null) {
            activeClient.destroy();
            activeClient = null;
        }
    }

    @EventSubscriber
    public void onDisconnectCommand(final DisconnectCommand command) {
        if (activeClient != null) {
            this.activeClient.destroy();
            this.activeClient = null;
        }
    }

    /**
     * Provides the currently active network session (Consumer).
     * This is used by the ClientPacketSender to know where to send packets.
     * @return The active Consumer, or null if not connected.
     */
    @Override
    public Consumer get() {
        if (activeClient != null && activeClient.getContext() != null) {
            return activeClient.getContext().getConsumer();
        }

        return null;
    }
}