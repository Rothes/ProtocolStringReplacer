package io.github.rothes.protocolstringreplacer.packetlistener.client;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.packetlistener.AbstractPacketListener;

public abstract class AbstractClientPacketListener extends AbstractPacketListener {

    protected AbstractClientPacketListener(PacketType packetType) {
        super(packetType);
        packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ProtocolStringReplacer.getInstance().getPacketListenerManager().getListenerPriority(), packetType) {
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
