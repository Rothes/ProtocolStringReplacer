package me.rothes.protocolstringreplacer.replacer.containers;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemMetaContainer extends AbstractContainer<ItemMeta> {

    protected ReplacerManager.ItemMetaCache metaCache;
    protected boolean fromCache = false;

    public ItemMetaContainer(@NotNull ItemMeta itemMeta) {
        super(itemMeta);
    }

    public ItemMetaContainer(@NotNull ItemMeta itemMeta, @NotNull Container<?> root) {
        super(itemMeta, root);
    }

    @Override
    public void createDefaultChildren() {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        metaCache = replacerManager.getReplacedItemCache(content);
        if (metaCache != null) {
            metaCache.setLastAccessTime(System.currentTimeMillis());
            content = metaCache.getReplacedItemMeta();
            fromCache = true;
        } else {
            ItemMeta original = content.clone();
            metaCache = replacerManager.addReplacedItemCache(original, content, new ArrayList<>());
        }
        if (content.hasDisplayName()) {
            children.add(new SimpleTextContainer(content.getDisplayName(), root) {
                @Override
                public String getResult() {
                    String result = super.getResult();
                    ItemMetaContainer.this.content.setDisplayName(result);
                    return result;
                }
            });
        }

        if (content.hasLore()) {
            children.add(new LoreListContainer(content.getLore(), root) {
                @Override
                public List<String> getResult() {
                    List<String> result = super.getResult();
                    ItemMetaContainer.this.content.setLore(result);
                    return result;
                }
            });
        }

        if (content instanceof BookMeta) {
            children.add(new BookMetaContainer((BookMeta) content, root));
        }
        super.createDefaultChildren();
    }

    public ReplacerManager.ItemMetaCache getMetaCache() {
        return metaCache;
    }

    public boolean isFromCache() {
        return fromCache;
    }

}
