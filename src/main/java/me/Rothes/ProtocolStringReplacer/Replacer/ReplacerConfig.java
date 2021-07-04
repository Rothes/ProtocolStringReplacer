package me.Rothes.ProtocolStringReplacer.Replacer;

import me.Rothes.ProtocolStringReplacer.API.Configuration.CommentYamlConfiguration;
import me.Rothes.ProtocolStringReplacer.API.Configuration.DotYamlConfiguration;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import org.apache.commons.collections.map.ListOrderedMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplacerConfig {

    public static class CommentLine {
        private String key;
        private String value;

        private CommentLine(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    public enum MatchType {
        CONTAIN("contain"),
        EQUAL("equal"),
        REGEX("regex");

        private String name;

        MatchType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private File file;
    private DotYamlConfiguration configuration;
    private boolean enable;
    private int priority;
    private List<ListenType> listenTypeList = new ArrayList<>();
    private MatchType matchType;
    private ListOrderedMap replaces = new ListOrderedMap();
    private HashMap<Short, LinkedList<CommentLine>> commentLines = new HashMap<>();
    private String author;
    private String version;
    private boolean edited;

    public ReplacerConfig(@Nonnull File file, @Nonnull DotYamlConfiguration configuration) {
        long startTime = System.currentTimeMillis();
        loadData(file, configuration);
        if (ProtocolStringReplacer.getInstance().getConfig().getBoolean("Options.Features.Console.Print-Replacer-Config-When-Loaded", false)) {
            Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §a载入替换配置: " + getRelativePath() + ". §8耗时 " + (System.currentTimeMillis() - startTime) + "ms");
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

    public DotYamlConfiguration getConfiguration() {
        return configuration;
    }

    public int getPriority() {
        return priority;
    }

    public List<ListenType> getListenTypeList() {
        return listenTypeList;
    }

    public ListOrderedMap getReplaces() {
        return replaces;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public String getRelativePath() {
        return file.getAbsolutePath().substring((ProtocolStringReplacer.getInstance().getDataFolder().getAbsolutePath() + "\\").length()).replace('\\', '/');
    }

    public HashMap<Short, LinkedList<CommentLine>> getCommentLines() {
        return commentLines;
    }

    public void saveConfig() {
        configuration.set("Options鰠Enable", enable);
        configuration.set("Options鰠Priority", priority);
        configuration.set("Options鰠Author", author);
        configuration.set("Options鰠Version", version);
        LinkedList<String> types = new LinkedList<>();
        for (var listenType : listenTypeList) {
            types.add(listenType.getName());
        }
        configuration.set("Options鰠Filter鰠Listen-Types", types);
        configuration.set("Options鰠Match-Type", matchType.getName());
        configuration.set("Replaces", new ArrayList<String>());
        if (matchType == MatchType.REGEX) {
            for (short i = 0; i < replaces.size(); i++) {
                if (commentLines.containsKey(i)) {
                    LinkedList<CommentLine> commentLineList = commentLines.get(i);
                    for (var commentLine: commentLineList) {
                        configuration.set("Replaces鰠" + commentLine.key, commentLine.value);
                    }
                }
                Pattern pattern = (Pattern) replaces.get(i);
                configuration.set("Replaces鰠" + pattern.toString(), replaces.get(pattern));
            }
        } else {
            for (short i = 0; i < replaces.size(); i++) {
                if (commentLines.containsKey(i)) {
                    LinkedList<CommentLine> commentLineList = commentLines.get(i);
                    for (var commentLine: commentLineList) {
                        configuration.set("Replaces鰠" + commentLine.key, commentLine.value);
                    }
                }
                String string = (String) replaces.get(i);
                configuration.set("Replaces鰠" + string, replaces.get(string));
            }
        }
        try {
            configuration.save(file);
            edited = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setReplace(int index, @Nonnull String value) {
        if (index < replaces.size()) {
            replaces.setValue(index, value);
            edited = true;
            saveConfig();
        }
    }

    public void setReplace(int index, @Nonnull String key, @Nonnull String value) {
        if (index < replaces.size()) {
            removeReplace(index, false);
        }
        if (index <= replaces.size()) {
            addReplace(index, key, value);
            edited = true;
            saveConfig();
        }
    }

    public void addReplace(int index, @Nonnull String key, @Nonnull String value) {
        if (index <= replaces.size()) {
            if (this.matchType == MatchType.REGEX) {
                replaces.put(index, Pattern.compile(key), value);
            } else {
            replaces.put(index, key, value);
            }
            HashMap<Short, LinkedList<CommentLine>> commentLines = new HashMap<>();
            for (Map.Entry<Short, LinkedList<CommentLine>> entry : this.commentLines.entrySet()) {
                short i = entry.getKey();
                LinkedList<CommentLine> commentLineList = entry.getValue();
                if (i >= index) {
                    commentLines.put((short) (i + 1), commentLineList);
                } else {
                    commentLines.put(i, commentLineList);
                }
                this.commentLines = commentLines;
            }
            edited = true;
            saveConfig();
        }
    }

    public void addReplace(@Nonnull String key, @Nonnull String value) {
        addReplace(replaces.size(), key, value);
    }

    public void removeReplace(int index) {
        removeReplace(index, true);
        edited = true;
        saveConfig();
    }

    public void removeReplace(int index, boolean editComment) {
        HashMap<Short, LinkedList<CommentLine>> commentLines = new HashMap<>();
        if (editComment) {
            for (Map.Entry<Short, LinkedList<CommentLine>> entry : this.commentLines.entrySet()) {
                short i = entry.getKey();
                LinkedList<CommentLine> commentLineList = entry.getValue();
                if (i > index) {
                    commentLines.put((short) (i - 1), commentLineList);
                } else if (i < index) {
                    commentLines.put(i, commentLineList);
                }
            }
            this.commentLines = commentLines;
        }
        replaces.remove(index);
        edited = true;
        saveConfig();
    }

    public int checkReplaceKey(@Nonnull String key) {
        for (int i = 0; i < replaces.size(); i++) {
            if (replaces.get(i).equals(key)) {
                return i;
            }
        }
        return -1;
    }

    private boolean checkComment(String key, String value, Pattern pattern) {
        Matcher matcher = pattern.matcher(key);
        if (matcher.find()) {
            LinkedList<CommentLine> commentLines = this.commentLines.get((short) replaces.size());
            if (commentLines == null) {
                this.commentLines.put((short) replaces.size(), new LinkedList<>(Collections.singletonList(new CommentLine(key, value))));
            } else {
                commentLines.add(new CommentLine(key, value));
            }
            return true;
        }
        return false;
    }

    private void loadData(File file, DotYamlConfiguration configuration) {
        this.configuration = configuration;
        this.file = file;
        enable = configuration.getBoolean("Options鰠Enable", false);
        priority = configuration.getInt("Options鰠Priority", 5);
        author = configuration.getString("Options鰠Author");
        version = configuration.getString("Options鰠Version");
        List<String> types = configuration.getStringList("Options鰠Filter鰠Listen-Types");
        boolean typeFound;
        if (types.isEmpty()) {
            ListenType[] listenTypes = ListenType.values();
            listenTypeList.addAll(Arrays.asList(listenTypes));
        } else {
            for (String type : types) {
                typeFound = false;
                for (var listenType : ListenType.values()) {
                    if (listenType.getName().equals(type)) {
                        typeFound = true;
                        listenTypeList.add(listenType);
                        break;
                    }
                }
                if (!typeFound) {
                    Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §c未知或不支持的监听类型: " + type);
                }
            }
        }

        String matchType = configuration.getString("Options鰠Match-Type", "contain");
        typeFound = false;
        for (MatchType availableMatchType : MatchType.values()) {
            if (availableMatchType.name.equalsIgnoreCase(matchType)) {
                this.matchType = availableMatchType;
                typeFound = true;
                break;
            }
        }
        if (!typeFound) {
            this.matchType = MatchType.CONTAIN;
            Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §c未知的文本匹配方式: " + matchType + ". 使用默认值\"contain\"");
        }
        ConfigurationSection section = configuration.getConfigurationSection("Replaces");
        if (section != null) {
            Pattern commentKeyPattern = CommentYamlConfiguration.getCommentKeyPattern();
            if (this.matchType == MatchType.REGEX) {
                for (String key : section.getKeys(true)) {
                    String value = configuration.getString("Replaces鰠" + key);
                    if (!checkComment(key, value, commentKeyPattern)) {
                        replaces.put(Pattern.compile(key), value);
                    }
                }
            } else {
                for (String key : section.getKeys(true)) {
                    String value = configuration.getString("Replaces鰠" + key);
                    if (!checkComment(key, value, commentKeyPattern)) {
                        replaces.put(key, value);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ReplacerConfig{" +
                "file=" + file +
                ", configuration=" + configuration +
                ", enable=" + enable +
                ", priority=" + priority +
                ", listenTypeList=" + listenTypeList +
                ", matchType=" + matchType +
                ", replaces=" + replaces +
                ", commentLines=" + commentLines +
                ", author='" + author + '\'' +
                ", version='" + version + '\'' +
                ", edited=" + edited +
                '}';
    }

}
