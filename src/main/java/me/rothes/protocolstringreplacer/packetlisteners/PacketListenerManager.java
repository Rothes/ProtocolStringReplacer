package me.rothes.protocolstringreplacer.packetlisteners;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.packetlisteners.client.CloseWindow;
import me.rothes.protocolstringreplacer.packetlisteners.client.itemstack.SetCreativeSlot;
import me.rothes.protocolstringreplacer.packetlisteners.client.itemstack.WindowClick;
import me.rothes.protocolstringreplacer.packetlisteners.server.ActionBar;
import me.rothes.protocolstringreplacer.packetlisteners.server.bossbar.BossBar;
import me.rothes.protocolstringreplacer.packetlisteners.server.Chat;
import me.rothes.protocolstringreplacer.packetlisteners.server.EntityMetadata;
import me.rothes.protocolstringreplacer.packetlisteners.server.OpenWindow;
import me.rothes.protocolstringreplacer.packetlisteners.server.bossbar.BossBarUpper17;
import me.rothes.protocolstringreplacer.packetlisteners.server.itemstack.MerchantTradeList;
import me.rothes.protocolstringreplacer.packetlisteners.server.sign.MapChunkUpper18;
import me.rothes.protocolstringreplacer.packetlisteners.server.sign.TileEntityDataUpper18;
import me.rothes.protocolstringreplacer.packetlisteners.server.title.SetSubtitleText;
import me.rothes.protocolstringreplacer.packetlisteners.server.title.SetTitleText;
import me.rothes.protocolstringreplacer.packetlisteners.server.title.Title;
import me.rothes.protocolstringreplacer.packetlisteners.server.scoreboard.ScoreBoardObjective;
import me.rothes.protocolstringreplacer.packetlisteners.server.scoreboard.UpdateScore;
import me.rothes.protocolstringreplacer.packetlisteners.server.sign.MapChunk;
import me.rothes.protocolstringreplacer.packetlisteners.server.sign.TileEntityData;
import me.rothes.protocolstringreplacer.packetlisteners.server.sign.UpdateSign;
import me.rothes.protocolstringreplacer.packetlisteners.server.itemstack.SetSlot;
import me.rothes.protocolstringreplacer.packetlisteners.server.itemstack.WindowItems;
import me.rothes.protocolstringreplacer.packetlisteners.server.itemstack.WindowItemsUpper12;
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
            userCacheKey = new NamespacedKey(ProtocolStringReplacer.getInstance(), "user_cache_key");
        }
        addListeners();
    }

    public void addListeners() {
        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 17) {
            protocolManager.addPacketListener(new SetTitleText().packetAdapter);
            protocolManager.addPacketListener(new SetSubtitleText().packetAdapter);
            protocolManager.addPacketListener(new ActionBar().packetAdapter);
        } else {
            protocolManager.addPacketListener(new Title().packetAdapter);
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 11) {
            protocolManager.addPacketListener(new WindowItemsUpper12().packetAdapter);
        } else {
            protocolManager.addPacketListener(new WindowItems().packetAdapter);
        }
        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 14) {
            protocolManager.addPacketListener(new MerchantTradeList().packetAdapter);
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 18) {
            protocolManager.addPacketListener(new MapChunkUpper18().packetAdapter);
            protocolManager.addPacketListener(new TileEntityDataUpper18().packetAdapter);
        } else if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 10) {
            protocolManager.addPacketListener(new MapChunk().packetAdapter);
            protocolManager.addPacketListener(new TileEntityData().packetAdapter);
        } else {
            protocolManager.addPacketListener(new UpdateSign().packetAdapter);
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 17) {
            protocolManager.addPacketListener(new BossBarUpper17().packetAdapter);
        } else if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 9) {
            protocolManager.addPacketListener(new BossBar().packetAdapter);
        }

        protocolManager.addPacketListener(new Chat().packetAdapter);
        protocolManager.addPacketListener(new SetSlot().packetAdapter);
        protocolManager.addPacketListener(new OpenWindow().packetAdapter);
        protocolManager.addPacketListener(new EntityMetadata().packetAdapter);
        protocolManager.addPacketListener(new UpdateScore().packetAdapter);
        protocolManager.addPacketListener(new ScoreBoardObjective().packetAdapter);


        protocolManager.addPacketListener(new WindowClick().packetAdapter);
        protocolManager.addPacketListener(new SetCreativeSlot().packetAdapter);
        protocolManager.addPacketListener(new CloseWindow().packetAdapter);
    }

    public void removeListeners() {
        protocolManager.removePacketListeners(ProtocolStringReplacer.getInstance());
    }

}
