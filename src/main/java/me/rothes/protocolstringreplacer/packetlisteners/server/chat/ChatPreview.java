package me.rothes.protocolstringreplacer.packetlisteners.server.chat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;
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
        ProtocolStringReplacer.info(chatComponents.read(0).getJson());
        WrappedChatComponent replaced = getReplacedJsonWrappedComponent(packetEvent, user, listenType,
                chatComponents.read(0).getJson(), filter);
        if (replaced != null) {
            chatComponents.write(0, replaced);
        }
    }

}
