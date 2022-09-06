package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.chat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.AbstractServerComponentsPacketListener;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.ListenType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SystemChat extends AbstractServerComponentsPacketListener {

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

            String replaced;

            StructureModifier<String> stringModifier = packet.getStrings();
            String read = stringModifier.read(0);
            if (read != null) {
                replaced = getReplacedJson(packetEvent, user, listenType, read, filter);
            } else {
                replaced = processPaperComponent(packet.getModifier(), packetEvent, user);
            }

            if (replaced != null) {
                stringModifier.write(0, replaced);
            }
        }
    }

}
