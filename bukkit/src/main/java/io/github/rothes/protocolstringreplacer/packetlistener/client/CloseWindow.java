package io.github.rothes.protocolstringreplacer.packetlistener.client;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.client.itemstack.BaseClientItemPacketListener;
import io.github.rothes.protocolstringreplacer.util.scheduler.PsrScheduler;

public class CloseWindow extends BaseClientItemPacketListener {

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
            PsrScheduler.runTaskAsynchronously(() -> user.getPlayer().updateInventory());
        }
    }

}
