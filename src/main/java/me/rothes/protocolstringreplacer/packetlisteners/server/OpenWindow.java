package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public final class OpenWindow extends AbstractServerPacketListener {

    public OpenWindow() {
        super(PacketType.Play.Server.OPEN_WINDOW, ListenType.WINDOW_TITLE);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            PacketContainer packet = packetEvent.getPacket();
            StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
            WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
            String json = wrappedChatComponent.getJson();
            User user = getEventUser(packetEvent);

            StringBuilder currentTitle = new StringBuilder();
            BaseComponent[] baseComponents = ComponentSerializer.parse(json);
            for (BaseComponent baseComponent : baseComponents) {
                currentTitle.append(baseComponent.toLegacyText().substring(2));
            }
            user.setCurrentWindowTitle(currentTitle.toString());

            wrappedChatComponent.setJson(ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                    .getReplacedComponents(ComponentSerializer.parse(json), user, filter)));
            wrappedChatComponentStructureModifier.write(0, wrappedChatComponent);
        }
    };

}
