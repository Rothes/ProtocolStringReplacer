package io.github.rothes.protocolstringreplacer.packetlistener.client;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.packetlistener.BasePacketListener;

public abstract class BaseClientPacketListener extends BasePacketListener {

    protected BaseClientPacketListener(PacketType packetType) {
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
