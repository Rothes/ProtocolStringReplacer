package io.github.rothes.protocolstringreplacer.packetlistener.server.bossbar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.server.AbstractServerPacketListener;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;

public final class BossBarPost17 extends AbstractServerPacketListener {

    private Field actionField;
    private final HashMap<Class<?>, FieldAccessor> actionComponentField = new HashMap<>();

    public BossBarPost17() {
        super(PacketType.Play.Server.BOSS, ListenType.BOSS_BAR);
    }

    @Override
    protected void register() {
        Class<?> packetClass = PacketType.Play.Server.BOSS.getPacketClass();
        Class<?> actionInterface = null;
        for (Class<?> declaredClass : packetClass.getDeclaredClasses()) {
            if (declaredClass.isInterface() &&
                    (declaredClass.getSimpleName().equals("Action") // Spigot mappings
                            || declaredClass.getSimpleName().equals("Operation") // Mojang mappings
                    )) {
                actionInterface = declaredClass;
                break;
            }
        }
        if (actionInterface == null) {
            throw new UnsupportedOperationException("Error when hooking into BOSS packet");
        }

        for (Field declaredField : packetClass.getDeclaredFields()) {
            if (declaredField.getType() == actionInterface) {
                actionField = declaredField;
                actionField.setAccessible(true);
                break;
            }
        }
        if (actionField == null) {
            throw new UnsupportedOperationException("Error when hooking into BOSS packet");
        }

        for (Class<?> declaredClass : packetClass.getDeclaredClasses()) {
            if (declaredClass.getInterfaces().length != 0 && declaredClass.getInterfaces()[0] == actionInterface) {
                for (Field field : declaredClass.getDeclaredFields()) {
                    if (field.getType() == MinecraftReflection.getIChatBaseComponentClass()) {
                        actionComponentField.put(declaredClass, Accessors.getFieldAccessor(field));
                    }
                }
            }
        }
        if (actionComponentField.isEmpty()) {
            throw new UnsupportedOperationException("Error when hooking into BOSS packet");
        }
        super.register();
    }

    protected void process(@NotNull PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        Object handle = packetEvent.getPacket().getHandle();
        try {
            Object action = actionField.get(handle);
            if (action == null) {
                return;
            }
            FieldAccessor field = actionComponentField.get(action.getClass());
            if (field != null) {
                WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromHandle(field.get(action));
                String replacedJson = getReplacedJson(packetEvent, user, listenType, wrappedChatComponent.getJson(), filter);
                if (replacedJson != null) {
                    wrappedChatComponent.setJson(replacedJson);
                    field.set(action, wrappedChatComponent.getHandle());
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


}
