package io.github.rothes.protocolstringreplacer.packetlistener.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;
import org.jetbrains.annotations.NotNull;

public class KickDisconnect extends AbstractServerPacketListener {

    public KickDisconnect() {
        super(PacketType.Play.Server.KICK_DISCONNECT, ListenType.KICK_DISCONNECT);
    }

    @Override
    protected void process(@NotNull PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();
        StructureModifier<WrappedChatComponent> chatComponents = packet.getChatComponents();
        String json = chatComponents.read(0).getJson();
        WrappedChatComponent replaced = getReplacedJsonWrappedComponent(packetEvent, user, listenType, json, filter);
        if (replaced != null) {
            chatComponents.write(0, replaced);
        }

    }

}
