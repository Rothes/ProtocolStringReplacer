package me.rothes.protocolstringreplacer.packetlisteners.client.itemstack;

import com.comphenix.protocol.PacketType;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.packetlisteners.client.AbstractClientPacketListener;
import me.rothes.protocolstringreplacer.api.user.User;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.HashMap;
import java.util.List;

public abstract class AbstractClientItemPacketListener extends AbstractClientPacketListener {

    protected NamespacedKey userCacheKey;

    protected AbstractClientItemPacketListener(PacketType packetType) {
        super(packetType);
    }

    protected void resotreItem(User user, ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            Short uniqueCacheKey = null;
            if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 13) {
                CustomItemTagContainer tagContainer = itemMeta.getCustomTagContainer();
                if (tagContainer.hasCustomTag(getUserCacheKey(), ItemTagType.SHORT)) {
                    uniqueCacheKey = tagContainer.getCustomTag(getUserCacheKey(), ItemTagType.SHORT);
                }
            } else {
                List<String> lore = itemMeta.getLore();
                if (lore != null) {
                    String string = lore.get(lore.size() - 1);
                    if (string.startsWith("§p§s§r§-§x")) {
                        StringBuilder stringBuilder = new StringBuilder(string.substring(10));
                        for (int i = stringBuilder.length() - 1; i > 0; i--) {
                            stringBuilder.deleteCharAt(--i);
                        }
                        uniqueCacheKey = Short.parseShort(stringBuilder.toString());
                    }
                }
            }
            if (uniqueCacheKey != null) {
                HashMap<Short, ItemMeta> userMetaCache = user.getMetaCache();
                ItemMeta original = userMetaCache.get(uniqueCacheKey);
                itemStack.setItemMeta(original);
            }
        }
    }

    protected NamespacedKey getUserCacheKey() {
        if (userCacheKey == null) {
            userCacheKey = ProtocolStringReplacer.getInstance().getPacketListenerManager().getUserCacheKey();
        }
        return userCacheKey;
    }

}
