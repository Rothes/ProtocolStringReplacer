package me.rothes.protocolstringreplacer.packetlisteners.client.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
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
        if (user.hasPermissionOrOp("protocolstringreplacer.feature.usermetacache.noncreative")) {
            ItemStack itemStack = packetEvent.getPacket().getItemModifier().read(0);
            restoreItem(user, itemStack);
        }
    }

}
