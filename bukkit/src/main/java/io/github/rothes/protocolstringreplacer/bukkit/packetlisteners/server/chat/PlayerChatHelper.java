package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.chat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.bukkit.ProtocolStringReplacer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.lang.reflect.Field;

public class PlayerChatHelper {

    // 1.19
    private static Class<?> chatSenderClass;

    // 1.19.1
    private static Class<?> playerChatMessageClass;
    private static Class<?> chatMessageTypeSubClass;
    private static Field chatMessageField;
    private static Field messageBodyField;
    private static Field messageContentField;
    private static Field stringField;
    private static Field componentField;

    private static Field displayNameField;
    private static Field teamNameField;
    private static Field chatTypeField;

    static {
        try {
            // 1.19.1+
            playerChatMessageClass = MinecraftReflection.getMinecraftClass("network.chat.PlayerChatMessage");
            Class<?> chatMessageTypeClass = MinecraftReflection.getMinecraftClass("network.chat.ChatMessageType");

            for (Field field : PacketType.Play.Server.CHAT.getPacketClass().getDeclaredFields()) {
                Class<?> declaringClass = field.getType().getDeclaringClass();
                if (declaringClass == chatMessageTypeClass) {
                    chatMessageTypeSubClass = field.getType();
                    setupNameFields(chatMessageTypeSubClass);
                } else if (field.getType() == playerChatMessageClass) {
                    chatMessageField = field;
                    chatMessageField.setAccessible(true);

                    for (Field declaredField : playerChatMessageClass.getDeclaredFields()) {
                        Class<?> messageBodyClass = MinecraftReflection.getMinecraftClass("network.chat.SignedMessageBody");
                        if (declaredField.getType() == messageBodyClass) {
                            messageBodyField = declaredField;
                            messageBodyField.setAccessible(true);

                            Class<?> messageContentClass = MinecraftReflection.getMinecraftClass("network.chat.ChatMessageContent");
                            for (Field messageContentClassDeclaredField : messageBodyClass.getDeclaredFields()) {
                                if (messageContentClassDeclaredField.getType() == messageContentClass) {
                                    messageContentField = messageContentClassDeclaredField;
                                    messageContentField.setAccessible(true);

                                    for (Field contentClassDeclaredField : messageContentClass.getDeclaredFields()) {
                                        if (contentClassDeclaredField.getType() == String.class) {
                                            stringField = contentClassDeclaredField;
                                            stringField.setAccessible(true);
                                        } else if (contentClassDeclaredField.getType() == MinecraftReflection.getIChatBaseComponentClass()) {
                                            componentField = contentClassDeclaredField;
                                            componentField.setAccessible(true);
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }

            if (chatMessageTypeSubClass == null) {
                // Not 1.19.1+, it's 1.19
                chatSenderClass = MinecraftReflection.getMinecraftClass("network.chat.ChatSender");
                setupNameFields(chatSenderClass);
            }
        } catch (Throwable t) {
            ProtocolStringReplacer.warn("Unable to init PlayerChatHelper. PlayerChat packet handle may not work.", t);
        }
    }

    private static void setupNameFields(Class<?> clazz) {
        Class<?> componentClass = MinecraftReflection.getIChatBaseComponentClass();
        for (Field field : clazz.getDeclaredFields()) {
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

            } else if (field.getType() == int.class) {
                chatTypeField = field;
                chatTypeField.setAccessible(true);
            }
        }
    }

    public static boolean isLegacy() {
        return chatSenderClass != null;
    }

    public static Object getChatSender(StructureModifier<Object> modifier) {
        return modifier.withType(getChatSenderClass()).read(0);
    }

    public static Class<?> getPlayerChatMessageClass() {
        return playerChatMessageClass;
    }

    public static Object getComponentHolder(Object playerChatMessage) {
        try {
            return messageContentField.get(messageBodyField.get((playerChatMessage)));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static WrappedChatComponent getChatMessage(Object playerChatMessage) {
        try {
            return WrappedChatComponent.fromHandle(componentField.get(messageContentField.get(messageBodyField.get((playerChatMessage)))));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static WrappedChatComponent getChatMessageByHolder(Object componentHolder) {
        try {
            return WrappedChatComponent.fromHandle(componentField.get(componentHolder));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setChatMessage(Object playerChatMessage, WrappedChatComponent wrappedChatComponent) {
        try {
            componentField.set(messageContentField.get(messageBodyField.get((playerChatMessage))), wrappedChatComponent.getHandle());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setChatMessageByHolder(Object componentHolder, WrappedChatComponent wrappedChatComponent) {
        try {
            componentField.set(componentHolder, wrappedChatComponent.getHandle());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getChatMessageTypeSub(StructureModifier<Object> modifier) {
        return modifier.withType(getChatMessageTypeSubClass()).read(0);
    }

    public static int getChatTypeId(Object object) {
        try {
            return (int) chatTypeField.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static BaseComponent getDisplayName(Object object) {
        return ComponentSerializer.parse(getDisplayNameWrapped(object).getJson())[0];
    }

    public static WrappedChatComponent getDisplayNameWrapped(Object object) {
        try {
            Object o = displayNameField.get(object);
            if (o == null) {
                return null;
            }
            return WrappedChatComponent.fromHandle(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setDisplayName(Object object, WrappedChatComponent wrappedChatComponent) {
        try {
            displayNameField.set(object, wrappedChatComponent);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static BaseComponent getTeamName(Object object) {
        return ComponentSerializer.parse(WrappedChatComponent.fromHandle(getTeamNameWrapped(object)).getJson())[0];
    }

    public static WrappedChatComponent getTeamNameWrapped(Object object) {
        try {
            Object o = teamNameField.get(object);
            if (o == null) {
                return null;
            }
            return WrappedChatComponent.fromHandle(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setTeamName(Object object, WrappedChatComponent wrappedChatComponent) {
        try {
            teamNameField.set(object, wrappedChatComponent);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getChatSenderClass() {
        return chatSenderClass;
    }

    public static Class<?> getChatMessageTypeSubClass() {
        return chatMessageTypeSubClass;
    }

    public static ChatType getChatTypeFromId(int id) {
        if (isLegacy()) {
            switch (id) {
                case 0:
                    return ChatType.PLAYER_CHAT;
                case 1:
                    return ChatType.SYSTEM_CHAT;
                case 2:
                    return ChatType.GAME_INFO;
                case 3:
                    return ChatType.SAY;
                case 4:
                    return ChatType.MSG_INCOMING;
                case 5:
                    return ChatType.TEAM_MSG_INCOMING;
                case 6:
                    return ChatType.EMOTE;
                case 7:
                    return ChatType.TELLRAW;
                default:
                    ProtocolStringReplacer.warn("Not supported PlayerChatType when convert: " + id);
                    return ChatType.SYSTEM_CHAT;
            }
        } else {
            switch (id) {
                case 0:
                    return ChatType.PLAYER_CHAT;
                case 1:
                    return ChatType.SAY;
                case 2:
                    return ChatType.MSG_INCOMING;
                case 3:
                    return ChatType.MSG_OUTGOING;
                case 4:
                    return ChatType.TEAM_MSG_INCOMING;
                case 5:
                    return ChatType.TEAM_MSG_OUTGOING;
                case 6:
                    return ChatType.EMOTE;
                case 7:
                    return ChatType.TELLRAW;
                default:
                    ProtocolStringReplacer.warn("Not supported PlayerChatType when convert: " + id);
                    return ChatType.SYSTEM_CHAT;
            }
        }
    }

    public enum ChatType {
        PLAYER_CHAT(0, 0),
        SYSTEM_CHAT(1, 1),
        GAME_INFO(2, -1),
        SAY(3, 2),
        MSG_INCOMING(4, 3),
        MSG_OUTGOING(-1, 4),
        TEAM_MSG_INCOMING(5, 5),
        TEAM_MSG_OUTGOING(-1, 6),
        EMOTE(6, 7),
        TELLRAW(7, 8);

        ChatType(int legacyId, int newId) {
            // TODO
        }
    }

}
