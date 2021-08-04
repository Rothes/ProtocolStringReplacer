package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.user.User;
import net.md_5.bungee.chat.ComponentSerializer;

public final class BossBar extends AbstractServerPacketListener {

    public BossBar() {
        super(PacketType.Play.Server.BOSS, ListenType.BOSS_BAR);
    }

    protected void process(PacketEvent packetEvent) {
        User user = getEventUser(packetEvent);
        StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packetEvent.getPacket().getChatComponents();
        if (wrappedChatComponentStructureModifier.size() != 0) {
            WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
            String replacedJson = ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedJson(wrappedChatComponent.getJson(), user, filter, false);
            wrappedChatComponent.setJson(ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                    .getReplacedComponents(ComponentSerializer.parse(replacedJson), user, filter)));
            wrappedChatComponentStructureModifier.write(0, wrappedChatComponent);
        }
    }

}
