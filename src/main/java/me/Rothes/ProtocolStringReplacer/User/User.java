package me.Rothes.ProtocolStringReplacer.User;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.Replacer.ReplacerConfig;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class User {

    private UUID uuid;
    private Player player;
    private CommandSender sender;

    private HashMap<Short, ItemMeta> metaCache = new HashMap<>();
    private String currentWindowTitle;
    private Short uniqueCacheKey = 0;

    private String[] commandToConfirm;
    private Long confirmTime;

    private ReplacerConfig editorReplacerConfig;
    private Integer editorIndex;
    private String editorPattern;
    private String editorReplacement;

    public User(Player player) {
        this.player = player;
        uuid = player.getUniqueId();
        sender = player;
    }

    public User(UUID uuid) {
        this.uuid = uuid;
        player = Bukkit.getPlayer(uuid);
        sender = player;
    }

    public User(CommandSender sender) {
        this.sender = sender;
        if (sender instanceof Player) {
            this.player = (Player) sender;
            uuid = player.getUniqueId();
        }
    }

    public CommandSender getSender() {
        return sender;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isOnline() {
        return player != null && player.isOnline();
    }

    public boolean hasPermission(String permission) {
        return sender.isOp() || sender.hasPermission(permission);
    }

    public boolean hasPermission(Permission permission) {
        return (isOnline() && player.hasPermission(permission));
    }

    public HashMap<Short, ItemMeta> getMetaCache() {
        return metaCache;
    }

    public Short nextUniqueCacheKey() {
        return ++uniqueCacheKey;
    }

    public String getCurrentWindowTitle() {
        return currentWindowTitle;
    }

    public void setCurrentWindowTitle(String title) {
        currentWindowTitle = title;
    }

    public void setEditorReplacerConfig(ReplacerConfig editorReplacerConfig) {
        this.editorReplacerConfig = editorReplacerConfig;
    }

    public void setCommandToConfirm(String[] args) {
        commandToConfirm = args;
        confirmTime = System.currentTimeMillis();
    }

    public boolean isConfirmed(@Nonnull String[] args) {
        Validate.notNull(args, "Arguments cannot be null");
        return Arrays.equals(args, commandToConfirm);
    }

    public String[] getCommandToConfirm() {
        return commandToConfirm;
    }

    public boolean hasCommandToConfirm() {
        return commandToConfirm != null;
    }

    public void clearCommandToConfirm() {
        commandToConfirm = null;
        confirmTime = null;
    }

    public boolean isConfirmExpired() {
        return confirmTime != null && System.currentTimeMillis() - confirmTime > 15000;
    }

    public void sendFilteredMessage(BaseComponent... baseComponents) {
        if (sender instanceof ConsoleCommandSender) {
            sender.spigot().sendMessage(baseComponents);
        } else {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.CHAT);
            packet.getModifier().write(2, baseComponents);
            packet.getChatTypes().write(0, EnumWrappers.ChatType.SYSTEM);
            try {
                ProtocolStringReplacer.getInstance().getPacketListenerManager().getProtocolManager().
                        sendServerPacket(player, packet, true);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendFilteredMessage(String json) {
        sendFilteredMessage(ComponentSerializer.parse(json));
    }

    public void sendFilteredText(String text) {
        sendFilteredMessage(TextComponent.fromLegacyText(text));
    }

    public void sendMessage(String text) {
        sender.sendMessage(text);
    }

    @Override
    public String toString() {
        return "User{" +
                "uuid=" + uuid +
                ", player=" + player +
                ", currentWindowTitle='" + currentWindowTitle + '\'' +
                ", metaCache=" + metaCache +
                ", uniqueCacheKey=" + uniqueCacheKey +
                ", editorReplacerConfig=" + editorReplacerConfig +
                ", editorIndex=" + editorIndex +
                ", editorPattern='" + editorPattern + '\'' +
                ", editorReplacement='" + editorReplacement + '\'' +
                '}';
    }

}
