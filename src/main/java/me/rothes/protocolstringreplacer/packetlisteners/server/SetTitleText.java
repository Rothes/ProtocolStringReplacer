package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.user.User;
import net.md_5.bungee.chat.ComponentSerializer;

public class SetTitleText extends AbstractServerPacketListener {

    public SetTitleText() {
        super(PacketType.Play.Server.SET_TITLE_TEXT, ListenType.TITLE);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            User user = getEventUser(packetEvent);
            StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packetEvent.getPacket().getChatComponents();
            WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
            if (wrappedChatComponent != null) {
                wrappedChatComponent.setJson(ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                        .getReplacedComponents(ComponentSerializer.parse(wrappedChatComponent.getJson()), user, filter)));
                wrappedChatComponentStructureModifier.write(0, wrappedChatComponent);
            }
        }
    };

}
