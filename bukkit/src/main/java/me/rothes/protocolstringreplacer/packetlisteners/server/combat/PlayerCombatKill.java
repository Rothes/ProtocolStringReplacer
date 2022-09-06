package me.rothes.protocolstringreplacer.packetlisteners.server.combat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import org.jetbrains.annotations.NotNull;

public class PlayerCombatKill extends AbstractServerPacketListener {

    public PlayerCombatKill() {
        super(PacketType.Play.Server.PLAYER_COMBAT_KILL, ListenType.COMBAT_KILL);
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
