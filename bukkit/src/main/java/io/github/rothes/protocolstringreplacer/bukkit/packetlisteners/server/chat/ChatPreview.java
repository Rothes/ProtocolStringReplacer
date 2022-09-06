package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.chat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.AbstractServerPacketListener;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.ListenType;
import org.jetbrains.annotations.NotNull;

public class ChatPreview extends AbstractServerPacketListener {

    public ChatPreview() {
        super(PacketType.Play.Server.CHAT_PREVIEW, ListenType.CHAT_PREVIEW);
    }

    @Override
    protected void process(@NotNull PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }

        StructureModifier<WrappedChatComponent> chatComponents = packet.getChatComponents();
        WrappedChatComponent read = chatComponents.read(0);
        if (read == null) {
            return;
        }
        WrappedChatComponent replaced = getReplacedJsonWrappedComponent(packetEvent, user, listenType,
                read.getJson(), filter);
        if (replaced != null) {
            chatComponents.write(0, replaced);
        }
    }

}
