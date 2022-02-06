package me.rothes.protocolstringreplacer.packetlisteners.server.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ListenType;

public class ScoreBoardObjective extends AbstractScoreBoardListener {

    public ScoreBoardObjective() {
        super(PacketType.Play.Server.SCOREBOARD_OBJECTIVE, ListenType.SCOREBOARD);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();

        if (packet.getIntegers().read(0) != 1) {
            if (ProtocolStringReplacer.getInstance().getServerMajorVersion() > 12) {
                StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
                WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
                String replaced = getReplacedJson(packetEvent, user, listenType, wrappedChatComponent.getJson(), titleFilter);
                if (replaced != null) {
                    wrappedChatComponentStructureModifier.write(0, wrappedChatComponent);
                }
            } else {
                StructureModifier<String> strings = packet.getStrings();
                String replaced = getReplacedText(packetEvent, user, listenType, strings.read(0), titleFilter);
                if (replaced != null)
                strings.write(0, replaced);
            }
        }
    }

}
