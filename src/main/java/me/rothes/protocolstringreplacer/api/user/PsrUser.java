package me.rothes.protocolstringreplacer.api.user;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.ComponentConverter;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.capture.CaptureInfo;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.FileReplacerConfig;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PsrUser {

    private UUID uuid;
    private Player player;
    private CommandSender sender;

    private HashMap<Short, ItemMeta> metaCache = new HashMap<>();
    private String currentWindowTitle;
    private boolean inAnvil;
    private boolean inMerchant;
    private Short uniqueCacheKey = 0;

    private String[] commandToConfirm;
    private Long confirmTime;

    private Set<ListenType> captureTypes = new HashSet<>();
    private HashMap<ListenType, ArrayList<CaptureInfo>> captures = new HashMap<>();

    private FileReplacerConfig editorReplacerConfig;
//TODO:    private Integer editorIndex;
//TODO:    private String editorPattern;
//TODO:    private String editorReplacement;

    public PsrUser(Player player) {
        this.player = player;
        uuid = player.getUniqueId();
        sender = player;
    }

    public PsrUser(UUID uuid) {
        this.uuid = uuid;
        player = Bukkit.getPlayer(uuid);
        sender = player;
    }

    public PsrUser(CommandSender sender) {
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

    public HashMap<Short, ItemMeta> getMetaCache() {
        return metaCache;
    }

    public Short nextUniqueCacheKey() {
        return ++uniqueCacheKey;
    }

    public String getCurrentWindowTitle() {
        return currentWindowTitle;
    }

    public boolean isInAnvil() {
        return inAnvil;
    }

    public boolean isInMerchant() {
        return inMerchant;
    }

    public String[] getCommandToConfirm() {
        return commandToConfirm;
    }

    public FileReplacerConfig getEditorReplacerConfig() {
        return editorReplacerConfig;
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

    public void setCurrentWindowTitle(String title) {
        currentWindowTitle = title;
    }

    public void setInAnvil(boolean inAnvil) {
        this.inAnvil = inAnvil;
    }

    public void setInMerchant(boolean inMerchant) {
        this.inMerchant = inMerchant;
    }

    public void addCaptureType(ListenType listenType) {
        captureTypes.add(listenType);
        captures.put(listenType, new ArrayList<>());
    }

    public void removeCaptureType(ListenType listenType) {
        captureTypes.remove(listenType);
        captures.remove(listenType);
    }

    public boolean isCapturing(ListenType listenType) {
        return captureTypes.contains(listenType);
    }

    public void addCaptureInfo(ListenType listenType, CaptureInfo info) {
        captures.get(listenType).add(info);
    }

    public List<CaptureInfo> getCaptureInfos(ListenType listenType) {
        return captures.get(listenType);
    }

    public void setEditorReplacerConfig(FileReplacerConfig editorReplacerConfig) {
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
    /*
    public void setCapturePacket(PacketType packetType, boolean capture) {
        if (capture) {
            capturePackets.add(packetType);
        } else {
            capturePackets.remove(packetType);
        }
    }

    public void openFilteredWindow(int windowId, int windowType, String title) {
        if (isOnline()) {
            PacketContainer packet = new PacketContainer(PacketType.Play.server.OPEN_WINDOW);
            packet.getIntegers().write(0, windowId);
            packet.getIntegers().write(1, windowType);
            packet.getChatComponents().write(0, WrappedChatComponent.fromLegacyText(title));
            packet.setMeta("psr_filtered_packet", true);
            try {
                ProtocolStringReplacer.getInstance().getPacketListenerManager().getProtocolManager().
                        sendServerPacket(player, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void setFiliteredWindowItems(int windowId, itemstack[] itemStacks) {
        if (isOnline()) {
            PacketContainer packet = new PacketContainer(PacketType.Play.server.WINDOW_ITEMS);
            packet.getIntegers().write(0, windowId);
            packet.getIntegers().write(1, itemStacks.length);
            packet.getItemArrayModifier().write(0, itemStacks);
            packet.setMeta("psr_filtered_packet", true);
            try {
                ProtocolStringReplacer.getInstance().getPacketListenerManager().getProtocolManager().
                        sendServerPacket(player, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }*/

    public void sendFilteredMessage(BaseComponent... baseComponents) {
        if (sender instanceof ConsoleCommandSender) {
            if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 12) {
                sender.spigot().sendMessage(baseComponents);
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                for (BaseComponent component : baseComponents) {
                    stringBuilder.append(component.toLegacyText());
                }
                sender.sendMessage(stringBuilder.toString());
            }
        } else {
            PacketContainer packet;
            if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 19) {
                packet = new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT);
                packet.getStrings().write(0, ComponentSerializer.toString(baseComponents));
                StructureModifier<Boolean> booleans = packet.getBooleans();
                if (booleans.size() >= 1) {
                    // 1.19 only
                    booleans.write(0, false);
                } else {
                    // 1.19.1+
                    packet.getIntegers().write(0, (int) EnumWrappers.ChatType.SYSTEM.getId());
                }
            } else {
                packet = new PacketContainer(PacketType.Play.Server.CHAT);
                packet.getChatComponents().write(0, ComponentConverter.fromBaseComponent(baseComponents));
                packet.getChatTypes().write(0, EnumWrappers.ChatType.SYSTEM);
            }
            packet.setMeta("psr_filtered_packet", true);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        }
    }

    public void sendFilteredMessage(String json) {
        sendFilteredMessage(ComponentSerializer.parse(json));
    }

    public void sendFilteredText(String text) {
        sendFilteredMessage(TextComponent.fromLegacyText(text));
    }

    public void sendMessage(BaseComponent... components) {
        sender.spigot().sendMessage(components);
    }

    public void sendActionBar(BaseComponent... components) {
        ((Player) sender).spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
    }

    public void sendMessage(String text) {
        sender.sendMessage(text);
    }


    public void cleanUserMetaCache() {
        getMetaCache().clear();
        uniqueCacheKey = 0;
    }

    public void saveUserMetaCache(ItemStack originalItem, ItemStack replacedItem) {
        if (this.hasPermission("protocolstringreplacer.feature.usermetacache") && originalItem.hasItemMeta()) {
            if (ProtocolStringReplacer.getInstance().getConfigManager().removeCacheWhenMerchantTrade && isInMerchant()) {
                return;
            }
            ItemMeta originalMeta = originalItem.getItemMeta();
            if (!originalMeta.equals(replacedItem.getItemMeta())) {
                NBTItem nbtItem = new NBTItem(replacedItem);
                nbtItem.addCompound("ProtocolStringReplacer").setShort("UserMetaCacheKey", nextUniqueCacheKey());
                replacedItem.setItemMeta(nbtItem.getItem().getItemMeta());
                this.getMetaCache().put(uniqueCacheKey, originalMeta);
            }
        }
    }

    @Override
    public String toString() {
        return "user{" +
                "uuid=" + uuid +
                ", player=" + player +
                ", currentWindowTitle='" + currentWindowTitle + '\'' +
                ", metaCache=" + metaCache +
                ", uniqueCacheKey=" + uniqueCacheKey +
                ", editorReplacerConfig=" + editorReplacerConfig +
                '}';
    }

}
