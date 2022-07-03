package me.rothes.protocolstringreplacer.packetlisteners.server.chat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerComponentsPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.api.user.PsrUser;

import java.util.Optional;

public final class Chat extends AbstractServerComponentsPacketListener {

    public Chat() {
        super(PacketType.Play.Server.CHAT, ListenType.CHAT);
    }

    protected void process(PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        Optional<Boolean> isFiltered = packet.getMeta("psr_filtered_packet");
        if (!(isFiltered.isPresent() && isFiltered.get())) {
            PsrUser user = getEventUser(packetEvent);
            if (user == null) {
                return;
            }

            String replaced;

            StructureModifier<WrappedChatComponent> componentModifier = packet.getChatComponents();
            WrappedChatComponent wrappedChatComponent = componentModifier.read(0);
            if (wrappedChatComponent != null) {
                String json = wrappedChatComponent.getJson();
                replaced = getReplacedJson(packetEvent, user, listenType, json, filter);
            } else {
                StructureModifier<Object> modifier = packet.getModifier();
                replaced = processSpigotComponent(modifier, packetEvent, user);
                if (replaced == null) {
                    replaced = processPaperComponent(modifier, packetEvent, user);
                }
            }
            if (replaced != null) {
                componentModifier.write(0, WrappedChatComponent.fromJson(replaced));
            }

        }
    }

}
