package me.Rothes.ProtocolStringReplacer.PacketListeners.Server;

import me.Rothes.ProtocolStringReplacer.API.ComponentConverter;
import me.Rothes.ProtocolStringReplacer.User.User;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public final class Chat extends AbstractServerPacketListener {

    public Chat() {
        super(PacketType.Play.Server.CHAT);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            PacketContainer packet = packetEvent.getPacket();
            Optional<Boolean> isFiltered = packet.getMeta("psr_filtered_packet");
            if (!(isFiltered.isPresent() && isFiltered.get())) {
                User user = getEventUser(packetEvent);
                StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
                WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
                if (wrappedChatComponent != null) {
                    // TODO
                    wrappedChatComponent.setJson(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(wrappedChatComponent.getJson(), user, filter));
                    wrappedChatComponentStructureModifier.write(0, wrappedChatComponent);
                } else {
                    StructureModifier<Object> structureModifier = packet.getModifier();
                    int fieldIndex = 0;
                    boolean foundComponent = false;
                    Object read = null;
                    // Paper has an extra field for its component.
                    while (fieldIndex < 2 && !(read instanceof BaseComponent[] || isPaperComponent(read))) {
                        read = structureModifier.read(++fieldIndex);
                        if (read instanceof BaseComponent[] || isPaperComponent(read)) {
                            foundComponent = true;
                            break;
                        }
                    }

                    if (foundComponent) {
                        if (isPaperComponent(read)) {
                            BaseComponent[] baseComponents = getReplacedComponents(ComponentConverter.paperToSpigot((net.kyori.adventure.text.TextComponent) read), user);
                            Bukkit.getConsoleSender().sendMessage(Arrays.toString(baseComponents));
                            structureModifier.write(fieldIndex, ComponentConverter.spigotToPaper(baseComponents));
                        } else {
                            structureModifier.write(fieldIndex, getReplacedComponents((BaseComponent[]) read, user));
                        }
                    }
                }
            }
        }
    };

    private boolean hasExtra(@Nonnull BaseComponent baseComponent) {
        return baseComponent.getExtra() != null;
    }

    @Nonnull
    private BaseComponent[] getReplacedComponents(@Nonnull BaseComponent[] baseComponents, @Nonnull User user) {
        for (int i = 0; i < baseComponents.length; i++) {
            BaseComponent baseComponent = baseComponents[i];
            baseComponent = getReplacedComponent(baseComponent, user);
            if (hasExtra(baseComponent)) {
                baseComponent.setExtra(getReplacedExtra(baseComponent, user));
            }
            baseComponents[i] = baseComponent;
        }

        return baseComponents;
    }

    @Nonnull
    private List<BaseComponent> getReplacedExtra(@Nonnull BaseComponent baseComponent, @Nonnull User user) {
        Validate.notNull(baseComponent, "BaseComponent cannot be null");
        LinkedList<BaseComponent> replacedExtra = new LinkedList<>();
        for (BaseComponent extra : baseComponent.getExtra()) {
            extra = getReplacedComponent(extra, user);
            if (hasExtra(extra)) {
                extra.setExtra(getReplacedExtra(extra, user));
            }
            replacedExtra.add(extra);
        }
        return replacedExtra;
    }

    @Nonnull
    private BaseComponent getReplacedComponent(@Nonnull BaseComponent baseComponent, @Nonnull User user) {
        Validate.notNull(baseComponent, "BaseComponent cannot be null");
        if (baseComponent instanceof TextComponent) {
            TextComponent textComponent = ((TextComponent) baseComponent);
            textComponent.setText(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(((TextComponent) baseComponent).getText(), user, filter));
        }
        return baseComponent;
    }

    private boolean isPaperComponent(Object object) {
        return ProtocolStringReplacer.getInstance().isPaper() && object instanceof net.kyori.adventure.text.TextComponent;
    }

}
