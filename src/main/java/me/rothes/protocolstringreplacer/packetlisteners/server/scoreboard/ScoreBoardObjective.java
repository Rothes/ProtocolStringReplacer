package me.rothes.protocolstringreplacer.packetlisteners.server.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.user.User;
import net.md_5.bungee.chat.ComponentSerializer;

public class ScoreBoardObjective extends AbstractScoreBoardListener {

    public ScoreBoardObjective() {
        super(PacketType.Play.Server.SCOREBOARD_OBJECTIVE, ListenType.SCOREBOARD);
    }

    protected void process(PacketEvent packetEvent) {
        User user = getEventUser(packetEvent);
        PacketContainer packet = packetEvent.getPacket();

        if (packet.getIntegers().read(0) != 1) {
            if (ProtocolStringReplacer.getInstance().getServerMajorVersion() > 12) {
                StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
                WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
                wrappedChatComponent.setJson(ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                        .getReplacedComponents(ComponentSerializer.parse(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedJson(
                                wrappedChatComponent.getJson(), user, titleFilter, false
                        )), user, titleFilter)));
                wrappedChatComponentStructureModifier.write(0, wrappedChatComponent);
            } else {
                StructureModifier<String> strings = packet.getStrings();
                strings.write(0, ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(strings.read(0), user, titleFilter));
            }
        }
    }

}
