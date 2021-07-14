package me.Rothes.ProtocolStringReplacer.PacketListeners.Server;

import com.comphenix.protocol.wrappers.BukkitConverters;
import io.papermc.paper.text.PaperComponents;
import me.Rothes.ProtocolStringReplacer.Replacer.ListenType;
import me.Rothes.ProtocolStringReplacer.User.User;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.List;
import java.util.Optional;

public final class Chat extends AbstractServerPacketListener {

    private GsonComponentSerializer paperGsonComponentSerializer;

    public Chat() {
        super(PacketType.Play.Server.CHAT, ListenType.CHAT);
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
                    BaseComponent[] baseComponents = ComponentSerializer.parse(wrappedChatComponent.getJson());
                    baseComponents = ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedComponents(baseComponents, user, filter);
                    wrappedChatComponentStructureModifier.write(0, WrappedChatComponent.fromJson(ComponentSerializer.toString(baseComponents)));
                } else {
                    StructureModifier<Object> structureModifier = packet.getModifier();
                    for (int fieldIndex = 1; fieldIndex < 3; fieldIndex++) {
                        Object read = structureModifier.read(fieldIndex);
                        if (read instanceof BaseComponent[]) {
                            structureModifier.write(fieldIndex, ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedComponents((BaseComponent[]) read, user, filter));
                        } else if (isPaperComponent(read)) {
                            structureModifier.write(fieldIndex, getPaperGsonComponentSerializer().deserialize(
                                    ComponentSerializer.toString(
                                            ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedComponents(
                                                    ComponentSerializer.parse(StringEscapeUtils.unescapeJson(getPaperGsonComponentSerializer().serialize((net.kyori.adventure.text.Component) read))), user, filter
                                            )
                                    )
                            ));
                        }
                    }
                }
            }
        }
    };

    private boolean isPaperComponent(Object object) {
        return ProtocolStringReplacer.getInstance().isPaper() && object instanceof net.kyori.adventure.text.Component;
    }

    private GsonComponentSerializer getPaperGsonComponentSerializer() {
        if (paperGsonComponentSerializer == null) {
            paperGsonComponentSerializer = PaperComponents.gsonSerializer();
        }
        return paperGsonComponentSerializer;
    }

}
