package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ListenType;

public final class ActionBar extends AbstractServerComponentsPacketListener {

    public ActionBar() {
        super(PacketType.Play.Server.SET_ACTION_BAR_TEXT, ListenType.CHAT);
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
