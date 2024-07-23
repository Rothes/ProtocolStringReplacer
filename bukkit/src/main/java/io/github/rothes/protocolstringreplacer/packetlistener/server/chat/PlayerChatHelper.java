package io.github.rothes.protocolstringreplacer.packetlistener.server.chat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.api.exceptions.IncompatibleServerException;
import io.github.rothes.protocolstringreplacer.nms.NmsManager;
import io.github.rothes.protocolstringreplacer.nms.packetreader.ChatType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;

import java.lang.reflect.Field;
import java.util.Optional;

public class PlayerChatHelper {

    public static Version version;

    // 1.19
    private static Class<?> chatSenderClass;

    // 1.19.1
    private static Class<?> playerChatMessageClass;
    private static Class<?> messageBodyClass;
    private static Class<?> messageBodySubClass;
    private static Class<?> chatMessageTypeSubClass;
    private static Field chatMessageField;
    private static Field messageBodyField;
    private static Field messageContentField;
    private static Field messageStringField; // 1.19.3
    private static Field playerChatMessageComponentField;
    private static Field stringField;
    private static Field componentField;

    private static Field displayNameField;
    private static Field teamNameField;
    private static Field chatTypeField;

    static {
        byte serverMajorVersion = ProtocolStringReplacer.getInstance().getServerMajorVersion();
        byte serverMinorVersion = ProtocolStringReplacer.getInstance().getServerMinorVersion();
        if (serverMajorVersion <= 18) {
            version = Version.V8_0_TO_V18_2;
        } else if (serverMajorVersion == 19){
            if (serverMinorVersion == 0) {
                version = Version.V19_0;
            } else if (serverMinorVersion <= 2) {
                version = Version.V19_1_TO_V19_2;
            } else if (serverMinorVersion == 3) {
                version = Version.V19_3;
            } else {
                version = Version.V19_4;
            }
        } else {
            version = Version.V19_4;
        }

        if (version != Version.V8_0_TO_V18_2) {
            try {
                messageBodyClass = MinecraftReflection.getMinecraftClass("network.chat.SignedMessageBody");
            } catch (Throwable ignored) {}

            try {
                // 1.19.1+
                playerChatMessageClass = MinecraftReflection.getMinecraftClass("network.chat.PlayerChatMessage");
                for (Field declaredField : playerChatMessageClass.getDeclaredFields()) {
                    if (declaredField.getType() == Optional.class) {
                        declaredField.setAccessible(true);
                        playerChatMessageComponentField = declaredField;
                        break;
                    }
                }

                Class<?> chatMessageTypeClass = net.minecraft.network.chat.ChatType.class;

                for (Field field : PacketType.Play.Server.CHAT.getPacketClass().getDeclaredFields()) {
                    Class<?> declaringClass = field.getType().getDeclaringClass();
                    if (declaringClass == chatMessageTypeClass) {
                        chatMessageTypeSubClass = field.getType();
                        setupNameFields(chatMessageTypeSubClass);
                    } else if (messageBodyClass != null && declaringClass == messageBodyClass) {
                        // 1.19.3 only
                        messageBodySubClass = field.getType();
                        for (Field declaredField : messageBodySubClass.getDeclaredFields()) {
                            if (declaredField.getType() == String.class) {
                                messageStringField = declaredField;
                                messageStringField.setAccessible(true);
                                break;
                            }
                        }

                    } else if (field.getType() == playerChatMessageClass) {
                        chatMessageField = field;
                        chatMessageField.setAccessible(true);

                        for (Field declaredField : playerChatMessageClass.getDeclaredFields()) {
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

    public static Class<?> getMessageBodySubClass() {
        return messageBodySubClass;
    }

    public static Object getComponentHolder(Object playerChatMessage) {
        try {
            return messageContentField.get(messageBodyField.get((playerChatMessage)));
        } catch (IllegalAccessException e) {
            throw new IncompatibleServerException(e);
        }
    }

    public static WrappedChatComponent getOptionalChatMessage(Object playerChatMessage) {
        try {
            return WrappedChatComponent.fromHandle(((Optional<Object>)playerChatMessageComponentField.get((playerChatMessage))).get());
        } catch (IllegalAccessException e) {
            throw new IncompatibleServerException(e);
        }
    }

    public static WrappedChatComponent getChatMessage(Object playerChatMessage) {
        try {
            return WrappedChatComponent.fromHandle(componentField.get(messageContentField.get(messageBodyField.get((playerChatMessage)))));
        } catch (IllegalAccessException e) {
            throw new IncompatibleServerException(e);
        }
    }

    public static WrappedChatComponent getChatMessageR3(Object messageBody) {
        try {
            return WrappedChatComponent.fromLegacyText((String) messageStringField.get(messageBody));
        } catch (IllegalAccessException e) {
            throw new IncompatibleServerException(e);
        }
    }

    public static WrappedChatComponent getChatMessageByHolder(Object componentHolder) {
        try {
            return WrappedChatComponent.fromHandle(componentField.get(componentHolder));
        } catch (IllegalAccessException e) {
            throw new IncompatibleServerException(e);
        }
    }

    public static void setChatMessage(Object playerChatMessage, WrappedChatComponent wrappedChatComponent) {
        try {
            componentField.set(messageContentField.get(messageBodyField.get((playerChatMessage))), wrappedChatComponent.getHandle());
        } catch (IllegalAccessException e) {
            throw new IncompatibleServerException(e);
        }
    }

    public static void setChatMessageByHolder(Object componentHolder, WrappedChatComponent wrappedChatComponent) {
        try {
            componentField.set(componentHolder, wrappedChatComponent.getHandle());
        } catch (IllegalAccessException e) {
            throw new IncompatibleServerException(e);
        }
    }

    public static Object getChatMessageTypeSub(StructureModifier<Object> modifier) {
        return modifier.withType(getChatMessageTypeSubClass()).read(0);
    }

    public static int getChatTypeId(Object object) {
        try {
            return (int) chatTypeField.get(object);
        } catch (IllegalAccessException e) {
            throw new IncompatibleServerException(e);
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
            throw new IncompatibleServerException(e);
        }
    }

    public static void setDisplayName(Object object, WrappedChatComponent wrappedChatComponent) {
        try {
            displayNameField.set(object, wrappedChatComponent);
        } catch (IllegalAccessException e) {
            throw new IncompatibleServerException(e);
        }
    }

    public static BaseComponent getTeamName(Object object) {
        return ComponentSerializer.parse(getTeamNameWrapped(object).getJson())[0];
    }

    public static WrappedChatComponent getTeamNameWrapped(Object object) {
        try {
            Object o = teamNameField.get(object);
            if (o == null) {
                return null;
            }
            return WrappedChatComponent.fromHandle(o);
        } catch (IllegalAccessException e) {
            throw new IncompatibleServerException(e);
        }
    }

    public static void setTeamName(Object object, WrappedChatComponent wrappedChatComponent) {
        try {
            teamNameField.set(object, wrappedChatComponent);
        } catch (IllegalAccessException e) {
            throw new IncompatibleServerException(e);
        }
    }

    public static Class<?> getChatSenderClass() {
        return chatSenderClass;
    }

    public static Class<?> getChatMessageTypeSubClass() {
        return chatMessageTypeSubClass;
    }

    public static ChatType getChatType(ClientboundPlayerChatPacket packet) {
        return NmsManager.INSTANCE.getPacketReader().readChatType(packet);
    }

    public enum Version {
        V8_0_TO_V18_2,
        V19_0,
        V19_1_TO_V19_2,
        V19_3,
        V19_4
    }

}
