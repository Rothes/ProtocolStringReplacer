package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.client;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import io.github.rothes.protocolstringreplacer.bukkit.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.AbstractPacketListener;

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
