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

            StructureModifier<WrappedChatComponent> componentModifier = packet.getChatComponents();
            WrappedChatComponent wrappedChatComponent = componentModifier.read(0);
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

        StructureModifier<WrappedChatComponent> componentModifier = packet.getChatComponents();
        StructureModifier<Object> modifier = packet.getModifier();
        WrappedChatComponent wrappedChatComponent = componentModifier.read(0);
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

        switch (packet.getIntegers().read(0)) {
            case 1:  // System message
            case 7:  // Command /tellraw
                user.sendMessage(message);
                break;
            case 2:  // Game Info (ActionBar)
                user.sendActionBar(message);
                break;
            case 0:  // Chat message
                Object chatSender = ConvertChatHelper.getChatSender(modifier);
                BaseComponent displayName = ConvertChatHelper.getDisplayName(chatSender);
                TranslatableComponent component = new TranslatableComponent("chat.type.text", displayName, message);
                user.sendMessage(component);
                break;
            case 3:  // Command /say
                chatSender = ConvertChatHelper.getChatSender(modifier);
                displayName = ConvertChatHelper.getDisplayName(chatSender);
                component = new TranslatableComponent("chat.type.announcement", displayName, message);
                user.sendMessage(component);
                break;
            case 4:  // Command /msg
                chatSender = ConvertChatHelper.getChatSender(modifier);
                displayName = ConvertChatHelper.getDisplayName(chatSender);
                component = new TranslatableComponent("commands.message.display.incoming", displayName, message);
                component.setItalic(true);
                component.setColor(ChatColor.GRAY);
                user.sendMessage(component);
                break;
            case 6:  // Command /me
                chatSender = ConvertChatHelper.getChatSender(modifier);
                displayName = ConvertChatHelper.getDisplayName(chatSender);
                component = new TranslatableComponent("chat.type.emote", displayName, message);
                user.sendMessage(component);
                break;
            case 5:  // Command /teammsg
                chatSender = ConvertChatHelper.getChatSender(modifier);
                displayName = ConvertChatHelper.getDisplayName(chatSender);
                BaseComponent teamName = ConvertChatHelper.getTeamName(chatSender);
                component = new TranslatableComponent("chat.type.team.text", teamName, displayName, message);
                user.sendMessage(component);
                break;
            default:
                ProtocolStringReplacer.warn("Not supported PlayerChat type when convert: " + packet.getIntegers().read(0));
                user.sendMessage(message);
                break;
        }
        return true;
    }

}
