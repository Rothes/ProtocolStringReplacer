package me.Rothes.ProtocolStringReplacer.Listeners;

import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ProtocolStringReplacer.getUserManager().unloadUser(player);
    }

}
