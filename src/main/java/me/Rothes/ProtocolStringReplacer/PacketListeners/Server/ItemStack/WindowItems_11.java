package me.Rothes.ProtocolStringReplacer.PacketListeners.Server.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.Rothes.ProtocolStringReplacer.User.User;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import org.bukkit.inventory.ItemStack;

public final class WindowItems_11 extends AbstractServerItemPacketListener {

    public WindowItems_11() {
        super(PacketType.Play.Server.WINDOW_ITEMS);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            User user = getEventUser(packetEvent);
            for (ItemStack itemStack : packetEvent.getPacket().getItemListModifier().read(0)) {
                if (itemStack.hasItemMeta()) {
                    ItemStack original = itemStack.clone();
                    ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedItemStack(itemStack, user, itemFilter);
                    if (!original.isSimilar(itemStack)) {
                        saveUserMetaCacche(user, original, itemStack);
                    }
                }
            }
        }
    };

}
