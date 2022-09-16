package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ListenType;

import java.lang.reflect.Field;

public final class OpenWindow extends AbstractServerPacketListener {

    private Field windowTypeField = null;
    private Object anvilType = null;

    public OpenWindow() {
        super(PacketType.Play.Server.OPEN_WINDOW, ListenType.WINDOW_TITLE);

        Class<?> packetClass = PacketType.Play.Server.OPEN_WINDOW.getPacketClass();
        for (Field declaredField : packetClass.getDeclaredFields()) {
            if (declaredField.getType() == String.class) {
                return;
            }
            if (declaredField.getType() != int.class && declaredField.getType() != MinecraftReflection.getIChatBaseComponentClass()) {
                windowTypeField = declaredField;
                windowTypeField.setAccessible(true);
                try {
                    anvilType = windowTypeField.getType().getDeclaredField("h").get(null);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();
        StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
        WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
        String json = wrappedChatComponent.getJson();

        WrappedChatComponent replaced = getReplacedJsonWrappedComponent(packetEvent, user, listenType, json, filter, true);
        if (replaced != null) {
            wrappedChatComponentStructureModifier.write(0, replaced);

            if (windowTypeField != null) {
                // 1.19+
                try {
                    if (windowTypeField.get(packet.getHandle()) == anvilType) {
                        user.setInAnvil(true);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                StructureModifier<String> strings = packet.getStrings();
                if (strings.size() != 0) {
                    // Really legacy servers
                    if (strings.read(0).equals("minecraft:anvil")) {
                        user.setInAnvil(true);
                    }
                    return;
                }

                if (packet.getIntegers().read(1) == 7) {
                    user.setInAnvil(true);
                }
            }
        }
    }

}
