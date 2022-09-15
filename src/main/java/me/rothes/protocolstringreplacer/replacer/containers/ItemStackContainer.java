package me.rothes.protocolstringreplacer.replacer.containers;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTList;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ItemStackContainer extends AbstractContainer<ItemStack> {

    private static final boolean NAME_JSON = ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 13;
    private static final boolean LORE_JSON = ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 14;
    private static final Material WRITABLE_BOOK;

    static {
        Material writableBook;
        try {
            writableBook = Material.valueOf("BOOK_AND_QUILL");
        } catch (IllegalArgumentException e) {
            writableBook = Material.WRITABLE_BOOK;
        }
        WRITABLE_BOOK = writableBook;
    }

    protected final boolean cache;
    protected final boolean fromCache;
    protected ReplacerManager.ItemMetaCache metaCache;
    protected NBTItem nbtItem;

    public ItemStackContainer(@NotNull ItemStack itemStack) {
        this(itemStack, true);
    }

    public ItemStackContainer(@NotNull ItemStack itemStack, boolean cache) {
        super(itemStack);
        this.cache = cache;
        if (cache) {
            fromCache = loadCache();
        } else {
            fromCache = false;
            nbtItem = new NBTItem(content);
        }
    }

    public ItemStackContainer(@NotNull ItemStack itemStack, @NotNull Container<?> root) {
        this(itemStack, root, true);
    }

    public ItemStackContainer(@NotNull ItemStack itemStack, @NotNull Container<?> root, boolean cache) {
        super(itemStack, root);
        this.cache = cache;
        if (cache) {
            fromCache = loadCache();
        } else {
            fromCache = false;
            nbtItem = new NBTItem(content);
        }
    }

    public void cloneItem() {
        nbtItem = new NBTItem(nbtItem.getItem());
    }

    @Override
    public void createDefaultChildren() {
        NBTCompound display = nbtItem.getCompound("display");
        if (display != null) {
            if (display.hasKey("Name")) {
                if (NAME_JSON) {
                    children.add(new ChatJsonContainer(display.getString("Name"), root, true) {
                        @Override
                        public String getResult() {
                            String result = super.getResult();
                            display.setString("Name", result);
                            return result;
                        }
                    });
                } else {
                    children.add(new SimpleTextContainer(display.getString("Name"), root) {
                        @Override
                        public String getResult() {
                            String result = super.getResult();
                            display.setString("Name", result);
                            return result;
                        }
                    });
                }
            }
            if (display.hasKey("Lore")) {
                if (LORE_JSON) {
                    addJsonList(display.getStringList("Lore"));
                } else {
                    NBTList<String> list = display.getStringList("Lore");
                    int size = list.size();
                    for (int line = 0; line < size; line++) {
                        int finalLine = line;
                        children.add(new SimpleTextContainer(list.get(finalLine), root) {
                            @Override
                            public String getResult() {
                                String result = super.getResult();
                                list.set(finalLine, result);
                                return result;
                            }
                        });
                    }
                }
            }
        }

        Material type = content.getType();
        if (type == WRITABLE_BOOK || type == Material.WRITTEN_BOOK) {
            if (nbtItem.hasKey("author")) {
                children.add(new SimpleTextContainer(nbtItem.getString("author"), root) {
                    @Override
                    public String getResult() {
                        String result = super.getResult();
                        nbtItem.setString("author", result);
                        return result;
                    }
                });
            }
            if (nbtItem.hasKey("title")) {
                children.add(new SimpleTextContainer(nbtItem.getString("title"), root) {
                    @Override
                    public String getResult() {
                        String result = super.getResult();
                        nbtItem.setString("title", result);
                        return result;
                    }
                });
            }
            if (nbtItem.hasKey("pages")) {
                addJsonList(nbtItem.getStringList("pages"));
            }
        }
        super.createDefaultChildren();
    }

    private void addJsonList(NBTList<String> list) {
        int size = list.size();
        for (int line = 0; line < size; line++) {
            int finalLine = line;
            children.add(new ChatJsonContainer(list.get(finalLine), root, true) {
                @Override
                public String getResult() {
                    String result = super.getResult();
                    list.set(finalLine, result);
                    return result;
                }
            });
        }
    }

    @Override
    public ItemStack getResult() {
        super.getResult();
        content.setItemMeta(nbtItem.getItem().getItemMeta());
        return content;
    }

    public boolean isCache() {
        return cache;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public ReplacerManager.ItemMetaCache getMetaCache() {
        return metaCache;
    }

    private boolean loadCache() {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        ItemMeta original = content.getItemMeta();
        metaCache = replacerManager.getReplacedItemCache(original);
        if (metaCache != null) {
            nbtItem = metaCache.getNbtItem();
            metaCache.setLastAccessTime(System.currentTimeMillis());
            return true;
        } else {
            nbtItem = new NBTItem(content);
            metaCache = replacerManager.addReplacedItemCache(original, nbtItem, false, new int[0]);
            return false;
        }
    }

}
