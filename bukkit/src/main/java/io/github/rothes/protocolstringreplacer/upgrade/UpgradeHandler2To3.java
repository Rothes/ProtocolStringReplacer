package io.github.rothes.protocolstringreplacer.upgrade;

import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import org.apache.commons.collections.map.ListOrderedMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class UpgradeHandler2To3 extends DotConfigUpgradeHandler {

    @Override
    public void upgrade() {
        upgradeAllReplacerConfigs(new File(ProtocolStringReplacer.getInstance().getDataFolder() + "/Replacers"));

        CommentYamlConfiguration config = ProtocolStringReplacer.getInstance().getConfig();
        config.set("Configs-Version", 3);
        try {
            config.save(ProtocolStringReplacer.getInstance().getConfigFile());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void upgradeReplacerConfig(@NotNull File file, @NotNull YamlConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("Replaces");
        if (section != null) {
            final ListOrderedMap keyValueHashMap = new ListOrderedMap();
            final Pattern commentKeyPattern = CommentYamlConfiguration.getCommentKeyPattern();
            final Pattern splitPattern = Pattern.compile("\\| ");
            for (String key : section.getKeys(false)) {
                Object value = section.get(key);
                if (value instanceof String) {
                    String valueString = (String) value;
                    if (commentKeyPattern.matcher(key).find()) {
                        String[] split = splitPattern.split(valueString);
                        value = split[0] + "|   " + split[1];
                    }
                    keyValueHashMap.put(key, value);
                    section.set(key, null);
                }
            }
            section.set("23307㩵遌㚳这是注释是", "0|   # 常规文本替换模式.");
            Set<Map.Entry<String, String>> set = (Set<Map.Entry<String, String>>) keyValueHashMap.entrySet();
            for (Map.Entry<String, String> entry : set) {
                section.set("Common鰠" + entry.getKey(), entry.getValue());
            }
            try {
                config.save(file);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

}
