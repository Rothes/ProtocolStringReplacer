package me.rothes.protocolstringreplacer.packetlisteners.server.bossbar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;

public final class BossBarPost17 extends AbstractServerPacketListener {

    private Field actionField;
    private final HashMap<Class<?>, Field> actionComponentField = new HashMap<>();

    public BossBarPost17() {
        super(PacketType.Play.Server.BOSS, ListenType.BOSS_BAR);
    }

    @Override
    protected void register() {
        Class<?> packetClass = PacketType.Play.Server.BOSS.getPacketClass();
        Class<?> actionInterface = null;
        for (Class<?> declaredClass : packetClass.getDeclaredClasses()) {
            if (declaredClass.isInterface() && declaredClass.getSimpleName().equals("Action")) {
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
                        actionComponentField.put(declaredClass, field);
                        field.setAccessible(true);
                    }
                }
            }
        }
        if (actionComponentField.size() == 0) {
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
            Field field = actionComponentField.get(action.getClass());
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
