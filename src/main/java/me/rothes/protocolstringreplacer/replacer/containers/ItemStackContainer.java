package me.rothes.protocolstringreplacer.replacer.containers;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTList;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ItemStackContainer extends AbstractContainer<ItemStack> {

    protected ReplacerManager.ItemMetaCache metaCache;
    protected boolean fromCache = false;

    protected NBTItem nbtItem;

    public ItemStackContainer(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    public ItemStackContainer(@NotNull ItemStack itemStack, @NotNull Container<?> root) {
        super(itemStack, root);
    }

    @Override
    public void createDefaultChildren() {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        ItemMeta original = content.getItemMeta();
        metaCache = replacerManager.getReplacedItemCache(original);
        if (metaCache != null) {
            nbtItem = metaCache.getNbtItem();
            metaCache.setLastAccessTime(System.currentTimeMillis());
            fromCache = true;
        } else {
            nbtItem = new NBTItem(content);
            metaCache = replacerManager.addReplacedItemCache(original, nbtItem, false, new ArrayList<>());
        }
        NBTCompound display = nbtItem.getCompound("display");
        if (display != null) {
            if (display.hasKey("Name")) {
                children.add(new ChatJsonContainer(display.getString("Name"), root, true) {
                    @Override
                    public String getResult() {
                        String result = super.getResult();
                        display.setString("Name", result);
                        return result;
                    }
                });
            }
            if (display.hasKey("Lore")) {
                NBTList<String> loreNbt = display.getStringList("Lore");
                int size = loreNbt.size();
                for (int line = 0; line < size; line++) {
                    int finalLine = line;
                    children.add(new ChatJsonContainer(loreNbt.get(finalLine), root, true) {
                        @Override
                        public String getResult() {
                            String result = super.getResult();
                            loreNbt.set(finalLine, result);
                            return result;
                        }
                    });
                }
            }
        }

        if (content instanceof BookMeta) {
            children.add(new BookMetaContainer((BookMeta) content, root));
        }
        super.createDefaultChildren();
    }

    @Override
    public ItemStack getResult() {
        super.getResult();
        content.setItemMeta(nbtItem.getItem().getItemMeta());
        return content;
    }

    public ReplacerManager.ItemMetaCache getMetaCache() {
        return metaCache;
    }

    public boolean isFromCache() {
        return fromCache;
    }

}
