package me.rothes.protocolstringreplacer.packetlisteners.server.chat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerComponentsPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;
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

            StructureModifier<Boolean> booleans = packet.getBooleans();
            if (booleans.size() == 1) {
                if (booleans.read(0)) {
                    return;
                }
            } else if (packet.getIntegers().read(0) == EnumWrappers.ChatType.GAME_INFO.getId()) {
                return;
            }

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
