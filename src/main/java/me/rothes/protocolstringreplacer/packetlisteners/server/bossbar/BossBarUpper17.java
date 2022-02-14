package me.rothes.protocolstringreplacer.packetlisteners.server.bossbar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;

import java.lang.reflect.Field;
import java.util.HashMap;

public class BossBarUpper17 extends AbstractServerPacketListener {

    private Field actionField;
    private HashMap<Class<?>, Field> actionComponentField = new HashMap<>();
    private final boolean hooked;

    public BossBarUpper17() {
        super(PacketType.Play.Server.BOSS, ListenType.BOSS_BAR);
        Class<?> packetClass = PacketType.Play.Server.BOSS.getPacketClass();
        Class<?> actionInterface = null;
        for (Class<?> declaredClass : packetClass.getDeclaredClasses()) {
            if (declaredClass.isInterface() && declaredClass.getSimpleName().equals("Action")) {
                actionInterface = declaredClass;
                break;
            }
        }
        if (actionInterface == null) {
            ProtocolStringReplacer.error("§4Error when hooking into BOSS packet: 0");
            hooked = false;
            return;
        }

        for (Field declaredField : packetClass.getDeclaredFields()) {
            if (declaredField.getType() == actionInterface) {
                actionField = declaredField;
                actionField.setAccessible(true);
                break;
            }
        }
        if (actionField == null) {
            ProtocolStringReplacer.error("§4Error when hooking into BOSS packet: 1");
            hooked = false;
            return;
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
        if (actionComponentField.size() != 0) {
            hooked = true;
        } else {
            ProtocolStringReplacer.error("§4Error when hooking into BOSS packet: 2");
            hooked = false;
        }

    }

    protected void process(PacketEvent packetEvent) {
        if (!hooked) {
            return;
        }
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
