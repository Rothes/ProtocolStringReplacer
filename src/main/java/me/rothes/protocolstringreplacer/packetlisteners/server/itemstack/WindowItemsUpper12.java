package me.rothes.protocolstringreplacer.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.user.User;
import org.bukkit.inventory.ItemStack;

public final class WindowItemsUpper12 extends AbstractServerItemPacketListener {

    public WindowItemsUpper12() {
        super(PacketType.Play.Server.WINDOW_ITEMS);
    }

    protected void process(PacketEvent packetEvent) {
        User user = getEventUser(packetEvent);
        for (ItemStack itemStack : packetEvent.getPacket().getItemListModifier().read(0)) {
            if (itemStack.hasItemMeta()) {
                ItemStack original = itemStack.clone();
                ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedItemStack(itemStack, user, itemFilter);
                if (!original.isSimilar(itemStack)) {
                    saveUserMetaCache(user, original, itemStack);
                }
            }
        }
    }

}
