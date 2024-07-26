package io.github.rothes.protocolstringreplacer.packetlistener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public abstract class BasePacketListener {

    protected final PacketType packetType;
    protected PacketAdapter packetAdapter;

    protected BasePacketListener(@Nonnull PacketType packetType) {
        this.packetType = packetType;
    }

    protected void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter);
    }

    @Nullable
    protected final PsrUser getEventUser(@Nonnull PacketEvent packetEvent) {
        Validate.notNull(packetEvent, "Packet Event cannot be null");
        Player player = packetEvent.getPlayer();
        if (packetEvent.isPlayerTemporary()) {
            ProtocolStringReplacer.warn("ProtocolLib returns temporary player [" + player.getAddress() + "] for packet " + packetType.name() + ". "
                    + "It cannot be processed.");
            return null;
        } else {
            return ProtocolStringReplacer.getInstance().getUserManager().getUser(player);
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
