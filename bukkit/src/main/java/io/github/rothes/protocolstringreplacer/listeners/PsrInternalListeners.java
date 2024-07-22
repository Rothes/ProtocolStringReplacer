package io.github.rothes.protocolstringreplacer.listeners;

import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.api.user.PsrUserManager;
import io.github.rothes.protocolstringreplacer.events.PsrReloadEvent;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PsrInternalListeners implements Listener {

    private int maxRecords;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPsrReload(PsrReloadEvent e) {
        if (e.getReloadState() == PsrReloadEvent.ReloadState.BEFORE) {
            maxRecords = ProtocolStringReplacer.getInstance().getConfigManager().maxCaptureRecords;
        } else if (e.getReloadState() == PsrReloadEvent.ReloadState.FINISH
                // maxCaptureRecords changed, reset user capture status.
                && maxRecords != ProtocolStringReplacer.getInstance().getConfigManager().maxCaptureRecords) {
            PsrUserManager userManager = ProtocolStringReplacer.getInstance().getUserManager();
            for (Player player : Bukkit.getOnlinePlayers()) {
                PsrUser user = userManager.getUser(player);
                for (ListenType value : ListenType.values()) {
                    user.removeCaptureType(value);
                }
            }
        }
    }

}
