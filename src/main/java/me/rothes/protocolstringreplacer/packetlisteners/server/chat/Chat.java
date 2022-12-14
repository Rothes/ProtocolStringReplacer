package me.rothes.protocolstringreplacer.packetlisteners.server.chat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerComponentsPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.utils.PaperUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Optional;

public final class Chat extends AbstractServerComponentsPacketListener {

    private final Version version;

    public Chat() {
        super(PacketType.Play.Server.CHAT, ListenType.CHAT);
        byte serverMajorVersion = ProtocolStringReplacer.getInstance().getServerMajorVersion();
        byte serverMinorVersion = ProtocolStringReplacer.getInstance().getServerMinorVersion();
        if (serverMajorVersion <= 18) {
            version = Version.R8_0_TO_R18_2;
        } else if (serverMajorVersion == 19){
            if (serverMinorVersion == 0) {
                version = Version.R19_0;
            } else if (serverMinorVersion <= 2) {
                version = Version.R19_1_TO_R19_2;
            } else {
                version = Version.R19_3;
            }
        } else {
            version = Version.R19_3;
        }
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

            if (version != Version.R8_0_TO_R18_2) {
                // on 1.19+ we can no longer modify the message, they have added the final modifier.
                return;
            }

            StructureModifier<WrappedChatComponent> componentModifier = packet.getChatComponents();
            WrappedChatComponent wrappedChatComponent = componentModifier.read(0);
            String replaced;

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
                componentModifier.write(0, WrappedChatComponent.fromJson(replaced));
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

        switch (version) {
            case R19_0:
                componentModifier = packet.getChatComponents();
                wrappedChatComponent = componentModifier.read(0);

                chatMessageTypeSubOrChatSender = PlayerChatHelper.getChatSender(modifier);
                chatType = PlayerChatHelper.getChatTypeFromId(packet.getIntegers().read(0));
                break;
            case R19_1_TO_R19_2:
                wrappedChatComponent = PlayerChatHelper.getChatMessage(packet.getModifier()
                        .withType(PlayerChatHelper.getPlayerChatMessageClass()).read(0));

                chatMessageTypeSubOrChatSender = PlayerChatHelper.getChatMessageTypeSub(modifier);
                chatType = PlayerChatHelper.getChatTypeFromId(PlayerChatHelper.getChatTypeId(chatMessageTypeSubOrChatSender));
                break;
            case R19_3:
                componentModifier = packet.getChatComponents();
                wrappedChatComponent = componentModifier.read(0);
                if (wrappedChatComponent == null) {

                    wrappedChatComponent = PlayerChatHelper.getChatMessageR3(packet.getModifier()
                            .withType(PlayerChatHelper.getMessageBodySubClass()).read(0));
                }

                chatMessageTypeSubOrChatSender = PlayerChatHelper.getChatMessageTypeSub(modifier);
                chatType = PlayerChatHelper.getChatTypeFromId(PlayerChatHelper.getChatTypeId(chatMessageTypeSubOrChatSender));
                break;
            default:
                return false;
        }

        if (wrappedChatComponent != null) {
            // 1.19.1+
            message = ComponentSerializer.parse(wrappedChatComponent.getJson())[0];
        } else {
            // 1.19
            BaseComponent[] spigotComponent = getSpigotComponent(modifier);
            if (spigotComponent != null) {
                message = spigotComponent[0];
            } else {
                message = ComponentSerializer.parse(PaperUtils.serializeComponent(getPaperComponent(modifier)))[0];
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

    private enum Version {
        R8_0_TO_R18_2,
        R19_0,
        R19_1_TO_R19_2,
        R19_3
    }

}
