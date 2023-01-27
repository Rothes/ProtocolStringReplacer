package me.rothes.protocolstringreplacer.packetlisteners.server.scoreboard;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
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
        String replacedText = getReplacedText(packetEvent, user, listenType, strings.read(1), teamDNameFilter);
        if (replacedText == null) {
            return;
        }
        strings.write(1, replacedText);

        replacedText = getReplacedText(packetEvent, user, listenType, strings.read(2), teamPrefixFilter);
        if (replacedText == null) {
            return;
        }
        strings.write(2, replacedText);

        replacedText = getReplacedText(packetEvent, user, listenType, strings.read(3), teamSuffixFilter);
        if (replacedText == null) {
            return;
        }
        strings.write(3, replacedText);

    }

}
