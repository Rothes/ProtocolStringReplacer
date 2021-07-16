package me.rothes.protocolstringreplacer.packetlisteners.client;

import com.comphenix.protocol.PacketType;
import me.rothes.protocolstringreplacer.packetlisteners.AbstractPacketListener;

public abstract class AbstractClientPacketListener extends AbstractPacketListener {

    protected AbstractClientPacketListener(PacketType packetType) {
        super(packetType);
    }

}
