package me.Rothes.ProtocolStringReplacer.PacketListeners.Server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.User.User;

public final class Title extends AbstractServerPacketListener {

    public Title() {
        super(PacketType.Play.Server.TITLE);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            PacketContainer packet = packetEvent.getPacket();
            User user = getEventUser(packetEvent);
            StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
            WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
            String currentTitle = jsonToLegacyText(wrappedChatComponent.getJson());
            user.setCurrentWindowTitle(currentTitle);
            wrappedChatComponent.setJson(legacyTextToJson(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(currentTitle, user, filter)));
            wrappedChatComponentStructureModifier.write(0, wrappedChatComponent);
        }
    };

}
