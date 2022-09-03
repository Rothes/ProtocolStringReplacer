package me.rothes.protocolstringreplacer.packetlisteners.server.chat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class TabComplete extends AbstractServerPacketListener {

    private int suggestionsField = -1;

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

        // 1.13+
        StructureModifier<Object> modifier = packet.getModifier();
        if (suggestionsField == -1) {
            Object read;
            try {
                for (int i = 0; i < modifier.size(); i++) {
                    read = modifier.read(i);
                    if (read instanceof Suggestions) {
                        suggestionsField = i;
                        break;
                    }
                }
            } catch (NoClassDefFoundError ignored) {
                suggestionsField = -2;
            }
        }
        if (suggestionsField != -2) {
            Suggestions suggestions = (Suggestions) modifier.read(suggestionsField);

            List<Suggestion> list = suggestions.getList();
            Suggestion suggestion;
            for (int i = 0; i < list.size(); i++) {
                suggestion = list.get(i);
                list.set(i, new Suggestion(suggestion.getRange(),
                        getReplacedText(packetEvent, user, listenType, suggestion.getText(), filter),
                        suggestion.getTooltip()));
            }
            return;
        }

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
