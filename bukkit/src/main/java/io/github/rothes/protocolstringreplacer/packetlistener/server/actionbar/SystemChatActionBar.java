package io.github.rothes.protocolstringreplacer.packetlistener.server.actionbar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.server.BaseServerComponentsPacketListener;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;

public class SystemChatActionBar extends BaseServerComponentsPacketListener {

    public SystemChatActionBar() {
        super(PacketType.Play.Server.SYSTEM_CHAT, ListenType.ACTIONBAR);
    }

    protected void process(PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();

        StructureModifier<Boolean> booleans = packet.getBooleans();
        if (booleans.size() == 1) {
            if (!booleans.read(0)) {
                return;
            }
        } else if (packet.getIntegers().read(0) != EnumWrappers.ChatType.GAME_INFO.getId()) {
            return;
        }

        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }

        StructureModifier<String> stringModifier = packet.getStrings();
        if (stringModifier.size() == 0) {
            // Since 1.20.3
            StructureModifier<WrappedChatComponent> components = packet.getChatComponents();
            WrappedChatComponent replaced = getReplacedJsonWrappedComponent(packetEvent, user, listenType, components.read(0).getJson(), filter);
            if (replaced == null) {
                return;
            }
            components.write(0, replaced);
        } else {
            String replaced;
            String read = stringModifier.read(0);
            if (read != null) {
                replaced = getReplacedJson(packetEvent, user, listenType, read, filter);
            } else {
                replaced = processPaperComponent(packet.getModifier(), packetEvent, user);
            }

            if (replaced != null) {
                stringModifier.write(0, replaced);
            }
        }
    }

}
