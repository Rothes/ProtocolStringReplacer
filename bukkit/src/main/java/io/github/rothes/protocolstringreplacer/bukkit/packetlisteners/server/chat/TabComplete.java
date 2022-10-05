package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.chat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import io.github.rothes.protocolstringreplacer.bukkit.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.AbstractServerPacketListener;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.ListenType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TabComplete extends AbstractServerPacketListener {

    public TabComplete() {
        super(PacketType.Play.Server.TAB_COMPLETE, ListenType.TAB_COMPLETE);
    }

    @Override
    protected void process(@NotNull PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 13) {
            // 1.13+
            StructureModifier<Suggestions> modifier = packet.getModifier().withType(Suggestions.class);
            Suggestions suggestions = modifier.read(0);

            List<Suggestion> list = suggestions.getList();
            Suggestion suggestion;
            for (int i = 0; i < list.size(); i++) {
                suggestion = list.get(i);
                list.set(i, new Suggestion(suggestion.getRange(),
                        getReplacedText(packetEvent, user, listenType, suggestion.getText(), filter),
                        suggestion.getTooltip()));
            }
        } else {
            // 1.8 - 1.12
            StructureModifier<String[]> stringArrays = packet.getStringArrays();
            if (stringArrays.size() != 0) {
                String[] read = stringArrays.read(0);
                for (int i = 0; i < read.length; i++) {
                    read[i] = getReplacedText(packetEvent, user, listenType, read[i], filter);
                }
            }
        }

    }

}
