package me.Rothes.ProtocolStringReplacer.PacketListeners.Server;

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
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.Validate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Chat extends AbstractServerPacketListener {

    public Chat() {
        super(PacketType.Play.Server.CHAT);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            PacketContainer packet = packetEvent.getPacket();
            User user = getEventUser(packetEvent);

            StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
            WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
            if (wrappedChatComponent != null) {
                // TODO
                wrappedChatComponent.setJson(ProtocolStringReplacer.getReplacerManager().getReplacedString(wrappedChatComponent.getJson(), user, filter));
                wrappedChatComponentStructureModifier.write(0, wrappedChatComponent);
            } else {
                StructureModifier<Object> structureModifier = packet.getModifier();
                BaseComponent[] baseComponents = ((BaseComponent[]) structureModifier.read(2));
                if (baseComponents != null) {
                    for (BaseComponent baseComponent : baseComponents) {
                        baseComponent = getReplacedComponent(baseComponent, user);
                        if (hasExtra(baseComponent)) {
                            baseComponent.setExtra(getReplacedExtra(baseComponent, user));
                        }
                    }
                    structureModifier.write(2, baseComponents);
                }
            }
        }
    };

    private boolean hasExtra(@Nonnull BaseComponent baseComponent) {
        return baseComponent.getExtra() != null;
    }

    @Nonnull
    private List<BaseComponent> getReplacedExtra(@Nonnull BaseComponent baseComponent, User user) {
        Validate.notNull(baseComponent, "BaseComponent cannot be null");
        List<BaseComponent> replacedExtra = new ArrayList<>();
        for (BaseComponent extra : baseComponent.getExtra()) {
            // TODO
            extra = getReplacedComponent(extra, user);
            if (hasExtra(extra)) {
                extra.setExtra(getReplacedExtra(extra, user));
            }
            replacedExtra.add(extra);
        }
        return replacedExtra;
    }

    @Nonnull
    private BaseComponent getReplacedComponent(@Nonnull BaseComponent baseComponent, User user) {
        Validate.notNull(baseComponent, "BaseComponent cannot be null");
        BaseComponent replaced = new TextComponent(ProtocolStringReplacer.getReplacerManager().getReplacedString(baseComponent.toPlainText(), user, filter));
        replaced.copyFormatting(baseComponent);
        return replaced;
    }

}