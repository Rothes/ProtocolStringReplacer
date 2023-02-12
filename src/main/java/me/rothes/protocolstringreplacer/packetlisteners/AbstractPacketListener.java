package me.rothes.protocolstringreplacer.packetlisteners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public abstract class AbstractPacketListener {

    protected final PacketType packetType;
    protected PacketAdapter packetAdapter;

    protected AbstractPacketListener(@Nonnull PacketType packetType) {
        this.packetType = packetType;
    }

    protected void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter);
    }

    @Nullable
    protected final PsrUser getEventUser(@Nonnull PacketEvent packetEvent) {
        Validate.notNull(packetEvent, "Packet Event cannot be null");
        Player player = packetEvent.getPlayer();
        try {
            return ProtocolStringReplacer.getInstance().getUserManager().getUser(player);
        } catch (UnsupportedOperationException e) {
            // TemporaryPlayer throws
            ProtocolStringReplacer.warn("Unable to get the player [" + player.getAddress() + "] from packet. Must be an issue from ProtocolLib: " + e);
            return null;
        }
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
