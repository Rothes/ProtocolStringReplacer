package me.rothes.protocolstringreplacer.packetlisteners.server.title;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerComponentsPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.api.user.PsrUser;

public abstract class AbstractTitleListener extends AbstractServerComponentsPacketListener {

    protected AbstractTitleListener(PacketType packetType) {
        this(packetType, ListenType.TITLE);
    }

    protected AbstractTitleListener(PacketType packetType, ListenType listenType) {
        super(packetType, listenType);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();

        String replaced;
        StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
        WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
        if (wrappedChatComponent != null) {
            String json = wrappedChatComponent.getJson();
            replaced = getReplacedJson(packetEvent, user, listenType, json, filter);
        } else {
            replaced = processSpigotComponent(packet.getModifier(), packetEvent, user);
            if (replaced == null) {
                replaced = processPaperComponent(packet.getModifier(), packetEvent, user);
            }
        }

        if (replaced != null) {
            wrappedChatComponentStructureModifier.write(0, WrappedChatComponent.fromJson(replaced));
        }
    }

}
