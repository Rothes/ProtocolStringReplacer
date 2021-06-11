package me.Rothes.ProtocolStringReplacer.Replacer;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;

public class ItemMetaCache {

    private ItemMeta replacedItemMeta;
    private Long lastAccessTime;

    public ItemMetaCache(ItemMeta replacedItemMeta, @Nonnull Long lastAccessTime) {
        Validate.notNull(lastAccessTime, "Last Access Time cannot be null");
        this.replacedItemMeta = replacedItemMeta;
        this.lastAccessTime = lastAccessTime;
    }

    public ItemMeta getReplacedItemMeta() {
        return replacedItemMeta;
    }

    public Long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setReplacedItemMeta(ItemMeta replacedItemMeta) {
        this.replacedItemMeta = replacedItemMeta;
    }

    public void setLastAccessTime(@Nonnull Long lastAccessTime) {
        Validate.notNull(lastAccessTime, "Last Access Time cannot be null");
        this.lastAccessTime = lastAccessTime;
    }

}
