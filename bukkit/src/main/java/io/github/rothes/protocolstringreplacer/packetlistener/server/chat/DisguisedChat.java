package io.github.rothes.protocolstringreplacer.packetlistener.server.chat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.server.BaseServerComponentsPacketListener;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;
import org.jetbrains.annotations.NotNull;

public class DisguisedChat extends BaseServerComponentsPacketListener {

    public DisguisedChat() {
        super(PacketType.Play.Server.DISGUISED_CHAT, ListenType.CHAT);
    }

    @Override
    protected void process(@NotNull PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }

        StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packetEvent.getPacket().getChatComponents();
        WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
        String replaced = getReplacedJson(packetEvent, user, listenType, wrappedChatComponent.getJson(), filter);

        if (replaced != null) {
            wrappedChatComponentStructureModifier.write(0, WrappedChatComponent.fromJson(replaced));
        }

    }

}
