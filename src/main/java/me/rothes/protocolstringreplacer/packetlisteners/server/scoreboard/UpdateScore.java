package me.rothes.protocolstringreplacer.packetlisteners.server.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.user.User;

public class UpdateScore extends AbstractScoreBoardListener {

    public UpdateScore() {
        super(PacketType.Play.Server.SCOREBOARD_SCORE, ListenType.SCOREBOARD);
    }

    protected void process(PacketEvent packetEvent) {
        User user = getEventUser(packetEvent);
        PacketContainer packet = packetEvent.getPacket();
        StructureModifier<String> strings = packet.getStrings();
        String entityName = strings.read(0);
        strings.write(0, ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(entityName, user, entityNameFilter));
        // TODO: Edit the score (Integer) maybe? But §klazy to do.
        /*if (packet.getScoreboardActions().read(0) != EnumWrappers.ScoreboardAction.REMOVE) {
            int score = packet.getIntegers().read(0);
        }*/
    }

}
