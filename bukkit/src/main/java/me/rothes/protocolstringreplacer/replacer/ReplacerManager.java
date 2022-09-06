package me.rothes.protocolstringreplacer.replacer;

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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.neosearch.stringsearcher.Emit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
    private LinkedList<ReplacerConfig> replacerConfigList = new LinkedList<>();
    private HashMap<ItemMeta, ItemMetaCache> replacedItemCache = new HashMap<>();
    private BukkitTask cleanTask;

    public static class ItemMetaCache {

        private ItemMeta replacedItemMeta;
        private Long lastAccessTime;
        private Boolean blocked;
        private List<Integer> placeholderIndexes;

        public ItemMetaCache(ItemMeta replacedItemMeta, @Nonnull Long lastAccessTime,
                             @Nonnull Boolean blocked, @Nonnull List<Integer> placeholderIndexes) {
            Validate.notNull(lastAccessTime, "Last Access Time cannot be null");
            Validate.notNull(placeholderIndexes, "List cannot be null");
            this.replacedItemMeta = replacedItemMeta;
            this.lastAccessTime = lastAccessTime;
            this.blocked = blocked;
            this.placeholderIndexes = placeholderIndexes;
        }

        public ItemMeta getReplacedItemMeta() {
            return replacedItemMeta;
        }

        public Long getLastAccessTime() {
            return lastAccessTime;
        }

        public Boolean isBlocked() {
            return blocked;
        }

        public List<Integer> getPlaceholderIndexes() {
            return placeholderIndexes;
        }

        public void setPlaceholderIndexes(@Nonnull List<Integer> placeholderIndexes) {
            Validate.notNull(placeholderIndexes, "List cannot be null");
            this.placeholderIndexes = placeholderIndexes;
        }

        public void setLastAccessTime(long lastAccessTime) {
            this.lastAccessTime = lastAccessTime;
        }

        public void setBlocked(@Nonnull Boolean blocked) {
            Validate.notNull(blocked, "Boolean cannot be null");
            this.blocked = blocked;
        }

    }

    @Nonnull
    public static HashMap<File, CommentYamlConfiguration> loadReplacesFiles(@Nonnull File path) {
        Validate.notNull(path, "Path cannot be null");
        HashMap<File, CommentYamlConfiguration> loaded = new HashMap<>();
        List<File> files = FileUtils.getFolderFiles(path, true, ".yml");
        for (File file : files) {
            loaded.put(file, CommentYamlConfiguration.loadConfiguration(file));
        }
        return loaded;
    }

    public BukkitTask getCleanTask() {
        return cleanTask;
    }

    public void initialize() {
        this.papiReplacer = new PAPIReplacer();
        papiHead = papiReplacer.getHead();
        papiTail = papiReplacer.getTail();

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
            } catch (PatternSyntaxException exception) {
                exception.printStackTrace();
                ProtocolStringReplacer.error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Replacer-Config.Replacer-Regex-Exception",
                        file.getAbsolutePath().substring((ProtocolStringReplacer.getInstance().getDataFolder().getAbsolutePath() + "\\").length()).replace('\\', '/')));
            }
        }

        // To warm up the lambda below.
        replacedItemCache.put(null, new ItemMetaCache(null, 1L, false, new ArrayList<>()));

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
                    ProtocolStringReplacer.info(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Schedule.Purging-Item-Cache",
                            String.valueOf(needToRemove.size())));
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
    public ItemMetaCache getReplacedItemCache(ItemMeta itemMeta) {
        return replacedItemCache.get(itemMeta);
    }

    public ItemMetaCache addReplacedItemCache(ItemMeta original, @NotNull ItemMeta replaced, @NotNull Boolean blocked, @NotNull List<Integer> papiIndexes) {
        Validate.notNull(replaced, "Replaced ItemMeta cannot be null");

        ItemMetaCache itemMetaCache = new ItemMetaCache(replaced, System.currentTimeMillis(), blocked, papiIndexes);
        replacedItemCache.put(original, itemMetaCache);
        return itemMetaCache;
    }

    public List<ReplacerConfig> getAcceptedReplacers(@Nonnull PsrUser user, @Nonnull BiPredicate<ReplacerConfig, PsrUser> filter) {
        Validate.notNull(user, "PsrUser cannot be null");
        Validate.notNull(filter, "BiPredicate Filter cannot be null");

        List<ReplacerConfig> result = new ArrayList<>();
        for (ReplacerConfig replacerConfig : replacerConfigList) {
            if (filter.test(replacerConfig, user)) {
                result.add(replacerConfig);
            }
        }
        return result;
    }

    public boolean isJsonBlocked(@Nonnull Container<?> container, @Nonnull List<ReplacerConfig> replacerConfigList) {
        Validate.notNull(container, "Container cannot be null");
        Validate.notNull(replacerConfigList, "List cannot be null");

        boolean blocked = false;
        for (Replaceable replaceable : container.getJsons()) {
            String json = replaceable.getText();
            if (json.isEmpty()) {
                continue;
            }
            for (ReplacerConfig replacerConfig : replacerConfigList) {
                blocked = getBlocked(json, replacerConfig, ReplaceMode.JSON);
                if (blocked) {
                    return true;
                }
            }
        }
        return blocked;
    }

    public boolean isTextBlocked(@Nonnull Container<?> container, @Nonnull List<ReplacerConfig> replacerConfigList) {
        Validate.notNull(container, "Container cannot be null");
        Validate.notNull(replacerConfigList, "List cannot be null");

        boolean blocked = false;
        for (Replaceable replaceable : container.getTexts()) {
            String text = replaceable.getText();
            if (text.isEmpty()) {
                continue;
            }
            for (ReplacerConfig replacerConfig : replacerConfigList) {
                blocked = getBlocked(text, replacerConfig, ReplaceMode.COMMON);
                if (blocked) {
                    return true;
                }
            }
        }
        return blocked;
    }

    public void replaceContainerJsons(@Nonnull Container<?> container, @Nonnull List<ReplacerConfig> replacerConfigList) {
        Validate.notNull(container, "Container cannot be null");
        Validate.notNull(replacerConfigList, "List cannot be null");

        for (Replaceable replaceable : container.getJsons()) {
            String json = replaceable.getText();
            if (json.isEmpty()) {
                continue;
            }
            for (ReplacerConfig replacerConfig : replacerConfigList) {
                json = getReplaced(json, replacerConfig, ReplaceMode.JSON);
            }
            replaceable.setText(json);
        }
    }

    public void replaceContainerTexts(@Nonnull Container<?> container, @Nonnull List<ReplacerConfig> replacerConfigList) {
        Validate.notNull(container, "Container cannot be null");
        Validate.notNull(replacerConfigList, "List cannot be null");

        for (Replaceable replaceable : container.getTexts()) {
            String text = replaceable.getText();
            if (text.isEmpty()) {
                continue;
            }
            for (ReplacerConfig replacerConfig : replacerConfigList) {
                text = getReplaced(text, replacerConfig, ReplaceMode.COMMON);
            }
            replaceable.setText(text);
        }
    }

    public void setPapi(@Nonnull PsrUser user, @Nonnull List<Replaceable> replaceables) {
        setPapi(user, replaceables, getPapiIndexes(replaceables));
    }

    public void setPapi(@Nonnull PsrUser user, @Nonnull List<Replaceable> replaceables, List<Integer> indexes) {
        Validate.notNull(user, "PsrUser cannot be null");
        Validate.notNull(replaceables, "List cannot be null");
        Validate.notNull(indexes, "List cannot be null");

        if (indexes.isEmpty()) {
            return;
        }
        for (int i : indexes) {
            Replaceable replaceable = replaceables.get(i);
            replaceable.setText(setPlaceholder(user, replaceable.getText()));
        }
    }

    public List<Integer> getPapiIndexes(@Nonnull List<Replaceable> replaceables) {
        Validate.notNull(replaceables, "List cannot be null");

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

    @SuppressWarnings("unchecked")
    @Nonnull
    private String getReplaced(@Nonnull String string, @Nonnull ReplacerConfig replacerConfig, @Nonnull ReplaceMode replaceMode) {
        Validate.notNull(string, "String cannot be null");
        Validate.notNull(replacerConfig, "Replacer File cannot be null");
        Validate.notNull(replaceMode, "Replaces Mode cannot be null");

        String result = string;

        if (replacerConfig.getMatchMode() == MatchMode.CONTAIN) {
            // Using Aho-Corasick algorithm.
            int i = 0;

            StringBuilder resultBuilder = new StringBuilder();
            for (Emit<String> emit : replacerConfig.getReplacesStringSearcher(replaceMode).parseText(string)) {
                if (emit.getStart() > i) {
                    resultBuilder.append(string.subSequence(i, emit.getStart()));
                }
                resultBuilder.append(replacerConfig.getReplaces(replaceMode).get(emit.getSearchString()));
                i = emit.getEnd() + 1;
            }

            if (i < string.length()) {
                resultBuilder.append(string.subSequence(i, string.length()));
            }

            result = resultBuilder.toString();
        } else if (replacerConfig.getMatchMode() == MatchMode.EQUAL) {
            Object get = replacerConfig.getReplaces(replaceMode).get(string);
            if (get != null) {
                result = (String) get;
            }
        } else if (replacerConfig.getMatchMode() == MatchMode.REGEX) {
            Set<Map.Entry<Pattern, String>> set = replacerConfig.getReplaces(replaceMode).entrySet();
            for (Map.Entry<Pattern, String> entry : set) {
                result = entry.getKey().matcher(result).replaceAll(entry.getValue());
            }
        }
        return result;
    }

    @Nonnull
    private boolean getBlocked(@Nonnull String string, @Nonnull ReplacerConfig replacerConfig, @Nonnull ReplaceMode replaceMode) {
        Validate.notNull(string, "String cannot be null");
        Validate.notNull(replacerConfig, "Replacer File cannot be null");
        Validate.notNull(replaceMode, "Replaces Mode cannot be null");

        if (replacerConfig.getMatchMode() == MatchMode.CONTAIN) {
            return replacerConfig.getBlocksStringSearcher(replaceMode).parseText(string).size() > 0;

        } else if (replacerConfig.getMatchMode() == MatchMode.EQUAL) {
            return replacerConfig.getBlocks(replaceMode).contains(string);

        } else if (replacerConfig.getMatchMode() == MatchMode.REGEX) {
            List<Object> blocks = replacerConfig.getBlocks(replaceMode);
            for (Object patternObject : blocks) {
                Pattern pattern = (Pattern) patternObject;
                if (pattern.matcher(string).find()) {
                    return true;
                }
            }
        }
        return false;
    }

}
