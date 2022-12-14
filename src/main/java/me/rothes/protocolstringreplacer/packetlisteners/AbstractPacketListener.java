package me.rothes.protocolstringreplacer.packetlisteners;

import com.comphenix.protocol.PacketType;
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
    public PacketAdapter packetAdapter;

    protected AbstractPacketListener(@Nonnull PacketType packetType) {
        this.packetType = packetType;
    }

    @Nullable
    protected final PsrUser getEventUser(@Nonnull PacketEvent packetEvent) {
        Validate.notNull(packetEvent, "Packet Event cannot be null");
        Player player = packetEvent.getPlayer();
        //noinspection DataFlowIssue // For ProtocolLib v4 compatibility
        return player instanceof Player? ProtocolStringReplacer.getInstance().getUserManager().getUser(player): null;
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
