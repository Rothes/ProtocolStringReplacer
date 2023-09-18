package me.rothes.protocolstringreplacer.packetlisteners.server.actionbar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerComponentsPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import org.jetbrains.annotations.NotNull;

public class ChatActionBar extends AbstractServerComponentsPacketListener {

    public ChatActionBar() {
        super(PacketType.Play.Server.CHAT, ListenType.ACTIONBAR);
    }

    @Override
    protected void process(@NotNull PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();

        if (packet.getChatTypes().read(0) != EnumWrappers.ChatType.GAME_INFO
                && (packet.getBytes().size() < 1 || packet.getBytes().read(0) != 2)) {
            // Not a ActionBar Message.
            return;
        }

        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }

        StructureModifier<WrappedChatComponent> componentModifier = packet.getChatComponents();
        WrappedChatComponent wrappedChatComponent = componentModifier.read(0);
        String replaced;

        if (wrappedChatComponent != null) {
            String json = wrappedChatComponent.getJson();
            replaced = getReplacedJson(packetEvent, user, listenType, json, filter);
        } else {
            StructureModifier<Object> modifier = packet.getModifier();
            replaced = processSpigotComponent(modifier, packetEvent, user);
            if (replaced == null) {
                replaced = processPaperComponent(modifier, packetEvent, user);
            }
        }

        if (replaced != null) {
            componentModifier.write(0, WrappedChatComponent.fromJson(replaced));
        }
    }

}
