package io.github.rothes.protocolstringreplacer.packetlistener.client.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import org.bukkit.inventory.ItemStack;

public final class WindowClick extends BaseClientItemPacketListener {

    public WindowClick() {
        super(PacketType.Play.Client.WINDOW_CLICK);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        if (user.hasPermission("protocolstringreplacer.feature.usermetacache.noncreative")) {
            com.comphenix.protocol.reflect.StructureModifier<org.bukkit.inventory.ItemStack> itemModifier = packetEvent.getPacket().getItemModifier();
            ItemStack itemStack = itemModifier.read(0);
            itemModifier.write(0, restoreItem(user, itemStack));
        }
    }

}
