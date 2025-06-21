package io.github.rothes.protocolstringreplacer.packetlistener.client.itemstack;

import com.comphenix.protocol.PacketType;
import de.tr7zw.nbtapi.NBT;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.packetlistener.client.BaseClientPacketListener;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public abstract class BaseClientItemPacketListener extends BaseClientPacketListener {

    protected BaseClientItemPacketListener(PacketType packetType) {
        super(packetType);
    }

    protected ItemStack restoreItem(PsrUser user, ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return itemStack;
        }
        return NBT.get(itemStack, nbt -> {
            if (nbt.hasTag("ProtocolStringReplacer")) {
                Short uniqueCacheKey = nbt.getCompound("ProtocolStringReplacer").getShort("UserMetaCacheKey");
                if (uniqueCacheKey != null) {
                    HashMap<Short, ItemStack> userItemRestoreCache = user.getItemRestoreCache();
                    ItemStack original = userItemRestoreCache.get(uniqueCacheKey);
                    if (original == null) {
                        ProtocolStringReplacer.warn("Failed to get original ItemMeta by meta-cache key, ignoring.\n" + itemStack);
                        return itemStack;
                    }
                    return original;
                } else {
                    ProtocolStringReplacer.warn("Failed to get original ItemMeta by meta-cache key due to null, ignoring.\n" + itemStack);
                }
            }
            return itemStack;
        });
    }

}
