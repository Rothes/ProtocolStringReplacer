package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.github.rothes.protocolstringreplacer.bukkit.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.client.CloseWindow;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.client.itemstack.SetCreativeSlot;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.client.itemstack.WindowClick;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.ActionBar;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.KickDisconnect;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.chat.ChatPreview;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.chat.SystemChat;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.bossbar.BossBar;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.chat.Chat;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.EntityMetadata;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.OpenWindow;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.bossbar.BossBarUpper17;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.chat.TabComplete;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.combat.CombatEvent;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.combat.PlayerCombatKill;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.itemstack.MerchantTradeList;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.sign.MapChunkUpper18;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.sign.TileEntityDataUpper18;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.title.SetSubtitleText;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.title.SetTitleText;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.title.Title;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.scoreboard.ScoreBoardObjective;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.scoreboard.UpdateScore;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.sign.MapChunk;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.sign.TileEntityData;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.sign.UpdateSign;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.itemstack.SetSlot;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.itemstack.WindowItems;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.itemstack.WindowItemsUpper12;

public class PacketListenerManager {

    private ProtocolManager protocolManager;

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public void initialize() {
        protocolManager = ProtocolLibrary.getProtocolManager();
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

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 17) {
            protocolManager.addPacketListener(new PlayerCombatKill().packetAdapter);
        } else {
            protocolManager.addPacketListener(new CombatEvent().packetAdapter);
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

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 19) {
            protocolManager.addPacketListener(new SystemChat().packetAdapter);
            protocolManager.addPacketListener(new ChatPreview().packetAdapter);
        }

        protocolManager.addPacketListener(new TabComplete().packetAdapter);
        protocolManager.addPacketListener(new Chat().packetAdapter);
        protocolManager.addPacketListener(new SetSlot().packetAdapter);
        protocolManager.addPacketListener(new OpenWindow().packetAdapter);
        protocolManager.addPacketListener(new EntityMetadata().packetAdapter);
        protocolManager.addPacketListener(new UpdateScore().packetAdapter);
        protocolManager.addPacketListener(new ScoreBoardObjective().packetAdapter);
        protocolManager.addPacketListener(new KickDisconnect().packetAdapter);


        protocolManager.addPacketListener(new WindowClick().packetAdapter);
        protocolManager.addPacketListener(new SetCreativeSlot().packetAdapter);
        protocolManager.addPacketListener(new CloseWindow().packetAdapter);

    }

    public void removeListeners() {
        protocolManager.removePacketListeners(ProtocolStringReplacer.getInstance());
    }

}
