package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public final class OpenWindow extends AbstractServerPacketListener {

    public OpenWindow() {
        super(PacketType.Play.Server.OPEN_WINDOW, ListenType.WINDOW_TITLE);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ProtocolStringReplacer.getInstance().getConfigManager().listenerPriority, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            if (packetEvent.isReadOnly()) {
                return;
            }
            PacketContainer packet = packetEvent.getPacket();
            StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
            WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
            String json = wrappedChatComponent.getJson();
            User user = getEventUser(packetEvent);

            StringBuilder currentTitle = new StringBuilder();
            BaseComponent[] baseComponents = ComponentSerializer.parse(json);
            for (BaseComponent baseComponent : baseComponents) {
                if (baseComponent instanceof TextComponent) {
                    TextComponent textComponent = (TextComponent) baseComponent;
                    if (textComponent.getColorRaw() != null) {
                        currentTitle.append(textComponent.getColorRaw());
                    }
                    if (textComponent.isBoldRaw() != null && textComponent.isBoldRaw()) {
                        currentTitle.append("§l");
                    }
                    if (textComponent.isItalicRaw() != null && textComponent.isItalicRaw()) {
                        currentTitle.append("§o");
                    }
                    if (textComponent.isObfuscatedRaw() != null && textComponent.isObfuscatedRaw()) {
                        currentTitle.append("§m");
                    }
                    if (textComponent.isUnderlinedRaw() != null && textComponent.isUnderlinedRaw()) {
                        currentTitle.append("§n");
                    }
                    currentTitle.append(textComponent.getText());
                }
            }
            user.setCurrentWindowTitle(currentTitle.toString());

            wrappedChatComponent.setJson(ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                    .getReplacedComponents(ComponentSerializer.parse(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedJson(
                            json, user, filter, false
                    )), user, filter)));
            wrappedChatComponentStructureModifier.write(0, wrappedChatComponent);
        }
    };

}
