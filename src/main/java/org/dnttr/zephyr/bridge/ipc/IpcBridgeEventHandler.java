package org.dnttr.zephyr.bridge.ipc;

import org.dnttr.zephyr.ClientIPC;
import org.dnttr.zephyr.event.EventBus;
import org.dnttr.zephyr.event.EventSubscriber;
import org.dnttr.zephyr.network.communication.core.flow.events.ipc.recv.*;

public class IpcBridgeEventHandler {

    public IpcBridgeEventHandler(final EventBus eventBus) {
    }

    @EventSubscriber
    public void onUserList(IncomingUserListEvent event) {
        ClientIPC.sendMessage("PUSH_USER_LIST:" + event.getPayload());
    }

    @EventSubscriber
    public void onReadyForIdentification(ReadyForIdentificationEvent event) {
        ClientIPC.sendMessage("READY_FOR_IDENTIFY");
    }

    @EventSubscriber
    public void onIdentificationSuccess(IdentificationSuccessEvent event) {
        ClientIPC.sendMessage("IDENTIFY_SUCCESS");
    }

    @EventSubscriber
    public void onIdentificationFailure(IdentificationFailureEvent event) {
        ClientIPC.sendMessage("IDENTIFY_FAIL:" + event.getReason());
    }

    @EventSubscriber
    public void onIncomingRelayRequest(IncomingRelayRequestEvent event) {
        ClientIPC.sendMessage("INCOMING_RELAY_REQUEST:" + event.getSenderName());
    }

    @EventSubscriber
    public void onRelayEstablished(RelayEstablishedEvent event) {
        ClientIPC.sendMessage("RELAY_ESTABLISHED");
    }

    @EventSubscriber
    public void onRelayRefused(RelayRefusedEvent event) {
        ClientIPC.sendMessage("RELAY_REFUSED");
    }

    @EventSubscriber
    public void onRelayTerminated(RelayTerminatedEvent event) {
        ClientIPC.sendMessage("RELAY_TERMINATED:" + event.getReason());
    }

    @EventSubscriber
    public void onChatMessage(IncomingChatMessageEvent event) {
        ClientIPC.sendMessage("INCOMING_CHAT:" + event.getMessage());
    }

    @EventSubscriber
    public void onUserStatus(IncomingUserStatusEvent event) {
        ClientIPC.sendMessage("INCOMING_STATUS:" + event.getStatus());
    }

    @EventSubscriber
    public void onUserDescription(IncomingUserDescriptionEvent event) {
        ClientIPC.sendMessage("INCOMING_DESCRIPTION:" + event.getDescription());
    }
}