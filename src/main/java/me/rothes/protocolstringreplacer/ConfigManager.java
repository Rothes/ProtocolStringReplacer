package me.rothes.protocolstringreplacer;

import com.comphenix.protocol.events.ListenerPriority;
import org.bukkit.Bukkit;

public class ConfigManager {

    public final boolean printReplacer;

    public final char placeholderHead;
    public final char placeholderTail;

    public final long cleanTaskInterval;
    public final long cleanAccessInterval;

    public final ListenerPriority listenerPriority;
    public final boolean forceReplace;
    public final boolean listenDroppedItemEntity;

    public ConfigManager(ProtocolStringReplacer instance) {
        this.printReplacer = instance.getConfig().getBoolean("Options.Features.Console.Print-Replacer-Config-When-Loaded", false);

        String placeholderHead = instance.getConfig().getString("Options.Features.Placeholder.Placeholder-Head");
        if (placeholderHead == null || placeholderHead.isEmpty()) {
            Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §cPlaceholder-Head 值无效! 请定义一个字符. 使用默认值 '｛'");
            this.placeholderHead = '｛';
        } else {
            this.placeholderHead = placeholderHead.charAt(0);
        }

        String placeholderTail = instance.getConfig().getString("Options.Features.Placeholder.Placeholder-Tail");
        if (placeholderTail == null || placeholderTail.isEmpty()) {
            Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §cPlaceholder-Tail 值无效! 请定义一个字符. 使用默认值 '｝'");
            this.placeholderTail = '｝';
        } else {
            this.placeholderTail = placeholderTail.charAt(0);
        }

        this.cleanTaskInterval = 1000L * instance.getConfig().getInt("Options.Features.ItemMetaCache.Clean-Task-Interval", 600);
        this.cleanAccessInterval = 1000L * instance.getConfig().getInt("Options.Features.ItemMetaCache.Clean-Access-Interval", 300);

        String priority = instance.getConfig().getString("Options.Features.Packet-Listener.Listener-Priority", "HIGHEST");
        ListenerPriority listenerPriority = null;
        for (ListenerPriority value : ListenerPriority.values()) {
            if (value.name().equalsIgnoreCase(priority)) {
                listenerPriority = value;
                break;
            }
        }
        if (listenerPriority == null) {
            Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §c数据包监听优先度值无效! 使用默认值 'HIGHEST'");
            listenerPriority = ListenerPriority.HIGHEST;
        }
        this.listenerPriority = listenerPriority;

        this.forceReplace = instance.getConfig().getBoolean("Options.Features.Packet-Listener.Force-Replace", false);
        this.listenDroppedItemEntity = instance.getConfig().getBoolean("Options.Features.Packet-Listener.Listen-Dropped-Item-Entity", true);
    }
}
