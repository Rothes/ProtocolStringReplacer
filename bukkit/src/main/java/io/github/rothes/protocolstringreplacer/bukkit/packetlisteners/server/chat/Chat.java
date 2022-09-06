package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.chat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.bukkit.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.AbstractServerComponentsPacketListener;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.ListenType;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.bukkit.utils.PaperUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Arrays;
import java.util.Optional;

public final class Chat extends AbstractServerComponentsPacketListener {

    // If server is before 1.19, or it's 1.19.1+
    private final boolean legacy = Arrays.stream(PacketType.Play.Server.CHAT.getPacketClass().getDeclaredFields())
            .anyMatch(it -> it.getType() == MinecraftReflection.getIChatBaseComponentClass());

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

            if (convert(packet, user)) {
                packetEvent.setCancelled(true);
                return;
            }

            String replaced;
//            Object componentHolder = null;
            StructureModifier<WrappedChatComponent> componentModifier = null;
            WrappedChatComponent wrappedChatComponent;

            if (legacy) {
                // Before 1.19
                componentModifier = packet.getChatComponents();
                wrappedChatComponent = componentModifier.read(0);
            } else {
                // 1.19.1+
                return;
//                componentHolder = PlayerChatHelper.getComponentHolder(packet.getModifier().withType(PlayerChatHelper.getPlayerChatMessageClass()).read(0));
//                wrappedChatComponent = PlayerChatHelper.getChatMessageByHolder(componentHolder);
            }

            if (wrappedChatComponent != null) {
                String json = wrappedChatComponent.getJson();
                replaced = getReplacedJson(packetEvent, user, listenType, json, filter);
            } else {
                StructureModifier<Object> modifier = packet.getModifier();
                replaced = processSpigotComponent(modifier, packetEvent, user);
                if (replaced == null) {
                    replaced = processPaperComponent(modifier, packetEvent, user);
                }
            }

            if (replaced != null) {
//                if (legacy) {
                    componentModifier.write(0, WrappedChatComponent.fromJson(replaced));
//                } else {
//                    PlayerChatHelper.setChatMessageByHolder(componentHolder, WrappedChatComponent.fromJson(replaced));
//
//                    Object typeSub = PlayerChatHelper.getChatMessageTypeSub(packet.getModifier());
//
//                    WrappedChatComponent wrapped = PlayerChatHelper.getDisplayNameWrapped(typeSub);
//                    if (wrapped != null) {
//                        replaced = getReplacedJson(packetEvent, user, listenType, wrapped.getJson(), filter);
//                        PlayerChatHelper.setDisplayName(typeSub, WrappedChatComponent.fromJson(replaced));
//                    }
//                    wrapped = PlayerChatHelper.getTeamNameWrapped(typeSub);
//                    if (wrapped != null) {
//                        replaced = getReplacedJson(packetEvent, user, listenType, wrapped.getJson(), filter);
//                        PlayerChatHelper.setTeamName(typeSub, WrappedChatComponent.fromJson(replaced));
//                    }
//                }
            }

        }
    }

    private boolean convert(PacketContainer packet, PsrUser user) {
        if (!ProtocolStringReplacer.getInstance().getConfigManager().convertPlayerChat) {
            return false;
        }
        BaseComponent message;

        StructureModifier<WrappedChatComponent> componentModifier;
        StructureModifier<Object> modifier = packet.getModifier();
        WrappedChatComponent wrappedChatComponent;

        Object chatMessageTypeSubOrChatSender;
        PlayerChatHelper.ChatType chatType;

        if (legacy) {
            // 1.19
            componentModifier = packet.getChatComponents();
            wrappedChatComponent = componentModifier.read(0);

            chatMessageTypeSubOrChatSender = PlayerChatHelper.getChatSender(modifier);
            chatType = PlayerChatHelper.getChatTypeFromId(packet.getIntegers().read(0));
        } else {
            // 1.19.1+
            wrappedChatComponent = PlayerChatHelper.getChatMessage(packet.getModifier()
                    .withType(PlayerChatHelper.getPlayerChatMessageClass()).read(0));

            chatMessageTypeSubOrChatSender = PlayerChatHelper.getChatMessageTypeSub(modifier);
            chatType = PlayerChatHelper.getChatTypeFromId(PlayerChatHelper.getChatTypeId(chatMessageTypeSubOrChatSender));
        }

        if (wrappedChatComponent != null) {
            message = ComponentSerializer.parse(wrappedChatComponent.getJson())[0];
        } else {
            BaseComponent[] spigotComponent = getSpigotComponent(modifier);
            if (spigotComponent != null) {
                message = spigotComponent[0];
            } else {
                message = ComponentSerializer.parse(PaperUtils.getPaperGsonComponentSerializer().serialize(getPaperComponent(modifier)))[0];
            }
        }

        switch (chatType) {
            case PLAYER_CHAT:
                BaseComponent displayName = PlayerChatHelper.getDisplayName(chatMessageTypeSubOrChatSender);
                TranslatableComponent component = new TranslatableComponent("chat.type.text", displayName, message);
                user.sendMessage(component);
                break;
            case SYSTEM_CHAT:
            case TELLRAW:
                user.sendMessage(message);
                break;
            case GAME_INFO:
                user.sendActionBar(message);
                break;
            case SAY:
                displayName = PlayerChatHelper.getDisplayName(chatMessageTypeSubOrChatSender);
                component = new TranslatableComponent("chat.type.announcement", displayName, message);
                user.sendMessage(component);
                break;
            case MSG_INCOMING:
                displayName = PlayerChatHelper.getDisplayName(chatMessageTypeSubOrChatSender);
                component = new TranslatableComponent("commands.message.display.incoming", displayName, message);
                component.setItalic(true);
                component.setColor(ChatColor.GRAY);
                user.sendMessage(component);
                break;
            case MSG_OUTGOING:
                displayName = PlayerChatHelper.getDisplayName(chatMessageTypeSubOrChatSender);
                component = new TranslatableComponent("commands.message.display.outgoing", displayName, message);
                component.setItalic(true);
                component.setColor(ChatColor.GRAY);
                user.sendMessage(component);
                break;
            case EMOTE:
                displayName = PlayerChatHelper.getDisplayName(chatMessageTypeSubOrChatSender);
                component = new TranslatableComponent("chat.type.emote", displayName, message);
                user.sendMessage(component);
                break;
            case TEAM_MSG_INCOMING:
                displayName = PlayerChatHelper.getDisplayName(chatMessageTypeSubOrChatSender);
                BaseComponent teamName = PlayerChatHelper.getTeamName(chatMessageTypeSubOrChatSender);
                component = new TranslatableComponent("chat.type.team.text", teamName, displayName, message);
                user.sendMessage(component);
                break;
            case TEAM_MSG_OUTGOING:
                displayName = PlayerChatHelper.getDisplayName(chatMessageTypeSubOrChatSender);
                teamName = PlayerChatHelper.getTeamName(chatMessageTypeSubOrChatSender);
                component = new TranslatableComponent("chat.type.team.sent", teamName, displayName, message);
                user.sendMessage(component);
                break;
            default:
                throw new AssertionError();
        }
        return true;
    }

}
