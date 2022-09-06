package me.rothes.protocolstringreplacer.listeners;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ProtocolStringReplacer.getInstance().getUserManager().unloadUser(player);
    }

}
