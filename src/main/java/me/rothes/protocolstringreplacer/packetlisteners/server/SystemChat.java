package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SystemChat extends AbstractServerPacketListener {

    public SystemChat() {
        super(PacketType.Play.Server.SYSTEM_CHAT, ListenType.CHAT);
    }

    @Override
    protected void process(@NotNull PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        Optional<Boolean> isFiltered = packet.getMeta("psr_filtered_packet");
        if (!(isFiltered.isPresent() && isFiltered.get())) {
            PsrUser user = getEventUser(packetEvent);
            if (user == null) {
                return;
            }

            String replaced = getReplacedJson(packetEvent, user, listenType, packet.getStrings().read(0), filter);
            if (replaced != null) {
                packet.getStrings().write(0, replaced);
            }
        }
    }

}
