package me.rothes.protocolstringreplacer.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.server.TemporaryPlayer;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.user.User;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public abstract class AbstractPacketListener {

    protected final PacketType packetType;

    protected AbstractPacketListener(PacketType packetType) {
        this.packetType = packetType;
    }

    protected final User getEventUser(@Nonnull PacketEvent event) {
        Validate.notNull(event, "Packet Event cannot be null");
        Player player = event.getPlayer();
        return player instanceof TemporaryPlayer? ProtocolStringReplacer.getInstance().getUserManager().getUser(player.getPlayer()) : ProtocolStringReplacer.getInstance().getUserManager().getUser(player);
    }

}
