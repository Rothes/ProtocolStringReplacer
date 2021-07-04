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
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.ItemStack.WindowItems_11;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.OpenWindow;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.SetSubtitleText;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.SetTitleText;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.Sign.MapChunk;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.Sign.TileEntityData;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.Sign.UpdateSign;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.Title;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import org.bukkit.NamespacedKey;

public class PacketListenerManager {

    private ProtocolManager protocolManager;
    private NamespacedKey userCacheKey;

    public NamespacedKey getUserCacheKey() {
        return userCacheKey;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public void initialize() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 13) {
            userCacheKey = new NamespacedKey(ProtocolStringReplacer.getInstance(), "psr_user_cache_key");
        }
        addListeners();
    }

    public void addListeners() {
        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 17) {
            protocolManager.addPacketListener(new SetTitleText().packetAdapter);
            protocolManager.addPacketListener(new SetSubtitleText().packetAdapter);
        } else {
            protocolManager.addPacketListener(new Title().packetAdapter);
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 11) {
            protocolManager.addPacketListener(new WindowItems_11().packetAdapter);
        } else {
            protocolManager.addPacketListener(new WindowItems().packetAdapter);
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 10) {
            protocolManager.addPacketListener(new MapChunk().packetAdapter);
            protocolManager.addPacketListener(new TileEntityData().packetAdapter);
        } else {
            protocolManager.addPacketListener(new UpdateSign().packetAdapter);
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 8) {
            protocolManager.addPacketListener(new BossBar().packetAdapter);
        }

        protocolManager.addPacketListener(new Chat().packetAdapter);
        protocolManager.addPacketListener(new SetSlot().packetAdapter);
        protocolManager.addPacketListener(new OpenWindow().packetAdapter);
        protocolManager.addPacketListener(new EntityMetadata().packetAdapter);


        protocolManager.addPacketListener(new WindowClick().packetAdapter);
        protocolManager.addPacketListener(new SetCreativeSlot().packetAdapter);
    }

    public void removeListeners() {
        protocolManager.removePacketListeners(ProtocolStringReplacer.getInstance());
    }

}
