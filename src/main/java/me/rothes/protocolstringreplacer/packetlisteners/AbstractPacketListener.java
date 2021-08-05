package me.rothes.protocolstringreplacer.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.server.TemporaryPlayer;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.user.User;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public abstract class AbstractPacketListener {

    protected final PacketType packetType;
    public PacketAdapter packetAdapter;

    protected AbstractPacketListener(@Nonnull PacketType packetType) {
        this.packetType = packetType;
    }

    protected final User getEventUser(@Nonnull PacketEvent packetEvent) {
        Validate.notNull(packetEvent, "Packet Event cannot be null");
        Player player = packetEvent.getPlayer();
        return player instanceof TemporaryPlayer? ProtocolStringReplacer.getInstance().getUserManager().getUser(player.getPlayer()) : ProtocolStringReplacer.getInstance().getUserManager().getUser(player);
    }

    protected boolean canWrite(@Nonnull PacketEvent packetEvent) {
        Validate.notNull(packetEvent, "Packet Event cannot be null");

        if (packetEvent.isReadOnly()) {
            if (ProtocolStringReplacer.getInstance().getConfigManager().forceReplace) {
                packetEvent.setReadOnly(false);
            } else {
                return false;
            }
        }
        return true;
    }

    abstract protected void process(@Nonnull PacketEvent packetEvent);

}
