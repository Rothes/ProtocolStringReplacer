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
import io.github.rothes.protocolstringreplacer.packetlistener.server.chat.DisguisedChatPost21;
import io.github.rothes.protocolstringreplacer.packetlistener.server.chat.SystemChat;
import io.github.rothes.protocolstringreplacer.packetlistener.server.EntityMetadata;
import io.github.rothes.protocolstringreplacer.packetlistener.server.OpenWindow;
import io.github.rothes.protocolstringreplacer.packetlistener.server.bossbar.BossBarPost17;
import io.github.rothes.protocolstringreplacer.packetlistener.server.chat.TabComplete;
import io.github.rothes.protocolstringreplacer.packetlistener.server.chat.TabCompletePost20_5;
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
        List<Class<? extends BasePacketListener>> listeners = new ArrayList<>();
        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 17) {
            listeners.add(SetTitleText.class);
            listeners.add(SetSubtitleText.class);
            listeners.add(SetActionBar.class);

            listeners.add(PlayerCombatKill.class);
        } else {
            listeners.add(Title.class);
            listeners.add(TitleActionBar.class);

            listeners.add(CombatEvent.class);
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 11) {
            listeners.add(WindowItemsPost11.class);
        } else {
            listeners.add(WindowItems.class);
        }
        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 14) {
            listeners.add(MerchantTradeList.class);
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 18) {
            listeners.add(MapChunkPost18.class);
            listeners.add(TileEntityDataPost18.class);
        } else if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 10) {
            listeners.add(MapChunk.class);
            listeners.add(TileEntityData.class);
        } else {
            listeners.add(UpdateSign.class);
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 17) {
            listeners.add(BossBarPost17.class);
        } else if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 9) {
            listeners.add(BossBar.class);
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 19) {
            listeners.add(SystemChat.class);
            listeners.add(SystemChatActionBar.class);
            if (ProtocolStringReplacer.getInstance().getServerMajorVersion() != 19 || ProtocolStringReplacer.getInstance().getServerMinorVersion() >= 3) {
                if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 21) {
                    listeners.add(DisguisedChatPost21.class);
                } else {
                    listeners.add(DisguisedChat.class);
                }
            }
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() == 19 && ProtocolStringReplacer.getInstance().getServerMinorVersion() <= 2) {
            listeners.add(ChatPreview.class);
        }

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 17) {
            listeners.add(UpdateTeamPost17.class);
        } else if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 13) {
            listeners.add(UpdateTeamPost13.class);
        } else {
            listeners.add(UpdateTeam.class);
        }
        listeners.add(UpdateScore.class);
        listeners.add(ScoreBoardObjective.class);

        listeners.add(KickDisconnect.class);
        if ((ProtocolStringReplacer.getInstance().getServerMajorVersion() == 20 && ProtocolStringReplacer.getInstance().getServerMinorVersion() >= 5)
                || ProtocolStringReplacer.getInstance().getServerMajorVersion() > 20) {
            listeners.add(TabCompletePost20_5.class);
        } else {
            listeners.add(TabComplete.class);
        }
        listeners.add(Chat.class);
        listeners.add(ChatActionBar.class);
        listeners.add(SetSlot.class);
        listeners.add(OpenWindow.class);
        listeners.add(EntityMetadata.class);


        listeners.add(WindowClick.class);
        listeners.add(SetCreativeSlot.class);
        listeners.add(CloseWindow.class);
        if ((ProtocolStringReplacer.getInstance().getServerMajorVersion() == 20 && ProtocolStringReplacer.getInstance().getServerMinorVersion() >= 2)
                || ProtocolStringReplacer.getInstance().getServerMajorVersion() > 20) {
            listeners.add(SettingsLocaleUpper20.class);
        } else {
            listeners.add(SettingsLocale.class);
        }

        for (Class<? extends BasePacketListener> listener : listeners) {
            try {
                BasePacketListener packetListener = listener.getConstructor().newInstance();
                packetListener.register();
            } catch (Throwable throwable) {
                ProtocolStringReplacer.error("Unable to register listener " + listener.getSimpleName() + ":", throwable);
            }
        }

    }

    public void removeListeners() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(ProtocolStringReplacer.getInstance());
    }

}
