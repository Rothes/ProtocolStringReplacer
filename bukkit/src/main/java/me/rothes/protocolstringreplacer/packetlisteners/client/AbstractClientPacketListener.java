package me.rothes.protocolstringreplacer.packetlisteners.client;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.packetlisteners.AbstractPacketListener;

public abstract class AbstractClientPacketListener extends AbstractPacketListener {

    protected AbstractClientPacketListener(PacketType packetType) {
        super(packetType);
        packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ProtocolStringReplacer.getInstance().getConfigManager().listenerPriority, packetType) {
            public void onPacketReceiving(PacketEvent packetEvent) {
                boolean readOnly = packetEvent.isReadOnly();
                if (!canWrite(packetEvent)) {
                    return;
                }
                process(packetEvent);
                if (readOnly) {
                    packetEvent.setReadOnly(true);
                }
            }
        };
    }

}
