package me.rothes.protocolstringreplacer.upgrades;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import me.rothes.protocolstringreplacer.api.configuration.DotYamlConfiguration;
import me.rothes.protocolstringreplacer.replacer.ReplacesMode;
import org.apache.commons.collections.map.ListOrderedMap;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class UpgradeHandler3To4 extends AbstractUpgradeHandler{

    @Override
    public void upgrade() {
        CommentYamlConfiguration config = ProtocolStringReplacer.getInstance().getConfig();
        Pattern commentPattern = CommentYamlConfiguration.getCommentKeyPattern();
        ArrayList<String> coments = new ArrayList<>();
        for (String key : config.getKeys(true)) {
            if (commentPattern.matcher(key).find()) {
                coments.add(key);
            } else {
                if (key.equals("Options.Features.Packet-Listener.Listen-Dropped-Item-Entity")) {
                    for (String comment : coments) {
                        config.set(comment, null);
                    }
                    config.set("Options.Features.Packet-Listener.Listen-Dropped-Item-Entity", null);
                }
                coments.clear();
            }
        }
        try {
            config.save(ProtocolStringReplacer.getInstance().getConfigFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        upgradeAllReplacerConfigs(new File(ProtocolStringReplacer.getInstance().getDataFolder() + "/Replacers"));
    }

    @Override
    protected void upgradeReplacerConfig(@NotNull File file, @NotNull DotYamlConfiguration config) {
        typeToMode(config);
        updateReplacesStructure(config);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void updateReplacesStructure(@NotNull DotYamlConfiguration config) {
        for (ReplacesMode replacesMode : ReplacesMode.values()) {
            String pathString = "Replaces鰠" + replacesMode.getNode();
            ConfigurationSection section = config.getConfigurationSection(pathString);
            if (section != null) {
                LinkedList<String> comments = new LinkedList<>();
                ListOrderedMap replaces = new ListOrderedMap();
                Pattern splitPattern = Pattern.compile("\\| ");
                final Pattern commentKeyPattern = CommentYamlConfiguration.getCommentKeyPattern();
                for (String key : section.getKeys(true)) {
                    Object value = section.get(key);
                    if (value instanceof String) {
                        String stringVaule = (String) value;
                        if (commentKeyPattern.matcher(key).find()) {
                            comments.add(0, splitPattern.split(stringVaule)[1]);
                        } else {
                            replaces.put(key, stringVaule);
                        }
                    }
                }
                int commentIndex = 1000;
                int lineIndex = 0;
                for (String comment : comments) {
                    config.set("Replaces鰠" + commentIndex++ + "㩵遌㚳这是注释是", lineIndex++ + "| " + comment);
                }
                config.set(pathString, null);
                ArrayList<ListOrderedMap> replacesList = new ArrayList<>();
                Set<Map.Entry<String, String>> set = replaces.entrySet();
                for (Map.Entry<String, String> entry : set) {
                    ListOrderedMap entryMap = new ListOrderedMap();
                    entryMap.put("Original", entry.getKey());
                    entryMap.put("Replacement", entry.getValue());
                    replacesList.add(entryMap);
                }
                if (!replacesList.isEmpty()) {
                    config.set(pathString, replacesList);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void typeToMode(@NotNull DotYamlConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("Options");
        if (section != null) {
            final ListOrderedMap comments = new ListOrderedMap();
            final Pattern commentKeyPattern = CommentYamlConfiguration.getCommentKeyPattern();
            int commentIndex = 6789;
            for (String key : section.getKeys(false)) {
                Object value = section.get(key);
                if (value instanceof String) {
                    String valueString = (String) value;
                    if (commentKeyPattern.matcher(key).find()) {
                        comments.put(key, valueString);
                    } else {
                        if (key.equals("Match-Type")) {
                            Set<Map.Entry<String, String>> set = comments.entrySet();
                            for (Map.Entry<String, String> entry : set) {
                                section.set(entry.getKey(), null);
                                section.set(commentIndex++ + "㩵遌㚳这是注释是", entry.getValue());
                            }
                            section.set("Match-Mode", valueString);
                            section.set(key, null);
                            return;
                        }
                        comments.clear();
                    }
                }
            }
        }
    }

}
