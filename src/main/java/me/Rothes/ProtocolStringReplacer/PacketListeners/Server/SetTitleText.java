package me.Rothes.ProtocolStringReplacer.PacketListeners.Server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.User.User;

public class SetTitleText extends AbstractServerPacketListener {

    public SetTitleText() {
        super(PacketType.Play.Server.SET_TITLE_TEXT);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            User user = getEventUser(packetEvent);
            StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packetEvent.getPacket().getChatComponents();
            WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
            String currentTitle = jsonToLegacyText(wrappedChatComponent.getJson());
            user.setCurrentWindowTitle(currentTitle);
            wrappedChatComponent.setJson(legacyTextToJson(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(currentTitle, user, filter)));
            wrappedChatComponentStructureModifier.write(0, wrappedChatComponent);
        }
    };

}
