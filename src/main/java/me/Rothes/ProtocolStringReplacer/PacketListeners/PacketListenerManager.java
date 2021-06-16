package me.Rothes.ProtocolStringReplacer.PacketListeners;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Client.ItemStack.WindowClick;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Client.ItemStack.SetCreativeSlot;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.BossBar;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.Chat;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.EntityMetadata;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.ItemStack.SetSlot;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.ItemStack.WindowItems;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.OpenWindow;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.SetSubtitleText;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.SetTitleText;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.Sign.MapChunk;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.Sign.TileEntityData;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.Title;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

public class PacketListenerManager {

    private ProtocolManager protocolManager;
    private NamespacedKey userCacheKey;

    public NamespacedKey getUserCacheKey() {
        return userCacheKey;
    }

    public void initialize() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        userCacheKey = new NamespacedKey(ProtocolStringReplacer.getInstance(), "psr_user_cache_key");
        addListeners();
    }

    public void addListeners() {
        protocolManager.addPacketListener(new Chat().packetAdapter);
        protocolManager.addPacketListener(new SetSlot().packetAdapter);
        protocolManager.addPacketListener(new OpenWindow().packetAdapter);
        protocolManager.addPacketListener(new WindowItems().packetAdapter);
        protocolManager.addPacketListener(new EntityMetadata().packetAdapter);
        protocolManager.addPacketListener(new TileEntityData().packetAdapter);
        protocolManager.addPacketListener(new MapChunk().packetAdapter);
        protocolManager.addPacketListener(new BossBar().packetAdapter);
        if (Integer.parseInt(Bukkit.getServer().getBukkitVersion().split("\\.")[1].split("-")[0]) >= 17) {
            Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §3启用 Spigot 1.17+ 兼容.");
            protocolManager.addPacketListener(new SetTitleText().packetAdapter);
            protocolManager.addPacketListener(new SetSubtitleText().packetAdapter);
        } else {
            protocolManager.addPacketListener(new Title().packetAdapter);
        }

        protocolManager.addPacketListener(new WindowClick().packetAdapter);
        protocolManager.addPacketListener(new SetCreativeSlot().packetAdapter);
    }

    public void removeListeners() {
        protocolManager.removePacketListeners(ProtocolStringReplacer.getInstance());
    }

}
