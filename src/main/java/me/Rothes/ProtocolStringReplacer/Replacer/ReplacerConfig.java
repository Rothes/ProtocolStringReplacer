package me.Rothes.ProtocolStringReplacer.Replacer;

import com.comphenix.protocol.PacketType;
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
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplacerConfig {

    private static class CommentLine {
        private String key;
        private String value;

        private CommentLine(String key, String value) {
            this.key = key;
            this.value = value;
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
    private List<PacketType> packetTypeList = new ArrayList<>();
    private MatchType matchType;
    private ListOrderedMap replaces = new ListOrderedMap();
    private HashMap<Short, CommentLine> commentLines = new HashMap<>();
    private String author;
    private String version;

    public ReplacerConfig(@Nonnull File file, @Nonnull DotYamlConfiguration configuration) {
        long startTime = System.currentTimeMillis();
        this.file = file;
        this.configuration = configuration;
        enable = configuration.getBoolean("Enable", false);
        priority = configuration.getInt("Priority", 5);
        author = configuration.getString("Author");
        version = configuration.getString("Version");
        List<String> types = configuration.getStringList("Filter鰠Packet-Types");
        boolean typeFound;
        if (types.isEmpty()) {
            ReplacerType[] replacerTypes = ReplacerType.values();
            for (ReplacerType replacerType : replacerTypes) {
                packetTypeList.add(replacerType.getPacketType());
            }
        } else {
            for (String type : types) {
                typeFound = false;
                for (ReplacerType replacerType : ReplacerType.values()) {
                    if (replacerType.getName().equals(type)) {
                        typeFound = true;
                        packetTypeList.add(replacerType.getPacketType());
                        break;
                    }
                }
                if (!typeFound) {
                    Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §c未知或不支持的数据包类型: " + type);
                }
            }
        }
        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 17) {
            if (packetTypeList.remove(PacketType.Play.Server.TITLE)) {
                packetTypeList.add(PacketType.Play.Server.SET_TITLE_TEXT);
                packetTypeList.add(PacketType.Play.Server.SET_SUBTITLE_TEXT);
            }
        }

        String matchType = configuration.getString("Match-Type", "contain");
        typeFound = false;
        for (MatchType availableMatchType : MatchType.values()) {
            if (availableMatchType.name.equalsIgnoreCase(matchType)) {
                this.matchType = availableMatchType;
                typeFound = true;
                break;
            }
        }
        if (!typeFound) {
            this.matchType = MatchType.EQUAL;
            Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §c未知的文本匹配方式: " + matchType + ". 使用默认值\"contain\"");
        }
        ConfigurationSection section = configuration.getConfigurationSection("Replaces");
        if (section != null) {
            Pattern commentKeyPattern = CommentYamlConfiguration.getCommentKeyPattern();
            if (this.matchType == MatchType.REGEX) {
                for (String key : section.getKeys(true)) {
                    String value = configuration.getString("Replaces鰠" + key);
                    if (!checkComment(key, value, commentKeyPattern)) {
                        replaces.put(Pattern.compile(key, Pattern.DOTALL), value);
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
        if (ProtocolStringReplacer.getInstance().getConfig().getBoolean("Options.Features.Console.Print-Replacer-Config-When-Loaded", false)) {
            Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §a载入替换配置: " + getRelativePath() + ". §8耗时 " + (System.currentTimeMillis() - startTime) + "ms");
        }
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

    public List<PacketType> getPacketTypeList() {
        return packetTypeList;
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
        return file.getAbsolutePath().replace(ProtocolStringReplacer.getInstance().getDataFolder().getAbsolutePath() + "\\", "");
    }

    public void saveConfig() {
        configuration.set("Enable", enable);
        configuration.set("Priority", priority);
        configuration.set("Author", author);
        configuration.set("Version", version);
        List<String> types = new ArrayList<>();
        boolean isUp17 = ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 17;
        for (PacketType packetType : packetTypeList) {
            for (ReplacerType replacerType : ReplacerType.values()) {
                if (replacerType.getPacketType() == packetType) {
                    types.add(replacerType.getName());
                    break;
                }
            }
            if (isUp17 && (packetType == PacketType.Play.Server.SET_TITLE_TEXT || packetType == PacketType.Play.Server.SET_SUBTITLE_TEXT)) {
                types.add(ReplacerType.TITLE.getName());
            }
        }
        configuration.set("Filter鰠Packet-Types", types);
        configuration.set("Match-Type", matchType.getName());
        configuration.set("Replaces", null);
        if (matchType == MatchType.REGEX) {
            for (short i = 0; i < replaces.size(); i++) {
                if (commentLines.containsKey(i)) {
                    CommentLine commentLine = commentLines.get(i);
                    configuration.set("Replaces鰠" + commentLine.key, commentLine.value);
                }
                Pattern pattern = (Pattern) replaces.get(i);
                configuration.set("Replaces鰠" + pattern.toString(), replaces.get(pattern));
            }
        } else {
            for (short i = 0; i < replaces.size(); i++) {
                if (commentLines.containsKey(i)) {
                    CommentLine commentLine = commentLines.get(i);
                    configuration.set("Replaces鰠" + commentLine.key, commentLine.value);
                }
                String string = (String) replaces.get(i);
                configuration.set("Replaces鰠" + string, replaces.get(string));
            }
        }
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "ReplacerConfig{" +
                "file=" + file +
                ", configuration=" + configuration +
                ", enable=" + enable +
                ", priority=" + priority +
                ", packetTypeList=" + packetTypeList +
                ", matchType=" + matchType +
                ", replaces=" + replaces +
                ", author='" + author + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    private boolean checkComment(String key, String value, Pattern pattern) {
        Matcher matcher = pattern.matcher(key);
        if (matcher.find()) {
            commentLines.put((short) replaces.size(), new CommentLine(key, value));
            return true;
        }
        return false;
    }

}
