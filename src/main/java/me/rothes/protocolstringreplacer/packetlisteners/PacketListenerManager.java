package me.rothes.protocolstringreplacer.packetlisteners;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.PsrLocalization;
import me.rothes.protocolstringreplacer.packetlisteners.client.CloseWindow;
import me.rothes.protocolstringreplacer.packetlisteners.client.SettingsLocale;
import me.rothes.protocolstringreplacer.packetlisteners.client.itemstack.SetCreativeSlot;
import me.rothes.protocolstringreplacer.packetlisteners.client.itemstack.WindowClick;
import me.rothes.protocolstringreplacer.packetlisteners.server.ActionBar;
import me.rothes.protocolstringreplacer.packetlisteners.server.KickDisconnect;
import me.rothes.protocolstringreplacer.packetlisteners.server.chat.ChatPreview;
import me.rothes.protocolstringreplacer.packetlisteners.server.chat.SystemChat;
import me.rothes.protocolstringreplacer.packetlisteners.server.bossbar.BossBar;
import me.rothes.protocolstringreplacer.packetlisteners.server.chat.Chat;
import me.rothes.protocolstringreplacer.packetlisteners.server.EntityMetadata;
import me.rothes.protocolstringreplacer.packetlisteners.server.OpenWindow;
import me.rothes.protocolstringreplacer.packetlisteners.server.bossbar.BossBarPost17;
import me.rothes.protocolstringreplacer.packetlisteners.server.chat.TabComplete;
import me.rothes.protocolstringreplacer.packetlisteners.server.combat.CombatEvent;
import me.rothes.protocolstringreplacer.packetlisteners.server.combat.PlayerCombatKill;
import me.rothes.protocolstringreplacer.packetlisteners.server.itemstack.MerchantTradeList;
import me.rothes.protocolstringreplacer.packetlisteners.server.scoreboard.UpdateTeam;
import me.rothes.protocolstringreplacer.packetlisteners.server.scoreboard.UpdateTeamPost13;
import me.rothes.protocolstringreplacer.packetlisteners.server.scoreboard.UpdateTeamPost17;
import me.rothes.protocolstringreplacer.packetlisteners.server.sign.MapChunkPost18;
import me.rothes.protocolstringreplacer.packetlisteners.server.sign.TileEntityDataPost18;
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
import me.rothes.protocolstringreplacer.packetlisteners.server.itemstack.WindowItemsPost11;

import java.util.ArrayList;
import java.util.List;

public class PacketListenerManager {

    private ListenerPriority listenerPriority;

    public ListenerPriority getListenerPriority() {
        return listenerPriority;
    }

    public void initialize() {
        listenerPriority = null;
        for (ListenerPriority value : ListenerPriority.values()) {
            if (value.name().equalsIgnoreCase(ProtocolStringReplacer.getInstance().getConfigManager().listenerPriority)) {
                listenerPriority = value;
                break;
            }
        }
        if (listenerPriority == null) {
            ProtocolStringReplacer.error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Config.Invalid-Listener-Priority"));
            listenerPriority = ListenerPriority.HIGHEST;
        }

        addListeners();
    }

    public void addListeners() {
        List<AbstractPacketListener> listeners = new ArrayList<>();
        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 17) {
            listeners.add(new SetTitleText());
            listeners.add(new SetSubtitleText());
            listeners.add(new ActionBar());

            listeners.add(new PlayerCombatKill());
        } else {
            listeners.add(new Title());

            listeners.add(new CombatEvent());
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 11) {
            listeners.add(new WindowItemsPost11());
        } else {
            listeners.add(new WindowItems());
        }
        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 14) {
            listeners.add(new MerchantTradeList());
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 18) {
            listeners.add(new MapChunkPost18());
            listeners.add(new TileEntityDataPost18());
        } else if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 10) {
            listeners.add(new MapChunk());
            listeners.add(new TileEntityData());
        } else {
            listeners.add(new UpdateSign());
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 17) {
            listeners.add(new BossBarPost17());
        } else if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 9) {
            listeners.add(new BossBar());
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 19) {
            listeners.add(new SystemChat());
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() == 19 && ProtocolStringReplacer.getInstance().getServerMinorVersion() <= 2) {
            listeners.add(new ChatPreview());
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 17) {
            listeners.add(new UpdateTeamPost17());
        } else if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 13) {
            listeners.add(new UpdateTeamPost13());
        } else {
            listeners.add(new UpdateTeam());
        }
        listeners.add(new UpdateScore());
        listeners.add(new ScoreBoardObjective());

        listeners.add(new KickDisconnect());
        listeners.add(new TabComplete());
        listeners.add(new Chat());
        listeners.add(new SetSlot());
        listeners.add(new OpenWindow());
        listeners.add(new EntityMetadata());


        listeners.add(new WindowClick());
        listeners.add(new SetCreativeSlot());
        listeners.add(new CloseWindow());
        listeners.add(new SettingsLocale());

        for (AbstractPacketListener listener : listeners) {
            try {
                listener.register();
            } catch (Throwable throwable) {
                ProtocolStringReplacer.error("Unable to register listener " + listener.getClass().getSimpleName() + ":", throwable);
            }
        }

    }

    public void removeListeners() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(ProtocolStringReplacer.getInstance());
    }

}
