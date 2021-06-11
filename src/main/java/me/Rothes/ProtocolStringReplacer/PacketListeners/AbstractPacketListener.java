package me.Rothes.ProtocolStringReplacer.PacketListeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import me.Rothes.ProtocolStringReplacer.User.User;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import org.apache.commons.lang.Validate;

import javax.annotation.Nonnull;

public abstract class AbstractPacketListener {

    protected final PacketType packetType;

    protected AbstractPacketListener(PacketType packetType) {
        this.packetType = packetType;
    }

    protected final User getEventUser(@Nonnull PacketEvent event) {
        Validate.notNull(event, "Packet Event cannot be null");
        return ProtocolStringReplacer.getUserManager().getUser(event.getPlayer());
    }

}
