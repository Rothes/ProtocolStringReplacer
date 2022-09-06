package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.bukkit.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.ListenType;

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
