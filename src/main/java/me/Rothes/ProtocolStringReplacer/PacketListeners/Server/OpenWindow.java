package me.Rothes.ProtocolStringReplacer.PacketListeners.Server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.Rothes.ProtocolStringReplacer.User.User;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class OpenWindow extends AbstractServerPacketListener {

    public OpenWindow() {
        super(PacketType.Play.Server.OPEN_WINDOW);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            PacketContainer packet = packetEvent.getPacket();
            User user = getEventUser(packetEvent);
            StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
            WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
            BaseComponent[] baseComponents = ComponentSerializer.parse(wrappedChatComponent.getJson());

            StringBuilder stringBuilder = new StringBuilder(baseComponents.length);
            for (BaseComponent baseComponent : baseComponents) {

                stringBuilder.append(baseComponent.toLegacyText().substring(2));
            }
            String currentTitle = stringBuilder.toString();
            user.setCurrentlyWindowTitle(currentTitle);
            stringBuilder = new StringBuilder(3);
            stringBuilder.append("{\"text\":\"").append(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(currentTitle, user, filter).replace("\"", "\"\"")).append("\"}");
            wrappedChatComponent.setJson(stringBuilder.toString());
            wrappedChatComponentStructureModifier.write(0, wrappedChatComponent);
        }
    };

}
