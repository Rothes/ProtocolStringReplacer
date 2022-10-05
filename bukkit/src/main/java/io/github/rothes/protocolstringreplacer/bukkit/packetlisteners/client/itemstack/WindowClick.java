package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.client.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import org.bukkit.inventory.ItemStack;

public final class WindowClick extends AbstractClientItemPacketListener {

    public WindowClick() {
        super(PacketType.Play.Client.WINDOW_CLICK);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        if (user.hasPermission("protocolstringreplacer.feature.usermetacache.noncreative")) {
            ItemStack itemStack = packetEvent.getPacket().getItemModifier().read(0);
            restoreItem(user, itemStack);
        }
    }

}
