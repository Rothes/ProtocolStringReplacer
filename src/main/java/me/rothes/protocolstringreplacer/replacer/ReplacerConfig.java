package me.rothes.protocolstringreplacer.replacer;

import me.rothes.protocolstringreplacer.PSRLocalization;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import org.apache.commons.collections.map.ListOrderedMap;
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

public class ReplacerConfig {

    private File file;
    private CommentYamlConfiguration configuration;
    private boolean enable;
    private int priority;
    private List<ListenType> listenTypeList = new ArrayList<>();
    private MatchMode matchMode;
    private HashMap<ReplacesMode, ListOrderedMap> replaces = new HashMap<>();
    private HashMap<ReplacesMode, List<Object>> blocks = new HashMap<>();
    private String author;
    private String version;
    private boolean edited;

    private HashMap<ReplacesMode, StringSearcher<String>> replacesStringSearcher = new HashMap<>();
    private HashMap<ReplacesMode, StringSearcher<String>> blocksStringSearcher = new HashMap<>();

    public ReplacerConfig(@Nonnull File file, @Nonnull CommentYamlConfiguration configuration) {
        long startTime = System.nanoTime();
        loadData(file, configuration);
        if (ProtocolStringReplacer.getInstance().getConfigManager().printReplacer) {
            ProtocolStringReplacer.info(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Replacer-Config.Replacer-Load-Complete",
                    getRelativePath(), String.valueOf((System.nanoTime() - startTime) / 1000000d)));
        }
    }

    public boolean isEdited() {
        return edited;
    }

    public File getFile() {
        return file;
    }

    public boolean isEnable() {
        return enable;
    }

    public CommentYamlConfiguration getConfiguration() {
        return configuration;
    }

    public int getPriority() {
        return priority;
    }

    public List<ListenType> getListenTypeList() {
        return listenTypeList;
    }

    public ListOrderedMap getReplaces(@Nonnull ReplacesMode replacesMode) {
        return replaces.get(replacesMode);
    }

    public List<Object> getBlocks(@Nonnull ReplacesMode replacesMode) {
        return blocks.get(replacesMode);
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public MatchMode getMatchMode() {
        return matchMode;
    }

    public String getRelativePath() {
        return file.getAbsolutePath().substring((ProtocolStringReplacer.getInstance().getDataFolder().getAbsolutePath() + "\\")
                .length()).replace('\\', '/');
    }

    public StringSearcher<String> getReplacesStringSearcher(ReplacesMode replacesMode) {
        return replacesStringSearcher.get(replacesMode);
    }

    public StringSearcher<String> getBlocksStringSearcher(ReplacesMode replacesMode) {
        return blocksStringSearcher.get(replacesMode);
    }

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
        for (ReplacesMode replacesMode : ReplacesMode.values()) {
            ListOrderedMap replaces = this.replaces.get(replacesMode);
            ArrayList<ListOrderedMap> result = new ArrayList<>();
            for (short i = 0; i < replaces.size(); i++) {
                ListOrderedMap entryMap = new ListOrderedMap();
                Object object = replaces.get(i);
                entryMap.put("Original", object);
                entryMap.put("Replacement", replaces.get(object));
                result.add(entryMap);
            }
            configuration.set("Replaces." + replacesMode.getNode(), result);

            configuration.set("Blocks." + replacesMode.getNode(), blocks.get(replacesMode));
        }
        try {
            configuration.save(file);
            edited = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setReplace(int index, @Nonnull String value, @Nonnull ReplacesMode replacesMode) {
        if (index < replaces.size()) {
            replaces.get(replacesMode).setValue(index, value);
            updateStringSearcher(replacesMode);
            edited = true;
            saveConfig();
        }
    }

    public void setReplace(int index, @Nonnull String key, @Nonnull String value, @Nonnull ReplacesMode replacesMode) {
        if (index < replaces.size()) {
            removeReplace(index, replacesMode);
        }
        if (index <= replaces.size()) {
            addReplace(index, key, value, replacesMode);
        }
    }

    public void addReplace(int index, @Nonnull String key, @Nonnull String value, @Nonnull ReplacesMode replacesMode) {
        if (index <= replaces.size()) {
            if (this.matchMode == MatchMode.REGEX) {
                replaces.get(replacesMode).put(index, Pattern.compile(key), value);
            } else {
                replaces.get(replacesMode).put(index, key, value);
            }
            updateStringSearcher(replacesMode);
            edited = true;
            saveConfig();
        }
    }

    public void addReplace(@Nonnull String key, @Nonnull String value, @Nonnull ReplacesMode replacesMode) {
        addReplace(replaces.get(replacesMode).size(), key, value, replacesMode);
    }

    public void removeReplace(int index, @Nonnull ReplacesMode replacesMode) {
        replaces.get(replacesMode).remove(index);
        updateStringSearcher(replacesMode);
        edited = true;
        saveConfig();
    }

    public void setBlock(int index, @Nonnull String block, @Nonnull ReplacesMode replacesMode) {
        if (index < replaces.size()) {
            blocks.get(replacesMode).set(index, block);
            updateStringSearcher(replacesMode);
            edited = true;
            saveConfig();
        }
    }

    public void addBlock(int index, @Nonnull String block, @Nonnull ReplacesMode replacesMode) {
        if (index <= replaces.size()) {
            if (this.matchMode == MatchMode.REGEX) {
                blocks.get(replacesMode).add(index, Pattern.compile(block));
            } else {
                blocks.get(replacesMode).add(index, block);
            }
            updateStringSearcher(replacesMode);
            edited = true;
            saveConfig();
        }
    }

    public void addBlock(@Nonnull String block, @Nonnull ReplacesMode replacesMode) {
        addBlock(blocks.get(replacesMode).size(), block, replacesMode);
    }

    public void removeBlock(int index, @Nonnull ReplacesMode replacesMode) {
        blocks.get(replacesMode).remove(index);
        updateStringSearcher(replacesMode);
        edited = true;
        saveConfig();
    }

    public int checkReplaceKey(@Nonnull String key, @Nonnull ReplacesMode replacesMode) {
        for (int i = 0; i < replaces.get(replacesMode).size(); i++) {
            if (replaces.get(replacesMode).get(i).equals(key)) {
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
                    ProtocolStringReplacer.warn(PSRLocalization.getLocaledMessage(
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
            ProtocolStringReplacer.warn(PSRLocalization.getLocaledMessage(
                    "Console-Sender.Messages.Replacer-Config.Invalid-Match-Mode", matchMode));
        }
        for (ReplacesMode replacesMode : ReplacesMode.values()) {
            List<Map<?, ?>> mapList = configuration.getMapList("Replaces." + replacesMode.getNode());
            replaces.put(replacesMode, new ListOrderedMap());
            if (this.matchMode == MatchMode.REGEX) {
                for (Map<?, ?> map : mapList) {
                    replaces.get(replacesMode).put(Pattern.compile((String) map.get("Original")),
                            map.get("Replacement"));
                }
            } else {
                for (Map<?, ?> map : mapList) {
                    replaces.get(replacesMode).put(map.get("Original"), map.get("Replacement"));
                }
            }

            List<String> loadedBlockList = configuration.getStringList("Blocks." + replacesMode.getNode());
            ArrayList<Object> list;
            if (this.matchMode == MatchMode.REGEX) {
                list = new ArrayList<>();
                for (String string : loadedBlockList) {
                    list.add(Pattern.compile(string));
                }
            } else {
                list = new ArrayList<>(loadedBlockList);
            }
            blocks.put(replacesMode, list);
            updateStringSearcher(replacesMode);
        }
    }

    private void updateStringSearcher(@Nonnull ReplacesMode replacesMode) {
        if (matchMode != MatchMode.CONTAIN) {
            return;
        }
        SimpleStringSearcherBuilder builder = StringSearcher.builder().ignoreOverlaps();
        for (Object object : this.getReplaces(replacesMode).keySet()) {
            if (object instanceof String) {
                builder.addSearchString((String) object);
            } else {
                ProtocolStringReplacer.error(PSRLocalization.getLocaledMessage(
                        "Console-Sender.Messages.Replacer-Config.Invalid-Original-Format",
                        getRelativePath(), object.toString()));
            }
        }
        this.replacesStringSearcher.put(replacesMode, builder.build());
        String[] strings = new String[blocks.get(replacesMode).size()];
        for (int i = 0; i < blocks.get(replacesMode).size(); i++) {
            strings[i] = (String) blocks.get(replacesMode).get(i);
        }
        this.blocksStringSearcher.put(replacesMode, StringSearcher.builder().ignoreOverlaps().addSearchStrings(strings).build());
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
