package me.rothes.protocolstringreplacer;

import com.comphenix.protocol.events.ListenerPriority;

import java.util.Locale;

public class ConfigManager {

    public final boolean printReplacer;

    public final char placeholderHead;
    public final char placeholderTail;

    public final long cleanTaskInterval;
    public final long cleanAccessInterval;

    public final LifeCycle loadConfigLifeCycle;

    public final ListenerPriority listenerPriority;
    public final boolean forceReplace;
    public final boolean consolePlaceholder;

    public final boolean convertPlayerChat;
    public final boolean removeCacheWhenMerchantTrade;
    public final int protocolLibSideStackPrintCount;

    public final String gitRawHost;

    public ConfigManager(ProtocolStringReplacer instance) {
        this.printReplacer = instance.getConfig().getBoolean("Options.Features.Console.Print-Replacer-Config-When-Loaded", false);

        String placeholderHead = instance.getConfig().getString("Options.Features.Placeholder.Placeholder-Head");
        if (placeholderHead == null || placeholderHead.isEmpty()) {
            ProtocolStringReplacer.error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Config.Invalid-Placeholder-Head"));
            this.placeholderHead = '｛';
        } else {
            this.placeholderHead = placeholderHead.charAt(0);
        }

        String placeholderTail = instance.getConfig().getString("Options.Features.Placeholder.Placeholder-Tail");
        if (placeholderTail == null || placeholderTail.isEmpty()) {
            ProtocolStringReplacer.error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Config.Invalid-Placeholder-Tail"));
            this.placeholderTail = '｝';
        } else {
            this.placeholderTail = placeholderTail.charAt(0);
        }

        this.cleanTaskInterval = 20L * instance.getConfig().getInt("Options.Features.ItemMetaCache.Purge-Task-Interval", 600);
        this.cleanAccessInterval = 1000L * instance.getConfig().getInt("Options.Features.ItemMetaCache.Purge-Access-Interval", 300);

        switch (instance.getConfig().getString("Options.Config-Load-LifeCycle", "ENABLE").toUpperCase(Locale.ROOT)) {
            case "INIT":
                this.loadConfigLifeCycle = LifeCycle.INIT;
                break;
            case "LOAD":
                this.loadConfigLifeCycle = LifeCycle.LOAD;
                break;
            case "ENABLE":
                this.loadConfigLifeCycle = LifeCycle.ENABLE;
                break;
            default:
                ProtocolStringReplacer.error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Config.Invalid-Config-Load-LifeCycle"));
                this.loadConfigLifeCycle = LifeCycle.ENABLE;
                break;
        }

        String priority = instance.getConfig().getString("Options.Features.Packet-Listener.Listener-Priority", "HIGHEST");
        ListenerPriority listenerPriority = null;
        for (ListenerPriority value : ListenerPriority.values()) {
            if (value.name().equalsIgnoreCase(priority)) {
                listenerPriority = value;
                break;
            }
        }
        if (listenerPriority == null) {
            ProtocolStringReplacer.error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Config.Invalid-Listener-Priority"));
            listenerPriority = ListenerPriority.HIGHEST;
        }
        this.listenerPriority = listenerPriority;

        this.forceReplace = instance.getConfig().getBoolean("Options.Features.Packet-Listener.Force-Replace", false);
        this.consolePlaceholder = instance.getConfig().getBoolean("Options.Features.Placeholder.Parse-For-Console", true);
        this.gitRawHost = instance.getConfig().getString("Options.Git-Raw-Host", "raw.githubusercontent.com");
        this.protocolLibSideStackPrintCount = instance.getConfig().getInt("Options.ProtocolLib-Side-Stack-Print-Count", 3);
        this.convertPlayerChat = instance.getServerMajorVersion() >= 19 && instance.getConfig().getBoolean("Options.Features.Chat-Packet.Convert-Player-Chat", true);
        this.removeCacheWhenMerchantTrade = instance.getConfig().getBoolean("Options.Features.ItemMetaCache.Remove-Cache-When-Merchant-Trade", false);
    }

    public enum LifeCycle {
        INIT,
        LOAD,
        ENABLE
    }

}
