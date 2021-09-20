package me.rothes.protocolstringreplacer.packetlisteners.server.title;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.user.User;

public abstract class AbstractTitleListener extends AbstractServerPacketListener {

    protected AbstractTitleListener(PacketType packetType) {
        super(packetType, ListenType.TITLE);
    }

    protected void process(PacketEvent packetEvent) {
        User user = getEventUser(packetEvent);
        PacketContainer packet = packetEvent.getPacket();
        StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
        WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
        if (wrappedChatComponent != null) {
            String json = wrappedChatComponent.getJson();
            saveCaptureMessage(user, json);
            WrappedChatComponent replaced = getReplacedJsonWrappedComponent(packetEvent, user, json, filter);
            if (replaced != null) {
                wrappedChatComponentStructureModifier.write(0, replaced);
            }
        }
    }

}
