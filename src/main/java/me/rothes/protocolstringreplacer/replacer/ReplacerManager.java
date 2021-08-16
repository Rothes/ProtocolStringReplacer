package me.rothes.protocolstringreplacer.replacer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import me.rothes.protocolstringreplacer.api.configuration.DotYamlConfiguration;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.user.User;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang3.StringEscapeUtils;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

public class ReplacerManager {

    private PAPIReplacer papiReplacer;
    private char papihead;
    private char papitail;
    private LinkedList<ReplacerConfig> replacerConfigList = new LinkedList<>();
    private HashMap<ItemMeta, ItemMetaCache> replacedItemCache = new HashMap<>();
    private BukkitTask cleanTask;

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

    @Nonnull
    public static HashMap<File, DotYamlConfiguration> loadReplacesFiles(@Nonnull File path) {
        Validate.notNull(path, "Path cannot be null");
        HashMap<File, DotYamlConfiguration> loaded = new HashMap<>();
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isFile() && isYmlFile(file)) {
                    DotYamlConfiguration dotYamlConfiguration = DotYamlConfiguration.loadConfiguration(file);
                    loaded.put(file, dotYamlConfiguration);
                } else if (file.isDirectory()) {
                    loaded.putAll(loadReplacesFiles(file));
                }
            }
        }
        return loaded;
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

    public BukkitTask getCleanTask() {
        return cleanTask;
    }

    public void initialize() {
        this.papiReplacer = new PAPIReplacer();
        papihead = papiReplacer.getHead();
        papitail = papiReplacer.getTail();

        File path = new File(ProtocolStringReplacer.getInstance().getDataFolder() + "/Replacers");
        long startTime = System.nanoTime();
        HashMap<File, DotYamlConfiguration> loadedFiles = loadReplacesFiles(path);
        Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §a预加载 " + loadedFiles.size() + " 个替换配置文件. §8耗时 " + (System.nanoTime() - startTime) / 1000000d + "ms");
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
        long cleanAccessInterval = instrance.getConfigManager().cleanAccessInterval;
        long cleanTaskInterval = instrance.getConfigManager().cleanTaskInterval;
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
        for (ReplacerConfig replacerConfig : replacerConfigList) {
            if (replacerConfig.isEnable()) {
                count = count + replacerConfig.getReplaces(ReplacesMode.COMMON).size();
            }
        }
        return count;
    }

    @Nonnull
    public BaseComponent[] getReplacedComponents(@Nonnull BaseComponent[] baseComponents, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter) {
        return getReplacedComponents(baseComponents, user, filter, true);
    }

    @Nonnull
    public BaseComponent[] getReplacedComponents(@Nonnull BaseComponent[] baseComponents, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter, boolean setPlaceholders) {
        Validate.notNull(baseComponents, "BaseComponent Array cannot be null");
        Validate.notNull(user, "user cannot be null");
        Validate.notNull(filter, "Filter cannot be null");
        for (int i = 0; i < baseComponents.length; i++) {
            BaseComponent baseComponent = baseComponents[i];
            baseComponents[i] = getReplacedComponent(baseComponent, user, filter, setPlaceholders);
        }
        return baseComponents;
    }

    public BaseComponent getReplacedComponent(@Nonnull BaseComponent baseComponent, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter) {
        return getReplacedComponent(baseComponent, user, filter, true);
    }

    public BaseComponent getReplacedComponent(@Nonnull BaseComponent baseComponent, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter, boolean setPlaceholders) {
        Validate.notNull(baseComponent, "BaseComponent cannot be null");
        Validate.notNull(user, "user cannot be null");
        Validate.notNull(filter, "Filter cannot be null");
        if (baseComponent instanceof TextComponent) {
            TextComponent textComponent = (TextComponent) baseComponent;
            String color = "";
            if (textComponent.getColorRaw() != null) {
                color += textComponent.getColorRaw().toString();
            }
            if (textComponent.isBoldRaw() != null && textComponent.isBoldRaw()) {
                color += "§l";
            }
            if (textComponent.isItalicRaw() != null && textComponent.isItalicRaw()) {
                color += "§o";
            }
            if (textComponent.isObfuscatedRaw() != null && textComponent.isObfuscatedRaw()) {
                color += "§m";
            }
            if (textComponent.isUnderlinedRaw() != null && textComponent.isUnderlinedRaw()) {
                color += "§n";
            }
            String replaced = getReplacedString(color + textComponent.getText(), user, filter, setPlaceholders);
            int length = color.length();
            if (replaced.substring(0, length).equals(color)) {
                replaced = replaced.substring(length);
            }
            textComponent.setText(replaced);
        } else if (baseComponent instanceof TranslatableComponent) {
            TranslatableComponent translatableComponent = (TranslatableComponent) baseComponent;
            if (translatableComponent.getWith() != null) {
                translatableComponent.setWith(replaceExtra(translatableComponent.getWith(), user, filter));
            }
        }

        HoverEvent hoverEvent = baseComponent.getHoverEvent();
        if (hoverEvent != null) {
            replaceHoverEvent(hoverEvent, user, filter);
        }

        if (baseComponent.getExtra() != null) {
            baseComponent.setExtra(replaceExtra(baseComponent.getExtra(), user, filter));
        }
        return baseComponent;
    }

    public void replaceHoverEvent(@Nonnull HoverEvent hoverEvent, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter) {
        Validate.notNull(hoverEvent, "HoverEvent cannot be null");
        Validate.notNull(user, "user cannot be null");
        Validate.notNull(filter, "Filter cannot be null");

        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() > 15) {
            List<Content> contents = hoverEvent.getContents();
            for (int i = 0; i < contents.size(); i++) {
                Content content = contents.get(i);
                if (content instanceof Text) {
                    Object object = ((Text) content).getValue();
                    Text result;
                    if (object instanceof BaseComponent[]) {
                        result = new Text(getReplacedComponents((BaseComponent[]) object, user, filter));
                    } else {
                        result = new Text(getReplacedString((String) object, user, filter));
                    }
                    contents.set(i, result);
                } else if (content instanceof Item) {
                    boolean edited = false;
                    Item itemContent = (Item) content;
                    ItemTag tag = itemContent.getTag();
                    if (tag != null) {
                        String nbt = tag.getNbt();
                        JsonElement element = new JsonParser().parse(nbt);
                        JsonObject root = element.getAsJsonObject();
                        JsonObject display = root.getAsJsonObject("display");

                        String diaplayNameJson = display.getAsJsonPrimitive("Name").toString();
                        diaplayNameJson = diaplayNameJson.substring(1, diaplayNameJson.length() - 1);
                        if (diaplayNameJson != null) {
                            BaseComponent[] parse = getReplacedComponents(ComponentSerializer.parse(StringEscapeUtils.unescapeJson(diaplayNameJson)), user, filter);
                            String result = ComponentSerializer.toString(parse);
                            display.add("Name", new JsonParser().parse("'" + result + "'"));
                            edited = true;
                        }

                        JsonArray lore = display.getAsJsonArray("Lore");
                        if (lore != null) {
                            for (int i1 = 0; i1 < lore.size(); i1++) {
                                String loreJson = lore.get(i1).getAsJsonPrimitive().toString();
                                loreJson = loreJson.substring(1, loreJson.length() - 1);
                                BaseComponent[] parse = getReplacedComponents(ComponentSerializer.parse(StringEscapeUtils.unescapeJson(loreJson)), user, filter);
                                String result = ComponentSerializer.toString(parse);
                                lore.set(i1, new JsonParser().parse("'" + result + "'"));
                            }
                            display.add("Lore", lore);
                            edited = true;
                        }

                        if (edited) {
                            JsonObject skullOwner = root.getAsJsonObject("SkullOwner");
                            if (skullOwner != null) {
                                JsonArray id = skullOwner.getAsJsonArray("Id");
                                if (id != null && id.size() == 5) {
                                    id.set(0, new JsonPrimitive(Integer.valueOf(id.get(1).getAsJsonPrimitive().toString())));
                                    id.set(1, id.get(2));
                                    id.set(2, id.get(3));
                                    id.remove(3);
                                }
                            }
                            contents.set(i, new Item(itemContent.getId(), itemContent.getCount(), ItemTag.ofNbt(element.toString())));
                        }
                    }
                } else if (content instanceof Entity) {
                    Entity entityContent = (Entity) content;
                    entityContent.setName(getReplacedComponent(entityContent.getName(), user, filter));
                    contents.set(i, entityContent);
                }
            }
        } else {
            getReplacedComponents(hoverEvent.getValue(), user, filter);
        }
    }

    public List<BaseComponent> replaceExtra(@Nonnull List<BaseComponent> extra, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter) {
        Validate.notNull(extra, "BaseComponent List cannot be null");
        Validate.notNull(user, "user cannot be null");
        Validate.notNull(filter, "Filter cannot be null");
        for (BaseComponent extraComponent : extra) {
            getReplacedComponent(extraComponent, user, filter);
        }
        return extra;
    }

    @Nonnull
    public String getReplacedString(@Nonnull String string, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter) {
        return getReplacedString(string, user, filter, true);
    }

    @Nonnull
    public String getReplacedString(@Nonnull String string, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter, boolean setPlaceholders) {
        Validate.notNull(string, "String cannot be null");
        Validate.notNull(user, "user cannot be null");
        Validate.notNull(filter, "Filter cannot be null");

        String result = string;
        for (ReplacerConfig replacerConfig : replacerConfigList) {
            if (replacerConfig.isEnable() && filter.test(replacerConfig, user)) {
                result = getFileReplacedString(user, result, replacerConfig, ReplacesMode.COMMON, setPlaceholders);
            }
        }
        return result;
    }

    @Nonnull
    public String getReplacedJson(@Nonnull String json, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter) {
        return getReplacedJson(json, user, filter, true);
    }

    @Nonnull
    public String getReplacedJson(@Nonnull String json, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter, boolean setPlaceholders) {
        Validate.notNull(json, "Json String cannot be null");
        Validate.notNull(user, "user cannot be null");
        Validate.notNull(filter, "Filter cannot be null");

        String result = json;
        for (ReplacerConfig replacerConfig : replacerConfigList) {
            if (replacerConfig.isEnable() && filter.test(replacerConfig, user)) {
                result = getFileReplacedString(user, result, replacerConfig, ReplacesMode.JSON, setPlaceholders);
            }
        }
        return result;
    }

    @SuppressWarnings("UnusedReturnValue")
    @Nonnull
    public ItemStack getReplacedItemStack(@Nonnull ItemStack itemStack, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter) {
        Validate.notNull(itemStack, "itemstack cannot be null");
        if (itemStack.hasItemMeta()) {
            itemStack.setItemMeta(getReplacedItemMeta(itemStack.getItemMeta(), user, filter));
        }
        return itemStack;
    }

    @SuppressWarnings("UnusedReturnValue")
    @Nonnull
    public ItemMeta getReplacedItemMeta(@Nonnull ItemMeta itemMeta, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter) {
        Validate.notNull(itemMeta, "ItemMeta cannot be null");
        Validate.notNull(user, "user cannot be null");
        Validate.notNull(filter, "Filter cannot be null");

        boolean hasPlaceholder = false;
        ItemMeta result = itemMeta;
        ItemMetaCache metaCache = replacedItemCache.get(result);
        if (metaCache != null) {
            metaCache.lastAccessTime = System.currentTimeMillis();
            result = metaCache.replacedItemMeta;
            hasPlaceholder = metaCache.hasPlaceholder;
        } else {
            ItemMeta original = result.clone();
            String replaced;
            for (ReplacerConfig replacerConfig : replacerConfigList) {
                if (replacerConfig.isEnable() && filter.test(replacerConfig, user)) {
                    if (result.hasDisplayName()) {
                        replaced = getFileReplacedString(user, result.getDisplayName(), replacerConfig, ReplacesMode.COMMON, false);
                        result.setDisplayName(replaced);
                        hasPlaceholder = hasPlaceholder || hasPlaceholder(replaced);
                    }

                    if (result.hasLore()) {
                        List<String> lore = result.getLore();
                        for (int i = 0; i < lore.size(); i++) {
                            replaced = getFileReplacedString(user, lore.get(i), replacerConfig, ReplacesMode.COMMON, false);
                            lore.set(i, replaced);
                            hasPlaceholder = hasPlaceholder || hasPlaceholder(replaced);
                        }
                        result.setLore(lore);
                    }
                }
            }
            if (result instanceof BookMeta) {
                hasPlaceholder = hasPlaceholder | replaceBookMeta((BookMeta) result, user, filter, hasPlaceholder);

            }
            replacedItemCache.put(original, new ItemMetaCache(result, System.currentTimeMillis(), hasPlaceholder));
        }

        return hasPlaceholder? updatePlaceholders(user, result) : result;
    }

    private boolean replaceBookMeta(@Nonnull BookMeta bookMeta, @Nonnull User user, @Nonnull BiPredicate<ReplacerConfig, User> filter, boolean placeholder) {
        boolean hasPlaceholder = placeholder;
        String replaced;
        if (bookMeta.hasAuthor()) {
            replaced = getReplacedString(bookMeta.getAuthor(), user, filter, false);
            bookMeta.setAuthor(replaced);
            hasPlaceholder = hasPlaceholder || hasPlaceholder(replaced);
        }
        if (bookMeta.hasTitle()) {
            replaced = getReplacedString(bookMeta.getTitle(), user, filter, false);
            bookMeta.setTitle(replaced);
            hasPlaceholder = hasPlaceholder || hasPlaceholder(replaced);
        }
        if (bookMeta.hasPages()) {
            if (ProtocolStringReplacer.getInstance().getServerMajorVersion() > 11) {
                List<BaseComponent[]> pages = new ArrayList<>(bookMeta.spigot().getPages());
                for (int i = 0; i < pages.size(); i++) {
                    BaseComponent[] result = ComponentSerializer.parse(
                            getReplacedJson(ComponentSerializer.toString(pages.get(i)), user, filter, false)
                    );
                    result = getReplacedComponents(result, user, filter, false);
                    pages.set(i, result);
                    hasPlaceholder = hasPlaceholder || hasPlaceholder(result);
                }
                bookMeta.spigot().setPages(pages);

            } else {
                List<String> pages = new ArrayList<>(bookMeta.getPages());
                for (int i = 0; i < pages.size(); i++) {
                    replaced = getReplacedString(pages.get(i), user, filter, false);
                    pages.set(i, replaced);
                    hasPlaceholder = hasPlaceholder || hasPlaceholder(replaced);
                }
                bookMeta.setPages(pages);
            }
        }
        return hasPlaceholder;
    }

    public void saveReplacerConfigs() {
        for (ReplacerConfig replacerConfig : replacerConfigList) {
            if (replacerConfig.isEdited()) {
                replacerConfig.saveConfig();
            }
        }
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

    public boolean hasPlaceholder(@NotNull BaseComponent[] baseComponents) {
        for (BaseComponent baseComponent : baseComponents) {
            if (baseComponent instanceof TextComponent) {
                String text = ((TextComponent) baseComponent).getText();
                boolean headFound = false;
                for(int i = 0; i < text.length(); i++) {
                    char Char = text.charAt(i);
                    if (!headFound) {
                        if (Char == papihead) {
                            headFound = true;
                        }
                    } else {
                        if (Char == papitail) {
                            return true;
                        }
                    }
                }
            } else if (baseComponent instanceof TranslatableComponent && hasPlaceholder(((TranslatableComponent) baseComponent).getWith().toArray(new BaseComponent[0]))) {
                return true;
            }
            if (baseComponent.getExtra() != null && hasPlaceholder(baseComponent.getExtra().toArray(new BaseComponent[0]))) {
                return true;
            }
        }
        return false;
    }

    public String setPlaceholder(@NotNull User user, @NotNull String string) {
        return papiReplacer.apply(string, user.getPlayer(),
                PlaceholderAPIPlugin.getInstance().getLocalExpansionManager()::getExpansion);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    private String getFileReplacedString(@Nonnull User user, @Nonnull String string, @Nonnull ReplacerConfig replacerConfig, @Nonnull ReplacesMode replacesMode, boolean setPlaceholders) {
        Validate.notNull(user, "user cannot be null");
        Validate.notNull(string, "String cannot be null");
        Validate.notNull(replacerConfig, "Replacer File cannot be null");
        Validate.notNull(replacesMode, "Replaces Type cannot be null");

        String result = string;
        if (replacerConfig.getMatchType() == ReplacerConfig.MatchType.CONTAIN) {
            result = StringUtils.replaceEachRepeatedly(result, replacerConfig.getSearchList(replacesMode), replacerConfig.getReplacementList(replacesMode));
        } else if (replacerConfig.getMatchType() == ReplacerConfig.MatchType.EQUAL) {
            Set<Map.Entry<String, String>> set = replacerConfig.getReplaces(replacesMode).entrySet();
            for (Map.Entry<String, String> entry : set) {
                if (result.equals(entry.getKey())) {
                    result = entry.getValue();
                }
            }
        } else if (replacerConfig.getMatchType() == ReplacerConfig.MatchType.REGEX) {
            Set<Map.Entry<Pattern, String>> set = replacerConfig.getReplaces(replacesMode).entrySet();
            for (Map.Entry<Pattern, String> entry : set) {
                result = entry.getKey().matcher(result).replaceAll(entry.getValue());
            }
        }
        return setPlaceholders && hasPlaceholder(result)? setPlaceholder(user, result) : result;
    }

    private ItemMeta updatePlaceholders(@Nonnull User user, @Nonnull ItemMeta itemMeta) {
        Validate.notNull(user, "user cannot be null");
        Validate.notNull(itemMeta, "ItemMeta cannot be null");

        ItemMeta result = itemMeta.clone();
        if (hasPlaceholder(result.getDisplayName())) {
            result.setDisplayName(setPlaceholder(user, result.getDisplayName()));
        }
        if (result.hasLore()) {
            List<String> lore = result.getLore();
            for (int i = 0; i < lore.size(); i++) {
                if (hasPlaceholder(lore.get(i))) {
                    lore.set(i, setPlaceholder(user, lore.get(i)));
                }
            }
            result.setLore(lore);
        }
        if (result instanceof BookMeta) {
            updatePlaceholders(user, (BookMeta) result);
        }
        return result;
    }

    private void updatePlaceholders(@Nonnull User user, @Nonnull BookMeta bookMeta) {
        if (bookMeta.hasAuthor() && hasPlaceholder(bookMeta.getAuthor())) {
            bookMeta.setAuthor(setPlaceholder(user, bookMeta.getAuthor()));
        }
        if (bookMeta.hasTitle() && hasPlaceholder(bookMeta.getTitle())) {
            bookMeta.setTitle(setPlaceholder(user, bookMeta.getTitle()));
        }
        if (bookMeta.hasPages()) {
            if (ProtocolStringReplacer.getInstance().getServerMajorVersion() > 11) {
                List<BaseComponent[]> pages = new ArrayList<>(bookMeta.spigot().getPages());
                for (int i = 0; i < pages.size(); i++) {
                    pages.set(i, updatePlaceholders(user, pages.get(i)));
                }
                bookMeta.spigot().setPages(pages);
            } else {
                List<String> pages = new ArrayList<>(bookMeta.getPages());
                for (int i = 0; i < pages.size(); i++) {
                    if (hasPlaceholder(pages.get(i))) {
                        pages.set(i, setPlaceholder(user, pages.get(i)));
                    }
                }
                bookMeta.setPages(pages);
            }
        }
    }

    private BaseComponent[] updatePlaceholders(@Nonnull User user, @Nonnull BaseComponent[] baseComponents) {
        for (int i = 0; i < baseComponents.length; i++) {
            BaseComponent baseComponent = baseComponents[i];
            if (baseComponent instanceof TextComponent) {
                TextComponent textComponent = (TextComponent) baseComponent;
                String text = textComponent.getText();
                if (hasPlaceholder(text)) {
                    textComponent.setText(setPlaceholder(user, text));
                }
            } else if (baseComponent instanceof TranslatableComponent) {
                TranslatableComponent translatableComponent = (TranslatableComponent) baseComponent;
                translatableComponent.setWith(Arrays.asList
                        (updatePlaceholders(user, translatableComponent.getWith().toArray(new BaseComponent[0]))));
            }
            if (baseComponent.getExtra() != null) {
                baseComponent.setExtra(Arrays.asList
                        (updatePlaceholders(user, baseComponent.getExtra().toArray(new BaseComponent[0]))));
            }
            if (baseComponent.getHoverEvent() != null) {
                updatePlaceholders(user, baseComponent.getHoverEvent());
            }
            baseComponents[i] = baseComponent;
        }
        return baseComponents;
    }

    private void updatePlaceholders(@Nonnull User user, @Nonnull HoverEvent hoverEvent) {
        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() > 12) {
            List<Content> contents = hoverEvent.getContents();
            for (int i = 0; i < contents.size(); i++) {
                Content content = contents.get(i);
                if (content instanceof Text) {
                    Object object = ((Text) content).getValue();
                    Text result;
                    if (object instanceof BaseComponent[]) {
                        result = new Text(updatePlaceholders(user, (BaseComponent[]) object));
                    } else {
                        if (hasPlaceholder((String) object)) {
                            result = new Text(setPlaceholder(user, (String) object));
                        } else {
                            continue;
                        }
                    }
                    contents.set(i, result);
                } else if (content instanceof Item) {
                    boolean edited = false;
                    Item itemContent = (Item) content;
                    ItemTag tag = itemContent.getTag();
                    if (tag != null) {
                        String nbt = tag.getNbt();
                        JsonElement element = new JsonParser().parse(nbt);
                        JsonObject root = element.getAsJsonObject();
                        JsonObject display = root.getAsJsonObject("display");

                        String diaplayNameJson = display.getAsJsonPrimitive("Name").toString();
                        diaplayNameJson = diaplayNameJson.substring(1, diaplayNameJson.length() - 1);
                        if (diaplayNameJson != null) {
                            BaseComponent[] parse = updatePlaceholders(user, ComponentSerializer.parse(StringEscapeUtils.unescapeJson(diaplayNameJson)));
                            String result = ComponentSerializer.toString(parse);
                            display.add("Name", new JsonParser().parse("'" + result + "'"));
                            edited = true;
                        }

                        JsonArray lore = display.getAsJsonArray("Lore");
                        if (lore != null) {
                            for (int i1 = 0; i1 < lore.size(); i1++) {
                                String loreJson = lore.get(i1).getAsJsonPrimitive().toString();
                                loreJson = loreJson.substring(1, loreJson.length() - 1);
                                BaseComponent[] parse = updatePlaceholders(user, ComponentSerializer.parse(StringEscapeUtils.unescapeJson(loreJson)));
                                String result = ComponentSerializer.toString(parse);
                                lore.set(i1, new JsonParser().parse("'" + result + "'"));
                            }
                            display.add("Lore", lore);
                            edited = true;
                        }

                        if (edited) {
                            JsonObject skullOwner = root.getAsJsonObject("SkullOwner");
                            if (skullOwner != null) {
                                JsonArray id = skullOwner.getAsJsonArray("Id");
                                if (id != null && id.size() == 5) {
                                    id.set(0, new JsonPrimitive(Integer.valueOf(id.get(1).getAsJsonPrimitive().toString())));
                                    id.set(1, id.get(2));
                                    id.set(2, id.get(3));
                                    id.remove(3);
                                }
                            }
                            contents.set(i, new Item(itemContent.getId(), itemContent.getCount(), ItemTag.ofNbt(element.toString())));
                        }
                    }
                } else if (content instanceof Entity) {
                    Entity entityContent = (Entity) content;
                    BaseComponent nameComponent = entityContent.getName();
                    if (nameComponent instanceof TextComponent) {
                        TextComponent textComponent = (TextComponent) nameComponent;
                        if (hasPlaceholder(textComponent.getText())) {
                            textComponent.setText(setPlaceholder(user, textComponent.getText()));
                        }
                    } else if (nameComponent instanceof TranslatableComponent) {
                        TranslatableComponent translatableComponent = (TranslatableComponent) nameComponent;
                        translatableComponent.setWith(Arrays.asList(updatePlaceholders(user,
                                translatableComponent.getWith().toArray(new BaseComponent[0])
                        )));
                    }
                    if (nameComponent.getExtra() != null) {
                        nameComponent.setExtra(Arrays.asList(updatePlaceholders(user,
                                nameComponent.getExtra().toArray(new BaseComponent[0])
                        )));
                    }
                    entityContent.setName(nameComponent);
                    contents.set(i, entityContent);
                }
            }
        } else {
            updatePlaceholders(user, hoverEvent.getValue());
        }
    }

}
