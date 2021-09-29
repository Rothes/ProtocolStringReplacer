package me.rothes.protocolstringreplacer;

import com.comphenix.protocol.events.ListenerPriority;

public class ConfigManager {

    public final boolean printReplacer;

    public final char placeholderHead;
    public final char placeholderTail;

    public final long cleanTaskInterval;
    public final long cleanAccessInterval;

    public final ListenerPriority listenerPriority;
    public final boolean forceReplace;

    public ConfigManager(ProtocolStringReplacer instance) {
        this.printReplacer = instance.getConfig().getBoolean("Options.Features.Console.Print-Replacer-Config-When-Loaded", false);

        String placeholderHead = instance.getConfig().getString("Options.Features.Placeholder.Placeholder-Head");
        if (placeholderHead == null || placeholderHead.isEmpty()) {
            ProtocolStringReplacer.error(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Config.Invalid-Placeholder-Head"));
            this.placeholderHead = '｛';
        } else {
            this.placeholderHead = placeholderHead.charAt(0);
        }

        String placeholderTail = instance.getConfig().getString("Options.Features.Placeholder.Placeholder-Tail");
        if (placeholderTail == null || placeholderTail.isEmpty()) {
            ProtocolStringReplacer.error(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Config.Invalid-Placeholder-Tail"));
            this.placeholderTail = '｝';
        } else {
            this.placeholderTail = placeholderTail.charAt(0);
        }

        this.cleanTaskInterval = 1000L * instance.getConfig().getInt("Options.Features.ItemMetaCache.Purge-Task-Interval", 600);
        this.cleanAccessInterval = 1000L * instance.getConfig().getInt("Options.Features.ItemMetaCache.Purge-Access-Interval", 300);

        String priority = instance.getConfig().getString("Options.Features.Packet-Listener.Listener-Priority", "HIGHEST");
        ListenerPriority listenerPriority = null;
        for (ListenerPriority value : ListenerPriority.values()) {
            if (value.name().equalsIgnoreCase(priority)) {
                listenerPriority = value;
                break;
            }
        }
        if (listenerPriority == null) {
            ProtocolStringReplacer.error(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Config.Invalid-Listener-Priority"));
            listenerPriority = ListenerPriority.HIGHEST;
        }
        this.listenerPriority = listenerPriority;

        this.forceReplace = instance.getConfig().getBoolean("Options.Features.Packet-Listener.Force-Replace", false);
    }
}
