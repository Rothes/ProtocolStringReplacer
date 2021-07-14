package me.Rothes.ProtocolStringReplacer.Replacer;

import me.Rothes.ProtocolStringReplacer.API.Configuration.CommentYamlConfiguration;
import me.Rothes.ProtocolStringReplacer.API.Configuration.DotYamlConfiguration;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.User.User;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
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

    private static class ItemMetaCache {

        private ItemMeta replacedItemMeta;
        private Long lastAccessTime;
        private Boolean hasPlaceholder;

        public ItemMetaCache(ItemMeta replacedItemMeta, @Nonnull Long lastAccessTime, @Nonnull Boolean hasPlaceholder) {
            Validate.notNull(lastAccessTime, "Last Access Time cannot be null");
            Validate.notNull(hasPlaceholder, "Boolean cannot be null");
            this.replacedItemMeta = replacedItemMeta;
            this.lastAccessTime = lastAccessTime;
            this.hasPlaceholder = hasPlaceholder;
        }

    }

    private PAPIReplacer papiReplacer;
    private char papihead;
    private char papitail;
    private LinkedList<ReplacerConfig> replacerConfigList = new LinkedList<>();
    private HashMap<ItemMeta, ItemMetaCache> replacedItemCache = new HashMap<>();
    private BukkitTask cleanTask;

    public BukkitTask getCleanTask() {
        return cleanTask;
    }

    public void initialize() {
        this.papiReplacer = new PAPIReplacer();
        papihead = papiReplacer.getHead();
        papitail = papiReplacer.getTail();

        File path = new File(ProtocolStringReplacer.getInstance().getDataFolder() + "/Replacers");
        long startTime = System.currentTimeMillis();
        HashMap<File, DotYamlConfiguration> loadedFiles = loadReplacesFiles(path);
        Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §a预加载 " + loadedFiles.size() + " 个替换配置文件. §8耗时 " + (System.currentTimeMillis() - startTime) + "ms");
        if (loadedFiles.size() == 0) {
            return;
        }
        for (Map.Entry<File, DotYamlConfiguration> entry : loadedFiles.entrySet()) {
            File file = entry.getKey();
            DotYamlConfiguration config = entry.getValue();
            ReplacerConfig replacerConfig = new ReplacerConfig(file, config);
            addReplacerConfig(replacerConfig);
        }

        // To warm up the lambda below.
        replacedItemCache.put(null, new ItemMetaCache(null, 1L, false));

        ProtocolStringReplacer instrance = ProtocolStringReplacer.getInstance();
        CommentYamlConfiguration config = instrance.getConfig();
        long cleanAccessInterval = config.getInt("Options.Features.ItemMetaCache.Clean-Access-Interval", 300) * 1000L;
        long cleanTaskInterval = config.getInt("Options.Features.ItemMetaCache.Clean-Task-Interval", 600) * 20L;
        cleanTask = Bukkit.getScheduler().runTaskTimerAsynchronously(instrance, () -> {
            List<ItemMeta> needToRemove = new ArrayList<>();
            long currentTime = System.currentTimeMillis();
            for (Map.Entry<ItemMeta, ItemMetaCache> entry : replacedItemCache.entrySet()) {
                if ((currentTime - entry.getValue().lastAccessTime) > cleanAccessInterval) {
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
        }, 0L, cleanTaskInterval);
    }

    public void addReplacerConfig(ReplacerConfig replacerConfig) {
        int size = replacerConfigList.size();
        for (int i = 0; i <= size; i++) {
            if (i == replacerConfigList.size()) {
                replacerConfigList.add(replacerConfig);
            } else if (replacerConfig.getPriority() > replacerConfigList.get(i).getPriority()) {
                replacerConfigList.add(i, replacerConfig);
                break;
            }
        }
    }

    public LinkedList<ReplacerConfig> getReplacerConfigList() {
        return replacerConfigList;
    }

    public int getReplacesCount() {
        int count = 0;
        for (var replacerConfig : replacerConfigList) {
            if (replacerConfig.isEnable()) {
                count = count + replacerConfig.getReplaces().size();
            }
        }
        return count;
    }

    @Nonnull
    public BaseComponent[] getReplacedComponents(@Nonnull BaseComponent[] baseComponents, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter) {
        Validate.notNull(baseComponents, "BaseComponent Array cannot be null");
        Validate.notNull(user, "User cannot be null");
        Validate.notNull(filter, "Filter cannot be null");
        for (int i = 0; i < baseComponents.length; i++) {
            BaseComponent baseComponent = baseComponents[i];
            baseComponents[i] = getReplacedComponent(baseComponent, user, filter);
        }
        return baseComponents;
    }

    public BaseComponent getReplacedComponent(@Nonnull BaseComponent baseComponent, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter) {
        Validate.notNull(baseComponent, "BaseComponent cannot be null");
        Validate.notNull(user, "User cannot be null");
        Validate.notNull(filter, "Filter cannot be null");
        if (baseComponent instanceof TextComponent) {
            TextComponent textComponent = (TextComponent) baseComponent;
            textComponent.setText(getReplacedString(textComponent.getText(), user, filter));
        } else if (baseComponent instanceof TranslatableComponent) {
            TranslatableComponent translatableComponent = (TranslatableComponent) baseComponent;
            if (translatableComponent.getWith() != null) {
                translatableComponent.setWith(replaceExtra(translatableComponent.getWith(), user, filter));
            }
        }
        if (baseComponent.getExtra() != null) {
            baseComponent.setExtra(replaceExtra(baseComponent.getExtra(), user, filter));
        }
        return baseComponent;
    }

    public List<BaseComponent> replaceExtra(@Nonnull List<BaseComponent> extra, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter) {
        Validate.notNull(extra, "BaseComponent List cannot be null");
        Validate.notNull(user, "User cannot be null");
        Validate.notNull(filter, "Filter cannot be null");
        for (BaseComponent extraComponent : extra) {
            getReplacedComponent(extraComponent, user, filter);
        }
        return extra;
    }

    @Nonnull
    public String getReplacedString(@Nonnull String string, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter) {
        Validate.notNull(string, "String cannot be null");
        Validate.notNull(user, "User cannot be null");
        Validate.notNull(filter, "Filter cannot be null");
        for (ReplacerConfig replacerConfig : replacerConfigList) {
            if (replacerConfig.isEnable() && filter.test(replacerConfig, user)) {
                string = getFileReplacedString(user, string, replacerConfig, true);
            }
        }
        return string;
    }

    @SuppressWarnings("UnusedReturnValue")
    @Nonnull
    public ItemStack getReplacedItemStack(@Nonnull ItemStack itemStack, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter) {
        Validate.notNull(itemStack, "ItemStack cannot be null");
        if (itemStack.hasItemMeta()) {
            itemStack.setItemMeta(getReplacedItemMeta(itemStack.getItemMeta(), user, filter));
        }
        return itemStack;
    }

    @SuppressWarnings("UnusedReturnValue")
    @Nonnull
    public ItemMeta getReplacedItemMeta(@Nonnull ItemMeta itemMeta, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter) {
        Validate.notNull(itemMeta, "ItemMeta cannot be null");
        Validate.notNull(user, "User cannot be null");
        Validate.notNull(filter, "Filter cannot be null");

        boolean hasPlaceholder = false;
        ItemMetaCache metaCache = replacedItemCache.get(itemMeta);
        if (metaCache != null) {
            metaCache.lastAccessTime = System.currentTimeMillis();
            itemMeta = metaCache.replacedItemMeta;
            hasPlaceholder = metaCache.hasPlaceholder;
        } else {
            ItemMeta original = itemMeta.clone();
            String replaced;
            for (var replacerConfig : replacerConfigList) {
                if (replacerConfig.isEnable() && filter.test(replacerConfig, user)) {
                    if (itemMeta.hasDisplayName()) {
                        replaced = getFileReplacedString(user, itemMeta.getDisplayName(), replacerConfig, false);
                        itemMeta.setDisplayName(replaced);
                        hasPlaceholder = hasPlaceholder || hasPlaceholder(replaced);
                    }

                    if (itemMeta.hasLore()) {
                        List<String> lore = itemMeta.getLore();
                        for (int i = 0; i < lore.size(); i++) {
                            replaced = getFileReplacedString(user, lore.get(i), replacerConfig, false);
                            lore.set(i, replaced);
                            hasPlaceholder = hasPlaceholder || hasPlaceholder(replaced);
                        }
                        itemMeta.setLore(lore);
                    }
                    if (itemMeta instanceof BookMeta) {
                        BookMeta bookMeta = (BookMeta) itemMeta;
                        if (bookMeta.hasAuthor()) {
                            replaced = getFileReplacedString(user, bookMeta.getAuthor(), replacerConfig, false);
                            bookMeta.setAuthor(replaced);
                            hasPlaceholder = hasPlaceholder || hasPlaceholder(replaced);
                        }
                        if (bookMeta.hasTitle()) {
                            replaced = getFileReplacedString(user, bookMeta.getTitle(), replacerConfig, false);
                            bookMeta.setTitle(replaced);
                            hasPlaceholder = hasPlaceholder || hasPlaceholder(replaced);
                        }
                        if (bookMeta.hasPages()) {
                            List<String> pages = new ArrayList<>(bookMeta.getPages());
                            for (int i = 0; i < pages.size(); i++) {
                                replaced = getFileReplacedString(user, pages.get(i), replacerConfig, false);
                                pages.set(i, replaced);
                                hasPlaceholder = hasPlaceholder || hasPlaceholder(replaced);
                            }
                            bookMeta.setPages(pages);
                        }
                    }
                }
            }
            replacedItemCache.put(original, new ItemMetaCache(itemMeta, System.currentTimeMillis(), hasPlaceholder));
        }

        return hasPlaceholder? updatePlaceholders(user, itemMeta) : itemMeta;
    }

    public void saveReplacerConfigs() {
        for (ReplacerConfig replacerConfig : replacerConfigList) {
            if (replacerConfig.isEdited()) {
                replacerConfig.saveConfig();
            }
        }
    }

    public static boolean isYmlFile(@Nonnull File file) {
        Validate.notNull(file, "File cannot be null");
        return isYmlFile(file.getName());
    }

    public static boolean isYmlFile(@Nonnull String name) {
        Validate.notNull(name, "FileName cannot be null");
        int length = name.length();
        if (length > 4) {
            String subfix = name.substring(length - 4, length);
            return subfix.equalsIgnoreCase(".yml");
        }
        return false;
    }

    public boolean hasPlaceholder(@NotNull String string) {
        boolean headFound = false;
        boolean tailFound = false;
        for(int i = 0; i < string.length(); i++) {
            char Char = string.charAt(i);
            if (!headFound) {
                if (Char == papihead) {
                    headFound = true;
                }
            } else {
                if (Char == papitail) {
                    tailFound = true;
                    break;
                }
            }
        }
        return tailFound;
    }

    public String setPlaceholder(@NotNull User user, @NotNull String string) {
        return papiReplacer.apply(string, user.getPlayer(),
                PlaceholderAPIPlugin.getInstance().getLocalExpansionManager()::getExpansion);
    }

    @Nonnull
    public HashMap<File, DotYamlConfiguration> loadReplacesFiles(@Nonnull File path) {
        Validate.notNull(path, "Path cannot be null");
        HashMap<File, DotYamlConfiguration> loaded = new HashMap<>();
        if (path.exists()) {
            File[] files = path.listFiles();
            for (var file : files) {
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
    private String getFileReplacedString(@Nonnull User user, @Nonnull String string, @Nonnull ReplacerConfig replacerConfig, boolean setPlaceholders) {
        Validate.notNull(user, "User cannot be null");
        Validate.notNull(string, "String cannot be null");
        Validate.notNull(replacerConfig, "Replacer File cannot be null");

        Object object = replacerConfig.getReplaces().entrySet();
        switch (replacerConfig.getMatchType()) {
            case CONTAIN:
                var containSet = (Set<Map.Entry<String, String>>) object;
                for (var entry : containSet) {
                    string = StringUtils.replace(string, entry.getKey(), entry.getValue());
                }
                break;
            case EQUAL:
                var equalSet = (Set<Map.Entry<String, String>>) object;
                for (var entry : equalSet) {
                    if (string.equals(entry.getKey())) {
                        string = entry.getValue();
                    }
                }
                break;
            case REGEX:
                var regexSet = (Set<Map.Entry<Pattern, String>>) object;
                for (var entry : regexSet) {
                    string = entry.getKey().matcher(string).replaceAll(entry.getValue());
                }
        }
        return setPlaceholders && hasPlaceholder(string)? setPlaceholder(user, string) : string;
    }

    private ItemMeta updatePlaceholders(@Nonnull User user, @Nonnull ItemMeta itemMeta) {
        Validate.notNull(user, "User cannot be null");
        Validate.notNull(itemMeta, "ItemMeta cannot be null");

        itemMeta = itemMeta.clone();
        if (hasPlaceholder(itemMeta.getDisplayName())) {
            itemMeta.setDisplayName(setPlaceholder(user, itemMeta.getDisplayName()));
        }
        if (itemMeta.hasLore()) {
            List<String> lore = itemMeta.getLore();
            for (int i = 0; i < lore.size(); i++) {
                if (hasPlaceholder(lore.get(i))) {
                    lore.set(i, setPlaceholder(user, lore.get(i)));
                }
            }
            itemMeta.setLore(lore);
        }
        if (itemMeta instanceof BookMeta) {
            BookMeta bookMeta = (BookMeta) itemMeta;
            if (bookMeta.hasAuthor() && hasPlaceholder(bookMeta.getAuthor())) {
                bookMeta.setAuthor(setPlaceholder(user, bookMeta.getAuthor()));
            }
            if (bookMeta.hasTitle() && hasPlaceholder(bookMeta.getTitle())) {
                bookMeta.setTitle(setPlaceholder(user, bookMeta.getTitle()));
            }
            if (bookMeta.hasPages()) {
                List<String> pages = new ArrayList<>(bookMeta.getPages());
                for (int i = 0; i < pages.size(); i++) {
                    if (hasPlaceholder(pages.get(i))) {
                        pages.set(i, setPlaceholder(user, pages.get(i)));
                    }
                }
                bookMeta.setPages(pages);
            }
        }
        return itemMeta;
    }

}
