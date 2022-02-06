package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.papermc.paper.text.PaperComponents;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
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
            PsrUser user = getEventUser(packetEvent);
            if (user == null) {
                return;
            }
            StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
            WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
            String json;
            if (wrappedChatComponent != null) {
                json = wrappedChatComponent.getJson();
                WrappedChatComponent replaced = getReplacedJsonWrappedComponent(packetEvent, user, listenType, json, filter);
                if (replaced != null) {
                    wrappedChatComponentStructureModifier.write(0, replaced);
                }

            } else {
                StructureModifier<Object> structureModifier = packet.getModifier();
                for (int fieldIndex = 1; fieldIndex < 4 && fieldIndex < structureModifier.size(); fieldIndex++) {
                    Object read = structureModifier.read(fieldIndex);
                    if (read instanceof BaseComponent[]) {
                        BaseComponent[] readComponents = (BaseComponent[]) read;
                        json = ComponentSerializer.toString(readComponents);

                        WrappedChatComponent replaced = getReplacedJsonWrappedComponent(packetEvent, user, listenType, json, filter);
                        if (replaced != null) {
                            wrappedChatComponentStructureModifier.write(0, replaced);
                            structureModifier.write(fieldIndex, null);
                        }
                    } else if (isPaperComponent(read)) {
                        json = getPaperGsonComponentSerializer().serialize((net.kyori.adventure.text.Component) read);

                        WrappedChatComponent replaced = getReplacedJsonWrappedComponent(packetEvent, user, listenType, json, filter);
                        if (replaced != null) {
                            wrappedChatComponentStructureModifier.write(0, replaced);
                            structureModifier.write(fieldIndex, null);
                        }
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
