package me.rothes.protocolstringreplacer.packetlisteners.client.itemstack;

import com.comphenix.protocol.PacketType;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.packetlisteners.client.AbstractClientPacketListener;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public abstract class AbstractClientItemPacketListener extends AbstractClientPacketListener {

    protected AbstractClientItemPacketListener(PacketType packetType) {
        super(packetType);
    }

    protected void restoreItem(PsrUser user, ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            NBTItem nbtItem = new NBTItem(itemStack);
            if (!nbtItem.hasKey("ProtocolStringReplacer")) {
                return;
            }
            Short uniqueCacheKey = nbtItem.getCompound("ProtocolStringReplacer").getShort("UserMetaCacheKey");

            if (uniqueCacheKey != null) {
                HashMap<Short, ItemMeta> userMetaCache = user.getMetaCache();
                ItemMeta original = userMetaCache.get(uniqueCacheKey);
                if (original == null) {
                    ProtocolStringReplacer.warn("Failed to get original ItemMeta by meta-cache key, ignoring.\n" + itemStack);
                    return;
                }
                itemStack.setItemMeta(original);
            }
        }
    }

}
