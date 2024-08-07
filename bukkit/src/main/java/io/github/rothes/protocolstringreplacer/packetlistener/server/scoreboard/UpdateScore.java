package io.github.rothes.protocolstringreplacer.packetlistener.server.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.server.BaseServerPacketListener;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;

public class UpdateScore extends BaseScoreBoardListener {

    public UpdateScore() {
        super(PacketType.Play.Server.SCOREBOARD_SCORE, ListenType.SCOREBOARD);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();
        StructureModifier<String> strings = packet.getStrings();

        String replaced = BaseServerPacketListener.getReplacedText(packetEvent, user, listenType, strings.read(0), entityNameFilter);
        if (replaced != null) {
            strings.write(0, replaced);
        }
        // TODO: Edit the score (Integer) maybe? But §klazy to do.
        /*if (packet.getScoreboardActions().read(0) != EnumWrappers.ScoreboardAction.REMOVE) {
            int score = packet.getIntegers().read(0);
        }*/
    }

}
