package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.user.User;
import net.md_5.bungee.chat.ComponentSerializer;

public class SetSubtitleText extends AbstractServerPacketListener {

    public SetSubtitleText() {
        super(PacketType.Play.Server.SET_SUBTITLE_TEXT, ListenType.TITLE);
    }

    protected void process(PacketEvent packetEvent) {
        User user = getEventUser(packetEvent);
        StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packetEvent.getPacket().getChatComponents();
        WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
        String json;
        if (wrappedChatComponent != null) {
            json = wrappedChatComponent.getJson();
            saveCaptureMessage(user, json);
            wrappedChatComponent.setJson(ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                    .getReplacedComponents(ComponentSerializer.parse(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedJson(
                            json, user, filter, false
                    )), user, filter)));
            wrappedChatComponentStructureModifier.write(0, wrappedChatComponent);
        }
    }

}
