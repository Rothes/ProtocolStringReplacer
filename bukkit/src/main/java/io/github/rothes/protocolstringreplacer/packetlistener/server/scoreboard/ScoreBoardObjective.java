package io.github.rothes.protocolstringreplacer.packetlistener.server.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.server.BaseServerPacketListener;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;
import org.jetbrains.annotations.NotNull;

public class ScoreBoardObjective extends BaseScoreBoardListener {

    public ScoreBoardObjective() {
        super(PacketType.Play.Server.SCOREBOARD_OBJECTIVE, ListenType.SCOREBOARD);
    }

    protected void process(@NotNull PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();

        if (packet.getIntegers().read(0) != 1) {
            if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 13) {
                StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
                WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
                String replaced = BaseServerPacketListener.getReplacedJson(packetEvent, user, listenType, wrappedChatComponent.getJson(), titleFilter);
                if (replaced != null) {
                    wrappedChatComponentStructureModifier.write(0, WrappedChatComponent.fromJson(replaced));
                }
            } else {
                StructureModifier<String> strings = packet.getStrings();
                String replaced = BaseServerPacketListener.getReplacedText(packetEvent, user, listenType, strings.read(1), titleFilter);
                if (replaced != null)
                    strings.write(1, replaced);
            }
        }
    }

}
