package me.rothes.protocolstringreplacer.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import org.bukkit.inventory.ItemStack;

public final class SetSlot extends AbstractServerItemPacketListener {

    public SetSlot() {
        super(PacketType.Play.Server.SET_SLOT);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        ItemStack itemStack = packetEvent.getPacket().getItemModifier().read(0);
        replaceItemStack(packetEvent, user, listenType, itemStack, itemFilter);
    }

}
