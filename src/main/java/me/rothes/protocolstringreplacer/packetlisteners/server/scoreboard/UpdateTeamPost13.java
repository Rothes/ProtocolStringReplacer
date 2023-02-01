package me.rothes.protocolstringreplacer.packetlisteners.server.scoreboard;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import org.jetbrains.annotations.NotNull;

public final class UpdateTeamPost13 extends BaseUpdateTeamListener {

    @Override
    protected void process(@NotNull PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();
        StructureModifier<WrappedChatComponent> chatComponents = packet.getChatComponents();
        WrappedChatComponent wrappedChatComponent = chatComponents.read(0);
        if (wrappedChatComponent == null) {
            return;
        }
        String json = wrappedChatComponent.getJson();
        WrappedChatComponent replaced = getReplacedJsonWrappedComponent(packetEvent, user, listenType, json, teamDNameFilter);
        if (replaced == null) {
            return;
        }
        chatComponents.write(0, replaced);

        wrappedChatComponent = chatComponents.read(1);
        json = wrappedChatComponent.getJson();
        replaced = getReplacedJsonWrappedComponent(packetEvent, user, listenType, json, teamPrefixFilter);
        if (replaced == null) {
            return;
        }
        chatComponents.write(1, replaced);

        wrappedChatComponent = chatComponents.read(2);
        json = wrappedChatComponent.getJson();
        replaced = getReplacedJsonWrappedComponent(packetEvent, user, listenType, json, teamSuffixFilter);
        if (replaced == null) {
            return;
        }
        chatComponents.write(2, replaced);
    }

}
