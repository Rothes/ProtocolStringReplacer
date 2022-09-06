package me.rothes.protocolstringreplacer.replacer;

import me.rothes.protocolstringreplacer.PsrLocalization;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import me.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import org.apache.commons.collections.map.ListOrderedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neosearch.stringsearcher.SimpleStringSearcherBuilder;
import org.neosearch.stringsearcher.StringSearcher;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class FileReplacerConfig implements ReplacerConfig {

    private File file;
    private CommentYamlConfiguration configuration;
    private boolean enable;
    private int priority;
    private List<ListenType> listenTypeList = new ArrayList<>();
    private MatchMode matchMode;
    private HashMap<ReplaceMode, ListOrderedMap> replaces = new HashMap<>();
    private HashMap<ReplaceMode, List<Object>> blocks = new HashMap<>();
    private String author;
    private String version;
    private boolean edited;

    private HashMap<ReplaceMode, StringSearcher<String>> replacesStringSearcher = new HashMap<>();
    private HashMap<ReplaceMode, StringSearcher<String>> blocksStringSearcher = new HashMap<>();

    public FileReplacerConfig(@Nonnull File file, @Nonnull CommentYamlConfiguration configuration) {
        long startTime = System.nanoTime();
        loadData(file, configuration);
        if (ProtocolStringReplacer.getInstance().getConfigManager().printReplacer) {
            ProtocolStringReplacer.info(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Replacer-Config.Replacer-Load-Complete",
                    getRelativePath(), String.valueOf((System.nanoTime() - startTime) / 1000000d)));
        }
    }

    @Override
    public boolean isEdited() {
        return edited;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public boolean isEnabled() {
        return enable;
    }

    @Override
    public CommentYamlConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public @NotNull List<ListenType> getListenTypeList() {
        return listenTypeList;
    }

    @Override
    public @NotNull ListOrderedMap getReplaces(@Nonnull ReplaceMode replaceMode) {
        return replaces.get(replaceMode);
    }

    @Override
    public @NotNull List<Object> getBlocks(@Nonnull ReplaceMode replaceMode) {
        return blocks.get(replaceMode);
    }

    @Override
    public @Nullable String getAuthor() {
        return author;
    }

    @Override
    public @Nullable String getVersion() {
        return version;
    }

    @Override
    public @NotNull MatchMode getMatchMode() {
        return matchMode;
    }

    @Override
    public @NotNull String getRelativePath() {
        return file.getAbsolutePath().substring((ProtocolStringReplacer.getInstance().getDataFolder().getAbsolutePath() + "\\")
                .length()).replace('\\', '/');
    }

    @Override
    public @NotNull StringSearcher<String> getReplacesStringSearcher(ReplaceMode replaceMode) {
        return replacesStringSearcher.get(replaceMode);
    }

    @Override
    public @NotNull StringSearcher<String> getBlocksStringSearcher(ReplaceMode replaceMode) {
        return blocksStringSearcher.get(replaceMode);
    }

    @Override
    public void saveConfig() {
        configuration.set("Options.Enable", enable);
        configuration.set("Options.Priority", priority);
        configuration.set("Options.Author", author);
        configuration.set("Options.Version", version);
        LinkedList<String> types = new LinkedList<>();
        for (ListenType listenType : listenTypeList) {
            types.add(listenType.getName());
        }
        configuration.set("Options.Filter.Listen-Types", types);
        configuration.set("Options.Match-Mode", matchMode.getName());
        for (ReplaceMode replaceMode : ReplaceMode.values()) {
            ListOrderedMap replaces = this.replaces.get(replaceMode);
            ArrayList<ListOrderedMap> result = new ArrayList<>();
            for (short i = 0; i < replaces.size(); i++) {
                ListOrderedMap entryMap = new ListOrderedMap();
                Object object = replaces.get(i);
                entryMap.put("Original", object);
                entryMap.put("Replacement", replaces.get(object));
                result.add(entryMap);
            }
            configuration.set("Replaces." + replaceMode.getNode(), result);

            configuration.set("Blocks." + replaceMode.getNode(), blocks.get(replaceMode));
        }
        try {
            configuration.save(file);
            edited = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setReplace(int index, @Nonnull String value, @Nonnull ReplaceMode replaceMode) {
        if (index < replaces.get(replaceMode).size()) {
            replaces.get(replaceMode).setValue(index, value);
            updateStringSearcher(replaceMode);
            edited = true;
            saveConfig();
        }
    }

    public void setReplace(int index, @Nonnull String key, @Nonnull String value, @Nonnull ReplaceMode replaceMode) {
        if (index < replaces.get(replaceMode).size()) {
            removeReplace(index, replaceMode);
        }
        if (index <= replaces.get(replaceMode).size()) {
            addReplace(index, key, value, replaceMode);
        }
    }

    public void addReplace(int index, @Nonnull String key, @Nonnull String value, @Nonnull ReplaceMode replaceMode) {
        if (index <= replaces.get(replaceMode).size()) {
            if (this.matchMode == MatchMode.REGEX) {
                replaces.get(replaceMode).put(index, Pattern.compile(key), value);
            } else {
                replaces.get(replaceMode).put(index, key, value);
            }
            updateStringSearcher(replaceMode);
            edited = true;
            saveConfig();
        }
    }

    public void addReplace(@Nonnull String key, @Nonnull String value, @Nonnull ReplaceMode replaceMode) {
        addReplace(replaces.get(replaceMode).size(), key, value, replaceMode);
    }

    public void removeReplace(int index, @Nonnull ReplaceMode replaceMode) {
        replaces.get(replaceMode).remove(index);
        updateStringSearcher(replaceMode);
        edited = true;
        saveConfig();
    }

    public void setBlock(int index, @Nonnull String block, @Nonnull ReplaceMode replaceMode) {
        if (index < blocks.get(replaceMode).size()) {
            blocks.get(replaceMode).set(index, block);
            updateStringSearcher(replaceMode);
            edited = true;
            saveConfig();
        }
    }

    public void addBlock(int index, @Nonnull String block, @Nonnull ReplaceMode replaceMode) {
        if (index <= blocks.get(replaceMode).size()) {
            if (this.matchMode == MatchMode.REGEX) {
                blocks.get(replaceMode).add(index, Pattern.compile(block));
            } else {
                blocks.get(replaceMode).add(index, block);
            }
            updateStringSearcher(replaceMode);
            edited = true;
            saveConfig();
        }
    }

    public void addBlock(@Nonnull String block, @Nonnull ReplaceMode replaceMode) {
        addBlock(blocks.get(replaceMode).size(), block, replaceMode);
    }

    public void removeBlock(int index, @Nonnull ReplaceMode replaceMode) {
        blocks.get(replaceMode).remove(index);
        updateStringSearcher(replaceMode);
        edited = true;
        saveConfig();
    }

    public int checkReplaceKey(@Nonnull String key, @Nonnull ReplaceMode replaceMode) {
        for (int i = 0; i < replaces.get(replaceMode).size(); i++) {
            if (replaces.get(replaceMode).get(i).equals(key)) {
                return i;
            }
        }
        return -1;
    }

    private void loadData(File file, CommentYamlConfiguration configuration) {
        this.configuration = configuration;
        this.file = file;
        enable = configuration.getBoolean("Options.Enable", false);
        priority = configuration.getInt("Options.Priority", 5);
        author = configuration.getString("Options.Author");
        version = configuration.getString("Options.Version");
        List<String> types = configuration.getStringList("Options.Filter.Listen-Types");
        boolean typeFound;
        if (types.isEmpty()) {
            ListenType[] listenTypes = ListenType.values();
            listenTypeList.addAll(Arrays.asList(listenTypes));
        } else {
            for (String type : types) {
                typeFound = false;
                for (ListenType listenType : ListenType.values()) {
                    if (listenType.getName().equalsIgnoreCase(type)) {
                        typeFound = true;
                        listenTypeList.add(listenType);
                        break;
                    }
                }
                if (!typeFound) {
                    ProtocolStringReplacer.warn(PsrLocalization.getLocaledMessage(
                            "Console-Sender.Messages.Replacer-Config.Invalid-Listen-Type", type));
                }
            }
        }

        String matchMode = configuration.getString("Options.Match-Mode", "Contain");
        typeFound = false;
        for (MatchMode availableMatchMode : MatchMode.values()) {
            if (availableMatchMode.getName().equalsIgnoreCase(matchMode)) {
                this.matchMode = availableMatchMode;
                typeFound = true;
                break;
            }
        }
        if (!typeFound) {
            this.matchMode = MatchMode.CONTAIN;
            ProtocolStringReplacer.warn(PsrLocalization.getLocaledMessage(
                    "Console-Sender.Messages.Replacer-Config.Invalid-Match-Mode", matchMode));
        }
        for (ReplaceMode replaceMode : ReplaceMode.values()) {
            List<Map<?, ?>> mapList = configuration.getMapList("Replaces." + replaceMode.getNode());
            replaces.put(replaceMode, new ListOrderedMap());
            if (this.matchMode == MatchMode.REGEX) {
                for (Map<?, ?> map : mapList) {
                    replaces.get(replaceMode).put(Pattern.compile((String) map.get("Original")),
                            map.get("Replacement"));
                }
            } else {
                for (Map<?, ?> map : mapList) {
                    replaces.get(replaceMode).put(map.get("Original"), map.get("Replacement"));
                }
            }

            List<String> loadedBlockList = configuration.getStringList("Blocks." + replaceMode.getNode());
            ArrayList<Object> list;
            if (this.matchMode == MatchMode.REGEX) {
                list = new ArrayList<>();
                for (String string : loadedBlockList) {
                    list.add(Pattern.compile(string));
                }
            } else {
                list = new ArrayList<>(loadedBlockList);
            }
            blocks.put(replaceMode, list);
            updateStringSearcher(replaceMode);
        }
    }

    private void updateStringSearcher(@Nonnull ReplaceMode replaceMode) {
        if (matchMode != MatchMode.CONTAIN) {
            return;
        }
        SimpleStringSearcherBuilder builder = StringSearcher.builder().ignoreOverlaps();
        for (Object object : this.getReplaces(replaceMode).keySet()) {
            if (object instanceof String) {
                builder.addSearchString((String) object);
            } else {
                ProtocolStringReplacer.error(PsrLocalization.getLocaledMessage(
                        "Console-Sender.Messages.Replacer-Config.Invalid-Original-Format",
                        getRelativePath(), object.toString()));
            }
        }
        this.replacesStringSearcher.put(replaceMode, builder.build());
        String[] strings = new String[blocks.get(replaceMode).size()];
        for (int i = 0; i < blocks.get(replaceMode).size(); i++) {
            strings[i] = (String) blocks.get(replaceMode).get(i);
        }
        this.blocksStringSearcher.put(replaceMode, StringSearcher.builder().ignoreOverlaps().addSearchStrings(strings).build());
    }

    @Override
    public String toString() {
        return "ReplacerConfig{" +
                "file=" + file +
                ", configuration=" + configuration +
                ", enable=" + enable +
                ", priority=" + priority +
                ", listenTypeList=" + listenTypeList +
                ", matchMode=" + matchMode +
                ", replaces=" + replaces +
                ", blocks=" + blocks +
                ", author='" + author + '\'' +
                ", version='" + version + '\'' +
                ", edited=" + edited +
                ", replacesStringSearcher=" + replacesStringSearcher +
                ", blocksStringSearcher=" + blocksStringSearcher +
                '}';
    }

}
