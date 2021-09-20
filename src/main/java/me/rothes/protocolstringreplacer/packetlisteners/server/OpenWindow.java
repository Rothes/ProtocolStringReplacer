package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.utils.ColorUtils;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public final class OpenWindow extends AbstractServerPacketListener {

    public OpenWindow() {
        super(PacketType.Play.Server.OPEN_WINDOW, ListenType.WINDOW_TITLE);
    }

    protected void process(PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
        WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
        String json = wrappedChatComponent.getJson();
        User user = getEventUser(packetEvent);
        saveCaptureMessage(user, json);

        StringBuilder currentTitle = new StringBuilder();
        BaseComponent[] baseComponents = ComponentSerializer.parse(json);
        for (BaseComponent baseComponent : baseComponents) {
            if (baseComponent instanceof TextComponent) {
                TextComponent textComponent = (TextComponent) baseComponent;
                currentTitle.append(ColorUtils.getTextColor(textComponent));
                currentTitle.append(textComponent.getText());
            }
        }
        user.setCurrentWindowTitle(currentTitle.toString());

        WrappedChatComponent replaced = getReplacedJsonWrappedComponent(packetEvent, user, json, filter);
        if (replaced != null) {
            wrappedChatComponentStructureModifier.write(0,
                    replaced);
        }
    }

}
