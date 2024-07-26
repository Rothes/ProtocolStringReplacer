package io.github.rothes.protocolstringreplacer.packetlistener.server.chat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.nms.packetreader.ChatType;
import io.github.rothes.protocolstringreplacer.packetlistener.server.BaseServerComponentsPacketListener;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.util.PaperUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;

import java.util.Optional;

public final class Chat extends BaseServerComponentsPacketListener {

    public final PlayerChatHelper.Version version = PlayerChatHelper.version;

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

            if (packet.getChatTypes().read(0) == EnumWrappers.ChatType.GAME_INFO
                    || (packet.getBytes().size() >= 1 && packet.getBytes().read(0) == 2)) {
                // ActionBar Message.
                return;
            }

            if (version != PlayerChatHelper.Version.V8_0_TO_V18_2) {
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
        if (version == PlayerChatHelper.Version.V8_0_TO_V18_2) {
            return false;
        }
        BaseComponent message;

        StructureModifier<WrappedChatComponent> componentModifier;
        StructureModifier<Object> modifier = packet.getModifier();
        WrappedChatComponent wrappedChatComponent;

        Object chatMessageTypeSubOrChatSender;
        ChatType chatType = PlayerChatHelper.getChatType((ClientboundPlayerChatPacket) packet.getHandle());

        switch (version) {
            case V19_0:
                componentModifier = packet.getChatComponents();
                wrappedChatComponent = componentModifier.read(0);

                chatMessageTypeSubOrChatSender = PlayerChatHelper.getChatSender(modifier);
                break;
            case V19_1_TO_V19_2:
                wrappedChatComponent = PlayerChatHelper.getChatMessage(packet.getModifier()
                        .withType(PlayerChatHelper.getPlayerChatMessageClass()).read(0));

                chatMessageTypeSubOrChatSender = PlayerChatHelper.getChatMessageTypeSub(modifier);
                break;
            case V19_3:
            case V19_4:
                componentModifier = packet.getChatComponents();
                wrappedChatComponent = componentModifier.read(0);
                if (wrappedChatComponent == null) {

                    wrappedChatComponent = PlayerChatHelper.getChatMessageR3(packet.getModifier()
                            .withType(PlayerChatHelper.getMessageBodySubClass()).read(0));
                }

                chatMessageTypeSubOrChatSender = PlayerChatHelper.getChatMessageTypeSub(modifier);
                break;
            default:
                throw new AssertionError();
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
                user.sendMessage(message);
                break;
            case TELLRAW:
                // The Format is modified in AsyncPlayerChatEvent or PlayerChatEvent
                switch (version) {
                    case V19_0:
                    case V19_1_TO_V19_2:
                        user.sendMessage(ComponentSerializer.parse(PlayerChatHelper.getOptionalChatMessage(packet.getModifier()
                                .withType(PlayerChatHelper.getPlayerChatMessageClass()).read(0)).getJson()));
                        break;
                    default:
                        user.sendMessage(ComponentSerializer.parse(packet.getChatComponents().read(0).getJson()));
                        break;
                }
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
