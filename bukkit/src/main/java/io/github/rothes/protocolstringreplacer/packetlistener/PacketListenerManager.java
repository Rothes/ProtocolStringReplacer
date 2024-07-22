package io.github.rothes.protocolstringreplacer.packetlistener;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.PsrLocalization;
import io.github.rothes.protocolstringreplacer.packetlistener.server.bossbar.BossBar;
import io.github.rothes.protocolstringreplacer.packetlistener.server.chat.Chat;
import io.github.rothes.protocolstringreplacer.packetlistener.server.chat.ChatPreview;
import io.github.rothes.protocolstringreplacer.packetlistener.client.CloseWindow;
import io.github.rothes.protocolstringreplacer.packetlistener.client.SettingsLocale;
import io.github.rothes.protocolstringreplacer.packetlistener.client.SettingsLocaleUpper20;
import io.github.rothes.protocolstringreplacer.packetlistener.client.itemstack.SetCreativeSlot;
import io.github.rothes.protocolstringreplacer.packetlistener.client.itemstack.WindowClick;
import io.github.rothes.protocolstringreplacer.packetlistener.server.actionbar.ChatActionBar;
import io.github.rothes.protocolstringreplacer.packetlistener.server.actionbar.SetActionBar;
import io.github.rothes.protocolstringreplacer.packetlistener.server.KickDisconnect;
import io.github.rothes.protocolstringreplacer.packetlistener.server.actionbar.SystemChatActionBar;
import io.github.rothes.protocolstringreplacer.packetlistener.server.actionbar.TitleActionBar;
import io.github.rothes.protocolstringreplacer.packetlistener.server.chat.DisguisedChat;
import io.github.rothes.protocolstringreplacer.packetlistener.server.chat.SystemChat;
import io.github.rothes.protocolstringreplacer.packetlistener.server.EntityMetadata;
import io.github.rothes.protocolstringreplacer.packetlistener.server.OpenWindow;
import io.github.rothes.protocolstringreplacer.packetlistener.server.bossbar.BossBarPost17;
import io.github.rothes.protocolstringreplacer.packetlistener.server.chat.TabComplete;
import io.github.rothes.protocolstringreplacer.packetlistener.server.combat.CombatEvent;
import io.github.rothes.protocolstringreplacer.packetlistener.server.combat.PlayerCombatKill;
import io.github.rothes.protocolstringreplacer.packetlistener.server.itemstack.MerchantTradeList;
import io.github.rothes.protocolstringreplacer.packetlistener.server.scoreboard.UpdateTeam;
import io.github.rothes.protocolstringreplacer.packetlistener.server.scoreboard.UpdateTeamPost13;
import io.github.rothes.protocolstringreplacer.packetlistener.server.scoreboard.UpdateTeamPost17;
import io.github.rothes.protocolstringreplacer.packetlistener.server.sign.MapChunkPost18;
import io.github.rothes.protocolstringreplacer.packetlistener.server.sign.TileEntityDataPost18;
import io.github.rothes.protocolstringreplacer.packetlistener.server.title.SetSubtitleText;
import io.github.rothes.protocolstringreplacer.packetlistener.server.title.SetTitleText;
import io.github.rothes.protocolstringreplacer.packetlistener.server.title.Title;
import io.github.rothes.protocolstringreplacer.packetlistener.server.scoreboard.ScoreBoardObjective;
import io.github.rothes.protocolstringreplacer.packetlistener.server.scoreboard.UpdateScore;
import io.github.rothes.protocolstringreplacer.packetlistener.server.sign.MapChunk;
import io.github.rothes.protocolstringreplacer.packetlistener.server.sign.TileEntityData;
import io.github.rothes.protocolstringreplacer.packetlistener.server.sign.UpdateSign;
import io.github.rothes.protocolstringreplacer.packetlistener.server.itemstack.SetSlot;
import io.github.rothes.protocolstringreplacer.packetlistener.server.itemstack.WindowItems;
import io.github.rothes.protocolstringreplacer.packetlistener.server.itemstack.WindowItemsPost11;

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
            listeners.add(new SetActionBar());

            listeners.add(new PlayerCombatKill());
        } else {
            listeners.add(new Title());
            listeners.add(new TitleActionBar());

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
            listeners.add(new SystemChatActionBar());
            if (ProtocolStringReplacer.getInstance().getServerMajorVersion() != 19 || ProtocolStringReplacer.getInstance().getServerMinorVersion() >= 3) {
                listeners.add(new DisguisedChat());
            }
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
        listeners.add(new ChatActionBar());
        listeners.add(new SetSlot());
        listeners.add(new OpenWindow());
        listeners.add(new EntityMetadata());


        listeners.add(new WindowClick());
        listeners.add(new SetCreativeSlot());
        listeners.add(new CloseWindow());
        if ((ProtocolStringReplacer.getInstance().getServerMajorVersion() == 20 && ProtocolStringReplacer.getInstance().getServerMinorVersion() >= 2)
                || ProtocolStringReplacer.getInstance().getServerMajorVersion() > 20) {
            listeners.add(new SettingsLocaleUpper20());
        } else {
            listeners.add(new SettingsLocale());
        }

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
