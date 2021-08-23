package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.papermc.paper.text.PaperComponents;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.user.User;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Optional;

public final class Chat extends AbstractServerPacketListener {

    private GsonComponentSerializer paperGsonComponentSerializer;

    public Chat() {
        super(PacketType.Play.Server.CHAT, ListenType.CHAT);
    }

    protected void process(PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        Optional<Boolean> isFiltered = packet.getMeta("psr_filtered_packet");
        if (!(isFiltered.isPresent() && isFiltered.get())) {
            User user = getEventUser(packetEvent);
            StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
            WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
            String json;
            if (wrappedChatComponent != null) {
                json = wrappedChatComponent.getJson();
                saveCaptureMessage(user, json);
                json = ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedJson(json, user, filter, false);
                BaseComponent[] replacedComponents = ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedComponents(ComponentSerializer.parse(json), user, filter);
                wrappedChatComponentStructureModifier.write(0, WrappedChatComponent.fromJson(ComponentSerializer.toString(replacedComponents)));
            } else {
                StructureModifier<Object> structureModifier = packet.getModifier();
                for (int fieldIndex = 1; fieldIndex < 3; fieldIndex++) {
                    Object read = structureModifier.read(fieldIndex);
                    if (read instanceof BaseComponent[]) {
                        BaseComponent[] readComponents = (BaseComponent[]) read;
                        json = ComponentSerializer.toString(readComponents);
                        saveCaptureMessage(user, json);
                        readComponents = ComponentSerializer.parse(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedJson(
                                json, user, filter, false
                        ));
                        structureModifier.write(fieldIndex, ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedComponents(readComponents, user, filter));
                    } else if (isPaperComponent(read)) {
                        json = getPaperGsonComponentSerializer().serialize((net.kyori.adventure.text.Component) read);
                        saveCaptureMessage(user, json);
                        structureModifier.write(fieldIndex, getPaperGsonComponentSerializer().deserialize(
                                ComponentSerializer.toString(
                                        ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedComponents(
                                                ComponentSerializer.parse(
                                                        ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedJson(
                                                                json, user, filter, false))
                                                , user, filter
                                        )
                                )
                        ));
                    }
                }
            }
        }
    }

    private boolean isPaperComponent(Object object) {
        return ProtocolStringReplacer.getInstance().hasPaperComponent() && object instanceof net.kyori.adventure.text.Component;
    }

    private GsonComponentSerializer getPaperGsonComponentSerializer() {
        if (paperGsonComponentSerializer == null) {
            paperGsonComponentSerializer = PaperComponents.gsonSerializer();
        }
        return paperGsonComponentSerializer;
    }

}
