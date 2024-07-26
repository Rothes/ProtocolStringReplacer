package io.github.rothes.protocolstringreplacer.packetlistener.server.combat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.server.BaseServerPacketListener;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;
import org.jetbrains.annotations.NotNull;

public class CombatEvent extends BaseServerPacketListener {

    public CombatEvent() {
        super(PacketType.Play.Server.COMBAT_EVENT, ListenType.COMBAT_KILL);
    }

    @Override
    protected void process(@NotNull PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();
        StructureModifier<WrappedChatComponent> chatComponents = packet.getChatComponents();
        if (packet.getCombatEvents().read(0) != EnumWrappers.CombatEventType.ENTITY_DIED) {
            return;
        }

        if (chatComponents.size() != 1) {
            // On 1.8 this is a String.
            StructureModifier<String> strings = packet.getStrings();
            strings.write(0,
                    getReplacedText(packetEvent, user, listenType, strings.read(0), filter));
            return;
        }
        String json = chatComponents.read(0).getJson();
        WrappedChatComponent replaced = getReplacedJsonWrappedComponent(packetEvent, user, listenType, json, filter);
        if (replaced != null) {
            chatComponents.write(0, replaced);
        }

    }

}
