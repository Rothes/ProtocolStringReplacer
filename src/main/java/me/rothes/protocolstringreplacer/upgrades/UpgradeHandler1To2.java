package me.rothes.protocolstringreplacer.upgrades;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import me.rothes.protocolstringreplacer.api.configuration.DotYamlConfiguration;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class UpgradeHandler1To2 extends AbstractUpgradeHandler{

    private enum PacketType {
        CHAT("CHAT", "chat"),
        TITLE("TITLE", "title"),
        BOSS_BAR("BOSS_BAR", "boss-bar"),
        OPEN_WINDOW("OPEN_WINDOW", "window-title"),
        SET_SLOT("SET_SLOT", "itemstack"),
        WINDOW_ITEMS("WINDOW_ITEMS", "itemstack"),
        ENTITY_METADATA("ENTITY_METADATA", "entity"),
        TILE_ENTITY_DATA("TILE_ENTITY_DATA", "sign"),
        MAP_CHUNK("MAP_CHUNK", "sign");

        private String packetType;
        private String listenType;
        PacketType(String packetType, String listenType) {
            this.packetType = packetType;
            this.listenType = listenType;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void upgrade() {
        HashMap<File, DotYamlConfiguration> loadReplacesFiles = ProtocolStringReplacer.getInstance().getReplacerManager().loadReplacesFiles(
                new File(ProtocolStringReplacer.getInstance().getDataFolder() + "/Replacers"));
        for (var entry : loadReplacesFiles.entrySet()) {
            upgradeReplacerConfig(entry.getKey(), entry.getValue());
        }

        CommentYamlConfiguration config = ProtocolStringReplacer.getInstance().getConfig();
        ListOrderedMap keyValues = new ListOrderedMap();
        ConfigurationSection configurationSection = config.getConfigurationSection("");
        for (String key: configurationSection.getKeys(true)) {
            keyValues.put(key, config.get(key));
        }
        config = new CommentYamlConfiguration();
        config.set("12340㩵遌㚳这是注释是", "0| # 请勿手动修改Configs-Version值!");
        config.set("Configs-Version", 2);
        config.set("12341㩵遌㚳这是注释是", "0| #");
        var entrySet = (Set<Map.Entry<String, Object>>) keyValues.entrySet();
        for (var entry : entrySet) {
            config.set(entry.getKey(), entry.getValue());
        }
        try {
            config.save(ProtocolStringReplacer.getInstance().getConfigFile());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    protected void upgradeReplacerConfig(@Nonnull File file, @Nonnull DotYamlConfiguration config) {
        Validate.notNull(file, "configuration File cannot be null");
        Validate.notNull(config, "configuration cannot be null");

        LinkedList<String> listenTypes = new LinkedList<>();
        List<String> packetTypes = config.getStringList("Options鰠Filter鰠Packet-Types");
        if (packetTypes.isEmpty()) {
            for (var packetType : PacketType.values()) {
                if (!listenTypes.contains(packetType.listenType)) {
                    listenTypes.add(packetType.listenType);
                }
            }
        } else {
            for (var type : packetTypes) {
                boolean typeFound = false;
                for (var packetType : PacketType.values()) {
                    if (packetType.packetType.equals(type)) {
                        typeFound = true;
                        if (!listenTypes.contains(packetType.listenType)) {
                            listenTypes.add(packetType.listenType);
                        }
                        break;
                    }
                }
                if (!typeFound) {
                    Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §c未知或不支持的数据包类型: " + type + ", 已忽略.");
                }
            }
        }

        config.set("Options鰠Filter鰠12340㩵遌㚳这是注释是", "0|     # window-title 替换容器标题文本");
        config.set("Options鰠Filter鰠12341㩵遌㚳这是注释是", "1|     # itemstack 替换物品(物品名|Lore|书署名|书内容)文本");
        config.set("Options鰠Filter鰠12342㩵遌㚳这是注释是", "2|     # boss-bar 替换Boss血量条文本");
        config.set("Options鰠Filter鰠12343㩵遌㚳这是注释是", "3|     # entity 替换实体名文本");
        config.set("Options鰠Filter鰠12344㩵遌㚳这是注释是", "4|     # title 替换标题(title|subtitle)文本");
        config.set("Options鰠Filter鰠12345㩵遌㚳这是注释是", "5|     # sign 替换告示牌文本");
        config.set("Options鰠Filter鰠12346㩵遌㚳这是注释是", "6|     # chat 替换聊天(chat|actionbar)信息文本");
        config.set("Options鰠Filter鰠12347㩵遌㚳这是注释是", "7|     # 指定替换何处的字符串. 默认为全部. 可选值: ");
        config.set("Options鰠Filter鰠Listen-Types", listenTypes);

        Pattern commentPattern = CommentYamlConfiguration.getCommentKeyPattern();
        List<String> commentKeys = new ArrayList<>();
        ConfigurationSection configurationSection = config.getConfigurationSection("");
        for (String key: configurationSection.getKeys(true)) {
            if (commentPattern.matcher(key).find()) {
                commentKeys.add(key);
            } else {
                if (key.equals("Config-Version") || key.equals("Options鰠Filter鰠Packet-Types")) {
                    for (var commentKey : commentKeys) {
                        config.set(commentKey, null);
                    }
                    config.set(key, null);
                }
                commentKeys.clear();
            }
        }
        try {
            config.save(file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
