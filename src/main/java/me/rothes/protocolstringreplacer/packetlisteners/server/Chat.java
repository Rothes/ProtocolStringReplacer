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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public final class Chat extends AbstractServerPacketListener {

    private boolean setup = false;
    private int bungeeComponentsField = -1;
    private int paperComponentField = -1;
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
                StructureModifier<Object> modifier = packet.getModifier();
                setupFields(modifier.getFields());
                if (bungeeComponentsField != -1) {
                    ProtocolStringReplacer.info(String.valueOf(modifier.getFields()));
                    Object obj = modifier.read(bungeeComponentsField);

                    if (obj == null) {
                        if (paperComponentField != -1) {
                            obj = modifier.read(paperComponentField);
                            Component component = (Component) obj;
                            json = getPaperGsonComponentSerializer().serialize(component);

                            WrappedChatComponent replaced = getReplacedJsonWrappedComponent(packetEvent, user, listenType, json, filter);
                            if (replaced != null) {
                                wrappedChatComponentStructureModifier.write(0, replaced);
                                modifier.write(paperComponentField, null);
                            }
                        }
                        return;
                    }

                    BaseComponent[] components = (BaseComponent[]) obj;
                    json = ComponentSerializer.toString(components);

                    WrappedChatComponent replaced = getReplacedJsonWrappedComponent(packetEvent, user, listenType, json, filter);
                    if (replaced != null) {
                        wrappedChatComponentStructureModifier.write(0, replaced);
                        modifier.write(bungeeComponentsField, null);
                    }
                }
            }
        }
    }

    private void setupFields(List<Field> fields) {
        if (setup) {
            return;
        }
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            Class<?> type = field.getType();
            if (ProtocolStringReplacer.getInstance().isSpigot() && type.getCanonicalName().equals("net.md_5.bungee.api.chat.BaseComponent[]")) {
                ProtocolStringReplacer.info(field +".");
                bungeeComponentsField = i;
            } else if (ProtocolStringReplacer.getInstance().hasPaperComponent() && type == Component.class) {
                paperComponentField = i;
            }
        }
        setup = true;
    }

    private GsonComponentSerializer getPaperGsonComponentSerializer() {
        if (paperGsonComponentSerializer == null) {
            paperGsonComponentSerializer = PaperComponents.gsonSerializer();
        }
        return paperGsonComponentSerializer;
    }

}
