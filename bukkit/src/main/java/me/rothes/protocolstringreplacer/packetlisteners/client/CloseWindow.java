package me.rothes.protocolstringreplacer.packetlisteners.client;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.packetlisteners.client.itemstack.AbstractClientItemPacketListener;
import org.bukkit.Bukkit;

public class CloseWindow extends AbstractClientItemPacketListener {

    public CloseWindow() {
        super(PacketType.Play.Client.CLOSE_WINDOW);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        user.setCurrentWindowTitle(null);
        user.setInAnvil(false);
        if (user.isInMerchant()) {
            user.setInMerchant(false);
            // Must be called in other threads or the inventory won't update.
            Bukkit.getScheduler().runTaskAsynchronously(ProtocolStringReplacer.getInstance(), () -> user.getPlayer().updateInventory());
        }
    }

}
