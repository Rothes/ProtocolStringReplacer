package me.Rothes.ProtocolStringReplacer.Replacer;

import me.Rothes.ProtocolStringReplacer.API.Configuration.CommentYamlConfiguration;
import me.Rothes.ProtocolStringReplacer.API.Configuration.DotYamlConfiguration;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.User.User;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.replacer.Replacer;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

public class ReplacerManager {

    private final char PAPIHead = ProtocolStringReplacer.getInstance().getConfig().getString("Options.Feature.Placeholder.Placeholder-Head", "｛").charAt(0);
    private final char PAPITail = ProtocolStringReplacer.getInstance().getConfig().getString("Options.Feature.Placeholder.Placeholder-Tail", "｝").charAt(0);

    private final Replacer replacer = new me.Rothes.ProtocolStringReplacer.Replacer.PAPIReplacer();
    private final LinkedList<ReplacerFile> replacerFileList = new LinkedList<>();
    private final HashMap<ItemMeta, ItemMetaCache> replacedItemCache = new HashMap<>();

    public void initialize() {
        File path = new File(ProtocolStringReplacer.getInstance().getDataFolder() + "/Replacers");
        long startTime = System.currentTimeMillis();
        HashMap<File, DotYamlConfiguration> loadedFiles = loadReplacesFiles(path);
        Bukkit.getConsoleSender().sendMessage("§7[§6ProtocolStringReplacer§7] §a预加载 " + loadedFiles.size() + " 个替换配置文件. §8耗时 " + (System.currentTimeMillis() - startTime) + "ms");
        if (loadedFiles.size() == 0) {
            return;
        }
        for (Map.Entry<File, DotYamlConfiguration> entry : loadedFiles.entrySet()) {
            File file = entry.getKey();
            DotYamlConfiguration config = entry.getValue();
            ReplacerFile replacerFile = new ReplacerFile(file, config);
            int size = replacerFileList.size();
            for (int i = 0; i <= size; i++) {
                if (i == replacerFileList.size()) {
                    replacerFileList.add(replacerFile);
                } else if (replacerFile.getPriority() > replacerFileList.get(i).getPriority()) {
                    replacerFileList.add(i, replacerFile);
                    break;
                }
            }
        }

        // To warm up the lambda below.
        replacedItemCache.put(null, new ItemMetaCache(null, 1L));

        ProtocolStringReplacer instrance = ProtocolStringReplacer.getInstance();
        CommentYamlConfiguration config = instrance.getConfig();
        Bukkit.getScheduler().runTaskTimerAsynchronously(instrance, () -> {
            List<ItemMeta> needToRemove = new ArrayList<>();
            long currentTime = System.currentTimeMillis();
            for (Map.Entry<ItemMeta, ItemMetaCache> entry : replacedItemCache.entrySet()) {
                if ((currentTime - entry.getValue().getLastAccessTime()) > config.getInt("Options.Feature.ItemMetaCache.Clean-Access-Interval", 300) * 1000L) {
                    needToRemove.add(entry.getKey());
                }
            }
            if (!needToRemove.isEmpty()) {
                Bukkit.getScheduler().runTask(instrance, () -> {
                    for (ItemMeta itemMeta : needToRemove) {
                        replacedItemCache.remove(itemMeta);
                    }
                });
            }
        }, 0L, config.getInt("Options.Feature.ItemMetaCache.Clean-Task-Interval", 600) * 20L);
    }

    @Nonnull
    public String getReplacedString(@Nonnull String string, @Nonnull User user, @Nonnull BiPredicate<ReplacerFile, User> filter) {
        Validate.notNull(string, "String cannot be null");
        Validate.notNull(user, "User cannot be null");
        Validate.notNull(filter, "Filter cannot be null");
        for (ReplacerFile replacerFile : replacerFileList) {
            if (replacerFile.isEnable() && filter.test(replacerFile, user)) {
                string = getFileReplacedString(user, string, replacerFile, true);
            }
        }
        return string;
    }

    @SuppressWarnings("UnusedReturnValue")
    @Nonnull
    public ItemStack getReplacedItemStack(@Nonnull ItemStack itemStack, @Nonnull User user, @Nonnull BiPredicate<ReplacerFile, User> filter) {
        Validate.notNull(itemStack, "ItemStack cannot be null");
        if (itemStack.hasItemMeta()) {
            itemStack.setItemMeta(getReplacedItemMeta(itemStack.getItemMeta(), user, filter));
        }
        return itemStack;
    }

    @SuppressWarnings("UnusedReturnValue")
    @Nonnull
    public ItemMeta getReplacedItemMeta(@Nonnull ItemMeta itemMeta, @Nonnull User user, @Nonnull BiPredicate<ReplacerFile, User> filter) {
        Validate.notNull(itemMeta, "ItemMeta cannot be null");
        Validate.notNull(user, "User cannot be null");
        Validate.notNull(filter, "Filter cannot be null");
            ItemMetaCache metaCache = replacedItemCache.get(itemMeta);
            if (metaCache != null) {
                metaCache.setLastAccessTime(System.currentTimeMillis());
                itemMeta = metaCache.getReplacedItemMeta();
            } else {
                ItemMeta original = itemMeta.clone();
                for (ReplacerFile replacerFile : replacerFileList) {
                    if (replacerFile.isEnable() && filter.test(replacerFile, user)) {
                        itemMeta.setDisplayName(getFileReplacedString(user, itemMeta.getDisplayName(), replacerFile, false));

                        if (itemMeta.hasLore()) {
                            List<String> lore = itemMeta.getLore();
                            for (int i = 0; i < lore.size(); i++) {
                                lore.set(i, getFileReplacedString(user, lore.get(i), replacerFile, false));
                            }
                            itemMeta.setLore(lore);
                        }
                    }
                }
                replacedItemCache.put(original, new ItemMetaCache(itemMeta, System.currentTimeMillis()));
            }

        return updatePlaceholders(user, itemMeta);
    }

    @Nonnull
    private HashMap<File, DotYamlConfiguration> loadReplacesFiles(@Nonnull File path) {
        Validate.notNull(path, "Path cannot be null");
        HashMap<File, DotYamlConfiguration> loaded = new HashMap<>();
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isFile() && isYmlFile(file)) {
                    DotYamlConfiguration dotYamlConfiguration = DotYamlConfiguration.loadConfiguration(file);
                    loaded.put(file, dotYamlConfiguration);
                } else if (file.isDirectory()) {
                    loadReplacesFiles(file).forEach(loaded::put);
                }
            }
        }
        return loaded;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    private String getFileReplacedString(@Nonnull User user, @Nonnull String string, @Nonnull ReplacerFile replacerFile, boolean setPlaceholders) {
        Validate.notNull(user, "User cannot be null");
        Validate.notNull(string, "String cannot be null");
        Validate.notNull(replacerFile, "Replacer File cannot be null");

        Object object = replacerFile.getReplaces().entrySet();
        switch (replacerFile.getMatchType()) {
            case CONTAIN:
                Set<Map.Entry<String, String>> containSet = (Set<Map.Entry<String, String>>) object;
                for (Map.Entry<String, String> entry : containSet) {
                    string = StringUtils.replace(string, entry.getKey(), entry.getValue());
                }
                break;
            case EQUAL:
                Set<Map.Entry<String, String>> equalSet = (Set<Map.Entry<String, String>>) object;
                for (Map.Entry<String, String> entry : equalSet) {
                    if (string.equals(entry.getKey())) {
                        string = entry.getValue();
                    }
                }
                break;
            case REGEX:
                Set<Map.Entry<Pattern, String>> regexSet = (Set<Map.Entry<Pattern, String>>) object;
                for (Map.Entry<Pattern, String> entry : regexSet) {
                    string = entry.getKey().matcher(string).replaceAll(entry.getValue());
                }
        }
        return setPlaceholders? setPlaceholder(user, string) : string;
    }

    private boolean isYmlFile(@Nonnull File file) {
        Validate.notNull(file, "File cannot be null");
        String name = file.getName();
        int length = name.length();
        if (length > 4) {
            String subfix = name.substring(length - 4, length);
            return subfix.equalsIgnoreCase(".yml");
        }
        return false;
    }

    private ItemMeta updatePlaceholders(@Nonnull User user, @Nonnull ItemMeta itemMeta) {
        Validate.notNull(user, "User cannot be null");
        Validate.notNull(itemMeta, "ItemMeta cannot be null");

        itemMeta = itemMeta.clone();
        itemMeta.setDisplayName(setPlaceholder(user, itemMeta.getDisplayName()));
        if (itemMeta.hasLore()) {
            List<String> lore = itemMeta.getLore();
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, setPlaceholder(user, lore.get(i)));
            }
            itemMeta.setLore(lore);
        }
        return itemMeta;
    }

    private String setPlaceholder(@NotNull User user, @NotNull String string) {
        boolean headFound = false;
        boolean tailFound = false;
        for (char Char : string.toCharArray()) {
            if (!headFound) {
                if (Char == PAPIHead) {
                    headFound = true;
                }
            } else {
                if (Char == PAPITail) {
                    tailFound = true;
                    break;
                }
            }
        }
        return tailFound? replacer.apply(string, user.getPlayer(),
                PlaceholderAPIPlugin.getInstance().getLocalExpansionManager()::getExpansion) : string;
    }

}
