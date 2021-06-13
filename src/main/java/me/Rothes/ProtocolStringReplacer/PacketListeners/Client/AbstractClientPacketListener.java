package me.Rothes.ProtocolStringReplacer.PacketListeners.Client;

import com.comphenix.protocol.PacketType;
import me.Rothes.ProtocolStringReplacer.PacketListeners.AbstractPacketListener;

public abstract class AbstractClientPacketListener extends AbstractPacketListener {

    protected AbstractClientPacketListener(PacketType packetType) {
        super(packetType);
    }

}
