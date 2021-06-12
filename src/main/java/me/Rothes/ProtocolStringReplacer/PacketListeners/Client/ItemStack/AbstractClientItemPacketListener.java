package me.Rothes.ProtocolStringReplacer.PacketListeners.Client.ItemStack;

import com.comphenix.protocol.PacketType;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.AbstractServerPacketListener;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.User.User;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.HashMap;

public class AbstractClientItemPacketListener extends AbstractServerPacketListener {

    protected final NamespacedKey userCacheKey = ProtocolStringReplacer.getInstance().getPacketListenerManager().getUserCacheKey();

    protected AbstractClientItemPacketListener(PacketType packetType) {
        super(packetType);
    }

    protected void resotreItem(User user, ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            CustomItemTagContainer tagContainer = itemMeta.getCustomTagContainer();
            if (tagContainer.hasCustomTag(userCacheKey, ItemTagType.SHORT)) {
                Short uniqueCacheKey = tagContainer.getCustomTag(userCacheKey, ItemTagType.SHORT);
                HashMap<Short, ItemMeta> userMetaCache = user.getMetaCache();
                ItemMeta original = userMetaCache.get(uniqueCacheKey);
                itemStack.setItemMeta(original);
            }
        }
    }

}
