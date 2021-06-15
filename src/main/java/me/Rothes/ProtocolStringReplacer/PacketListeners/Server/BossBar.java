package me.Rothes.ProtocolStringReplacer.PacketListeners.Server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.User.User;

public final class BossBar extends AbstractServerPacketListener {

    public BossBar() {
        super(PacketType.Play.Server.BOSS);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            User user = getEventUser(packetEvent);
            StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packetEvent.getPacket().getChatComponents();
            // Temply check for ProtocolLib for 1.17.
            // However, it's from ProtocolLib. I won't fix it right now.
            if (wrappedChatComponentStructureModifier.size() != 0) {
                WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
                if (wrappedChatComponent != null) {
                    String currentTitle = jsonToLegacyText(wrappedChatComponent.getJson());
                    user.setCurrentWindowTitle(currentTitle);
                    wrappedChatComponent.setJson(legacyTextToJson(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(currentTitle, user, filter)));
                    wrappedChatComponentStructureModifier.write(0, wrappedChatComponent);
                }
            }
        }
    };

}
