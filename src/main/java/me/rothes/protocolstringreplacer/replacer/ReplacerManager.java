package me.rothes.protocolstringreplacer.replacer;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.rothes.protocolstringreplacer.PsrLocalization;
import me.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import me.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.containers.Container;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.containers.Replaceable;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.rothes.protocolstringreplacer.utils.FileUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.neosearch.stringsearcher.Emit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ReplacerManager {

    private PAPIReplacer papiReplacer;
    private char papiHead;
    private char papiTail;
    private final LinkedList<ReplacerConfig> replacerConfigList = new LinkedList<>();
    private final EnumMap<Material, HashMap<ItemMeta, ItemMetaCache>> replacedItemCache = new EnumMap<>(Material.class);
    private BukkitTask cleanTask;

    public static class ItemMetaCache {

        private final NBTItem nbtItem;
        private long lastAccessTime;
        private boolean blocked;
        private int[] placeholderIndexes;

        public ItemMetaCache(NBTItem nbtItem, long lastAccessTime, boolean blocked, int[] placeholderIndexes) {
            this.nbtItem = nbtItem;
            this.lastAccessTime = lastAccessTime;
            this.blocked = blocked;
            this.placeholderIndexes = placeholderIndexes;
        }

        public NBTItem getNbtItem() {
            return nbtItem;
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }

        public boolean isBlocked() {
            return blocked;
        }

        public int[] getPlaceholderIndexes() {
            return placeholderIndexes;
        }

        public void setPlaceholderIndexes(int[] placeholderIndexes) {
            this.placeholderIndexes = placeholderIndexes;
        }

        public void setLastAccessTime(long lastAccessTime) {
            this.lastAccessTime = lastAccessTime;
        }

        public void setBlocked(boolean blocked) {
            this.blocked = blocked;
        }

    }

    @Nonnull
    public static HashMap<File, CommentYamlConfiguration> loadReplacesFiles(@Nonnull File path) {
        Validate.notNull(path, "Path cannot be null");
        HashMap<File, CommentYamlConfiguration> loaded = new HashMap<>();
        List<File> files = FileUtils.getFolderFiles(path, true, ".yml");
        for (File file : files) {
            try {
                CommentYamlConfiguration config = new CommentYamlConfiguration();
                config.load(file);
                loaded.put(file, config);
            } catch (Throwable e) {
                ProtocolStringReplacer.warn(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Replacer-Config.Replacer-Failed-To-Load",
                        file.getAbsolutePath().substring((ProtocolStringReplacer.getInstance().getDataFolder().getAbsolutePath() + "\\").length()).replace('\\', '/')), e);
            }
        }
        return loaded;
    }

    public BukkitTask getCleanTask() {
        return cleanTask;
    }

    public void cancelCleanTask() {
        if (getCleanTask() != null) {
            getCleanTask().cancel();
        }
    }

    public void registerTask() {
        ProtocolStringReplacer instance = ProtocolStringReplacer.getInstance();
        long cleanAccessInterval = instance.getConfigManager().cleanAccessInterval;
        long cleanTaskInterval = instance.getConfigManager().cleanTaskInterval;
        cleanTask = Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            long currentTime = System.currentTimeMillis();
            int purged = 0;

            List<ItemMeta> needToRemove = new ArrayList<>();
            for (Map.Entry<Material, HashMap<ItemMeta, ItemMetaCache>> outEntry : replacedItemCache.entrySet()) {
                needToRemove.clear();
                HashMap<ItemMeta, ItemMetaCache> map = outEntry.getValue();
                for (Map.Entry<ItemMeta, ItemMetaCache> entry : map.entrySet()) {
                    if ((currentTime - entry.getValue().lastAccessTime) > cleanAccessInterval) {
                        needToRemove.add(entry.getKey());
                    }
                }
                if (!needToRemove.isEmpty()) {
                    for (ItemMeta itemMeta : needToRemove) {
                        map.remove(itemMeta);
                        purged++;
                    }
                }

            }
            if (purged != 0) {
                int finalPurged = purged;
                Bukkit.getScheduler().runTask(instance, () -> ProtocolStringReplacer.info(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Schedule.Purging-Item-Cache",
                        String.valueOf(finalPurged))));
            }
        }, 0L, cleanTaskInterval);
    }

    public void initialize() {
        if (ProtocolStringReplacer.getInstance().getConfigManager().placeholderEnabled) {
            this.papiReplacer = new PAPIReplacer();
            papiHead = papiReplacer.getHead();
            papiTail = papiReplacer.getTail();
        }

        File path = new File(ProtocolStringReplacer.getInstance().getDataFolder() + "/Replacers");
        long startTime = System.nanoTime();
        HashMap<File, CommentYamlConfiguration> loadedFiles = loadReplacesFiles(path);
        ProtocolStringReplacer.info(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Replacer-Config.Pre-Loaded-Replacers",
                String.valueOf(loadedFiles.size()), String.valueOf((System.nanoTime() - startTime) / 1000000D)));
        if (loadedFiles.size() == 0) {
            return;
        }
        for (Map.Entry<File, CommentYamlConfiguration> entry : loadedFiles.entrySet()) {
            File file = entry.getKey();
            CommentYamlConfiguration config = entry.getValue();
            try {
                FileReplacerConfig replacerConfig = new FileReplacerConfig(file, config);
                addReplacerConfig(replacerConfig);
            } catch (PatternSyntaxException ex) {
                ProtocolStringReplacer.error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Replacer-Config.Replacer-Regex-Exception",
                        file.getAbsolutePath().substring((ProtocolStringReplacer.getInstance().getDataFolder().getAbsolutePath() + "\\").length())
                                .replace('\\', '/')), ex);
            } catch (Throwable throwable) {
                ProtocolStringReplacer.error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Replacer-Config.Replacer-Throw-Exception",
                        file.getAbsolutePath().substring((ProtocolStringReplacer.getInstance().getDataFolder().getAbsolutePath() + "\\").length())
                                .replace('\\', '/')), throwable);
            }
        }
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

    public List<ReplacerConfig> getReplacerConfigList() {
        return new ArrayList<>(replacerConfigList);
    }

    public int getReplacesCount() {
        int count = 0;
        for (ReplacerConfig replacerConfig : replacerConfigList) {
            if (replacerConfig.isEnabled()) {
                count = count + replacerConfig.getReplaces(ReplaceMode.COMMON).size();
            }
        }
        return count;
    }

    public void saveReplacerConfigs() {
        for (ReplacerConfig replacerConfig : replacerConfigList) {
            if (replacerConfig.isEdited()) {
                replacerConfig.saveConfig();
            }
        }
    }

    @Nullable
    public ItemMetaCache getReplacedItemCache(ItemMeta itemMeta, Material material) {
        HashMap<ItemMeta, ItemMetaCache> map = replacedItemCache.get(material);
        return map == null ? null : map.get(itemMeta);
    }

    public ItemMetaCache addReplacedItemCache(ItemMeta original, @NotNull NBTItem nbtItem,
                                              @NotNull Material type, boolean blocked, int[] papiIndexes) {
        Validate.notNull(nbtItem, "Replaced NBTItem cannot be null");

        ItemMetaCache itemMetaCache = new ItemMetaCache(nbtItem, System.currentTimeMillis(), blocked, papiIndexes);
        replacedItemCache.computeIfAbsent(type, key -> new HashMap<>()).put(original, itemMetaCache);
        return itemMetaCache;
    }

    public List<ReplacerConfig> getAcceptedReplacers(@Nonnull PsrUser user, @Nonnull BiPredicate<ReplacerConfig, PsrUser> filter) {
        Validate.notNull(user, "PsrUser cannot be null");
        Validate.notNull(filter, "BiPredicate Filter cannot be null");

        List<ReplacerConfig> result = new LinkedList<>(replacerConfigList);
        Iterator<ReplacerConfig> iterator = result.iterator();
        while (iterator.hasNext()) {
            if (!filter.test(iterator.next(), user)) {
                iterator.remove();
            }
        }
        return result;
    }

    public boolean isJsonBlocked(@Nonnull Container<?> container, @Nonnull List<ReplacerConfig> replacerConfigList) {
        Validate.notNull(container, "Container cannot be null");
        Validate.notNull(replacerConfigList, "List cannot be null");

        int length;
        int maxLength;
        String json;
        for (Replaceable replaceable : container.getJsons()) {
            json = replaceable.getText();
            if (json.isEmpty()) {
                continue;
            }
            length = json.length();
            for (ReplacerConfig replacerConfig : replacerConfigList) {
                maxLength = replacerConfig.getMaxJsonLength();
                if (maxLength != -1 && maxLength < length) {
                    continue;
                }
                if (getBlocked(json, replacerConfig, ReplaceMode.JSON)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isTextBlocked(@Nonnull Container<?> container, @Nonnull List<ReplacerConfig> replacerConfigList) {
        Validate.notNull(container, "Container cannot be null");
        Validate.notNull(replacerConfigList, "List cannot be null");

        int length;
        int maxLength;
        String text;
        for (Replaceable replaceable : container.getTexts()) {
            text = replaceable.getText();
            if (text.isEmpty()) {
                continue;
            }
            length = text.length();
            for (ReplacerConfig replacerConfig : replacerConfigList) {
                maxLength = replacerConfig.getMaxTextLength();
                if (maxLength != -1 && maxLength < length) {
                    continue;
                }
                if (getBlocked(text, replacerConfig, ReplaceMode.COMMON)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isDirectBlocked(@Nonnull String string, @Nonnull List<ReplacerConfig> replacerConfigList) {
        Validate.notNull(string, "String cannot be null");
        Validate.notNull(replacerConfigList, "List cannot be null");

        if (string.isEmpty()) {
            return false;
        }
        int length = string.length();
        int maxLength;

        for (ReplacerConfig replacerConfig : replacerConfigList) {
            maxLength = replacerConfig.getMaxDirectLength();
            if (maxLength != -1 && maxLength < length) {
                continue;
            }
            if (getBlocked(string, replacerConfig, ReplaceMode.DIRECT)) {
                return true;
            }
        }
        return false;
    }

    public void replaceContainerJsons(@Nonnull Container<?> container, @Nonnull List<ReplacerConfig> replacerConfigList) {
        Validate.notNull(container, "Container cannot be null");
        Validate.notNull(replacerConfigList, "List cannot be null");

        int length;
        int maxLength;
        String json;
        for (Replaceable replaceable : container.getJsons()) {
            json = replaceable.getText();
            if (json.isEmpty()) {
                continue;
            }
            length = json.length();
            for (ReplacerConfig replacerConfig : replacerConfigList) {
                maxLength = replacerConfig.getMaxJsonLength();
                if (maxLength != -1 && maxLength < length) {
                    continue;
                }
                json = getReplaced(json, replacerConfig, ReplaceMode.JSON);
            }
            replaceable.setText(json);
        }
    }

    public void replaceJsonReplaceable(@Nonnull Replaceable replaceable, @Nonnull List<ReplacerConfig> replacerConfigList) {
        Validate.notNull(replaceable, "Replaceable cannot be null");
        Validate.notNull(replacerConfigList, "List cannot be null");

        String json = replaceable.getText();
        if (json.isEmpty()) {
            return;
        }
        int length = json.length();
        int maxLength;
        for (ReplacerConfig replacerConfig : replacerConfigList) {
            maxLength = replacerConfig.getMaxJsonLength();
            if (maxLength != -1 && maxLength < length) {
                continue;
            }
            json = getReplaced(json, replacerConfig, ReplaceMode.JSON);
        }
        replaceable.setText(json);
    }

    public void replaceContainerTexts(@Nonnull Container<?> container, @Nonnull List<ReplacerConfig> replacerConfigList) {
        Validate.notNull(container, "Container cannot be null");
        Validate.notNull(replacerConfigList, "List cannot be null");

        int length;
        int maxLength;
        String text;
        for (Replaceable replaceable : container.getTexts()) {
            text = replaceable.getText();
            if (text.isEmpty()) {
                continue;
            }
            length = text.length();
            for (ReplacerConfig replacerConfig : replacerConfigList) {
                maxLength = replacerConfig.getMaxTextLength();
                if (maxLength != -1 && maxLength < length) {
                    continue;
                }
                text = getReplaced(text, replacerConfig, ReplaceMode.COMMON);
            }
            replaceable.setText(text);
        }
    }

    public String replaceDirect(@Nonnull String string, @Nonnull List<ReplacerConfig> replacerConfigList) {
        Validate.notNull(string, "String cannot be null");
        Validate.notNull(replacerConfigList, "List cannot be null");

        if (string.isEmpty()) {
            return string;
        }
        int length = string.length();
        int maxLength;

        String result = string;
        for (ReplacerConfig replacerConfig : replacerConfigList) {
            maxLength = replacerConfig.getMaxDirectLength();
            if (maxLength != -1 && maxLength < length) {
                continue;
            }
            result = getReplaced(result, replacerConfig, ReplaceMode.DIRECT);
        }
        return result;
    }

    public void setPapi(@Nonnull PsrUser user, @Nonnull List<Replaceable> replaceables) {
        Validate.notNull(user, "PsrUser cannot be null");
        Validate.notNull(replaceables, "List cannot be null");

        List<Integer> indexes = getPapiIndexes(replaceables);
        if (indexes.isEmpty()) {
            return;
        }
        for (int i : indexes) {
            Replaceable replaceable = replaceables.get(i);
            replaceable.setText(setPlaceholder(user, replaceable.getText()));
        }
    }

    public void setPapi(@Nonnull PsrUser user, @Nonnull List<Replaceable> replaceables, int[] indexes) {
        Validate.notNull(user, "PsrUser cannot be null");
        Validate.notNull(replaceables, "List cannot be null");
        Validate.notNull(indexes, "List cannot be null");

        for (int i : indexes) {
            Replaceable replaceable = replaceables.get(i);
            replaceable.setText(setPlaceholder(user, replaceable.getText()));
        }
    }

    public List<Integer> getPapiIndexes(@Nonnull List<Replaceable> replaceables) {
        Validate.notNull(replaceables, "List cannot be null");

        if (!ProtocolStringReplacer.getInstance().getConfigManager().placeholderEnabled) {
            return Collections.emptyList();
        }

        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < replaceables.size(); i++) {
            Replaceable replaceable = replaceables.get(i);
            if (hasPlaceholder(replaceable.getText())) {
                result.add(i);
            }
        }
        return result;
    }

    public boolean hasPlaceholder(@NotNull String string) {
        boolean headFound = false;
        boolean tailFound = false;
        for(int i = 0; i < string.length(); i++) {
            char Char = string.charAt(i);
            if (!headFound) {
                if (Char == papiHead) {
                    headFound = true;
                }
            } else {
                if (Char == papiTail) {
                    tailFound = true;
                    break;
                }
            }
        }
        return tailFound;
    }

    public String setPlaceholder(@NotNull PsrUser user, @NotNull String string) {
        return papiReplacer.apply(string, user.getPlayer(),
                PlaceholderAPIPlugin.getInstance().getLocalExpansionManager()::getExpansion);
    }

    @Nonnull
    private String getReplaced(@Nonnull String string, @Nonnull ReplacerConfig replacerConfig, @Nonnull ReplaceMode replaceMode) {
        Validate.notNull(string, "String cannot be null");
        Validate.notNull(replacerConfig, "Replacer File cannot be null");
        Validate.notNull(replaceMode, "Replaces Mode cannot be null");

        if (replacerConfig.getMatchMode() == MatchMode.CONTAIN) {
            // Using Aho-Corasick algorithm.
            int i = 0;

            StringBuilder resultBuilder = new StringBuilder();
            for (Emit<String> emit : replacerConfig.getReplacesStringSearcher(replaceMode).parseText(string)) {
                if (emit.getStart() > i) {
                    resultBuilder.append(string, i, emit.getStart());
                }
                resultBuilder.append(emit.getPayload());
                i = emit.getEnd() + 1;
            }

            if (i < string.length()) {
                resultBuilder.append(string.substring(i));
            }

            return resultBuilder.toString();

        } else if (replacerConfig.getMatchMode() == MatchMode.EQUAL) {
            Object get = replacerConfig.getReplaces(replaceMode).get(string);
            if (get != null) {
                return (String) get;
            }
            return string;
        } else if (replacerConfig.getMatchMode() == MatchMode.REGEX) {
            String result = string;

            @SuppressWarnings("unchecked")
            Set<Map.Entry<Pattern, String>> set = replacerConfig.getReplaces(replaceMode).entrySet();
            for (Map.Entry<Pattern, String> entry : set) {
                result = entry.getKey().matcher(result).replaceAll(entry.getValue());
            }
            return result;
        }
        throw new AssertionError();
    }

    public boolean getBlocked(@Nonnull String string, @Nonnull ReplacerConfig replacerConfig, @Nonnull ReplaceMode replaceMode) {
        Validate.notNull(string, "String cannot be null");
        Validate.notNull(replacerConfig, "Replacer File cannot be null");
        Validate.notNull(replaceMode, "Replaces Mode cannot be null");

        switch (replacerConfig.getMatchMode()) {
            case CONTAIN:
                return replacerConfig.getBlocksStringSearcher(replaceMode).containsMatch(string);
            case EQUAL:
                return replacerConfig.getBlocks(replaceMode).contains(string);
            case REGEX:
                List<Object> blocks = replacerConfig.getBlocks(replaceMode);
                for (Object patternObject : blocks) {
                    Pattern pattern = (Pattern) patternObject;
                    if (pattern.matcher(string).find()) {
                        return true;
                    }
                }
                break;
            default:
                throw new AssertionError();
        }
        return false;
    }

}
