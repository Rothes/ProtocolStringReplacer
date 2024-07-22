package io.github.rothes.protocolstringreplacer;

import io.github.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ConfigManager {

    public final boolean printReplacer;

    public final boolean cmdTypingSound;

    public final boolean placeholderEnabled;
    public final char placeholderHead;
    public final char placeholderTail;
    public final boolean consolePlaceholder;

    public final long cleanTaskInterval;
    public final long cleanAccessInterval;

    public final LifeCycle loadConfigLifeCycle;

    public final String listenerPriority;
    public final boolean forceReplace;

    public final boolean convertPlayerChat;
    public final boolean removeCacheWhenMerchantTrade;
    public final int protocolLibSideStackPrintCount;
    public final int maxCaptureRecords;

    public final boolean directSkips;

    public final String gitRawHost;

    public ConfigManager(ProtocolStringReplacer instance) {
        CommentYamlConfiguration config = instance.getConfig();
        this.printReplacer = config.getBoolean("Options.Features.Console.Print-Replacer-Config-When-Loaded", false);
        this.cmdTypingSound = config.getBoolean("Options.Features.Command-Typing-Sound-Enabled", true);

        String placeholderHead = config.getString("Options.Features.Placeholder.Placeholder-Head");
        if (placeholderHead == null || placeholderHead.isEmpty()) {
            ProtocolStringReplacer.error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Config.Invalid-Placeholder-Head"));
            this.placeholderHead = '｛';
        } else {
            this.placeholderHead = placeholderHead.charAt(0);
        }

        if (config.getBoolean("Options.Features.Placeholder.Placeholder-Enabled", true)) {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                List<Integer> ver = Arrays.stream(Bukkit.getPluginManager().getPlugin("PlaceholderAPI")
                        .getDescription().getVersion().split("-")[0].split("\\."))
                        .map(Integer::parseInt).collect(Collectors.toList());

                if (ver.get(1) < 10 || (ver.get(1) == 10 && ver.get(2) < 7)) {
                    ProtocolStringReplacer.warn(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Incompatible-PAPI-Version"));
                    placeholderEnabled = false;
                } else {
                    placeholderEnabled = true;
                }
            } else {
                ProtocolStringReplacer.warn(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Missing-PAPI"));
                placeholderEnabled = false;
            }
        } else {
            placeholderEnabled = false;
        }
        String placeholderTail = config.getString("Options.Features.Placeholder.Placeholder-Tail");
        if (placeholderTail == null || placeholderTail.isEmpty()) {
            ProtocolStringReplacer.error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Config.Invalid-Placeholder-Tail"));
            this.placeholderTail = '｝';
        } else {
            this.placeholderTail = placeholderTail.charAt(0);
        }

        this.cleanTaskInterval = 20L * config.getInt("Options.Features.ItemMetaCache.Purge-Task-Interval", 600);
        this.cleanAccessInterval = 1000L * config.getInt("Options.Features.ItemMetaCache.Purge-Access-Interval", 300);

        switch (config.getString("Options.Config-Load-LifeCycle", "ENABLE").toUpperCase(Locale.ROOT)) {
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

        this.listenerPriority = config.getString("Options.Features.Packet-Listener.Listener-Priority", "HIGHEST");

        this.forceReplace = config.getBoolean("Options.Features.Packet-Listener.Force-Replace", false);
        this.consolePlaceholder = config.getBoolean("Options.Features.Placeholder.Parse-For-Console", true);
        this.gitRawHost = config.getString("Options.Git-Raw-Host", "raw.githubusercontent.com");
        this.protocolLibSideStackPrintCount = config.getInt("Options.ProtocolLib-Side-Stack-Print-Count", 3);
        this.maxCaptureRecords = config.getInt("Options.Max-Capture-Records", 100);
        this.convertPlayerChat = instance.getServerMajorVersion() >= 19 && config.getBoolean("Options.Features.Chat-Packet.Convert-Player-Chat", true);
        this.removeCacheWhenMerchantTrade = config.getBoolean("Options.Features.ItemMetaCache.Remove-Cache-When-Merchant-Trade", false);

        directSkips = config.getBoolean("Options.Features.Replace-Mode.Skip-When-Direct-Replaced", true);
    }

    public enum LifeCycle {
        INIT,
        LOAD,
        ENABLE
    }

}
