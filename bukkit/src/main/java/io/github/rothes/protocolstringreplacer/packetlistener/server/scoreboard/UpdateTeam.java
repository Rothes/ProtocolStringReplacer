package io.github.rothes.protocolstringreplacer.packetlistener.server.scoreboard;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.server.AbstractServerPacketListener;
import org.jetbrains.annotations.NotNull;

public final class UpdateTeam extends BaseUpdateTeamListener {

    @Override
    protected void process(@NotNull PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();
        StructureModifier<String> strings = packet.getStrings();
        String read = strings.read(1);
        if (read == null) {
            return;
        }
        String replacedText = AbstractServerPacketListener.getReplacedText(packetEvent, user, listenType, read, teamDNameFilter);
        if (replacedText == null) {
            return;
        }
        strings.write(1, replacedText);

        replacedText = AbstractServerPacketListener.getReplacedText(packetEvent, user, listenType, strings.read(2), teamPrefixFilter);
        if (replacedText == null) {
            return;
        }
        strings.write(2, replacedText);

        replacedText = AbstractServerPacketListener.getReplacedText(packetEvent, user, listenType, strings.read(3), teamSuffixFilter);
        if (replacedText == null) {
            return;
        }
        strings.write(3, replacedText);

    }

}
