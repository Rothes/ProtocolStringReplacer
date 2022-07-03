package me.rothes.protocolstringreplacer.packetlisteners.server.chat;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.lang.reflect.Field;

public class ConvertChatHelper {

    private static Class<?> chatSenderClass = MinecraftReflection.getMinecraftClass("network.chat.ChatSender");
    private static Field displayNameField;
    private static Field teamNameField;

    static {
        Class<?> componentClass = MinecraftReflection.getIChatBaseComponentClass();
        for (Field field : chatSenderClass.getDeclaredFields()) {
            if (field.getType() == componentClass) {
                if (displayNameField == null) {
                    displayNameField = field;
                    field.setAccessible(true);
                } else {
                    if (teamNameField == null) {
                        teamNameField = field;
                        field.setAccessible(true);
                    } else {
                        ProtocolStringReplacer.warn("There may be errors hooking into ChatSender, which may lead to PlayerChat convert problems.");
                    }
                }
            }
        }
    }

    public static Object getChatSender(StructureModifier<Object> modifier) {
        return modifier.withType(getChatSenderClass()).read(0);
    }

    public static BaseComponent getDisplayName(Object chatSender) {
        try {
            return ComponentSerializer.parse(WrappedChatComponent.fromHandle(displayNameField.get(chatSender)).getJson())[0];
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static BaseComponent getTeamName(Object chatSender) {
        try {
            return ComponentSerializer.parse(WrappedChatComponent.fromHandle(teamNameField.get(chatSender)).getJson())[0];
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getChatSenderClass() {
        return chatSenderClass;
    }

    public static Field getDisplayNameField() {
        return displayNameField;
    }

    public static Field getTeamNameField() {
        return teamNameField;
    }

}
