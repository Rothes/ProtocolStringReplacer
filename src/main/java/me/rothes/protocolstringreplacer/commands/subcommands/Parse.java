package me.rothes.protocolstringreplacer.commands.subcommands;

import me.rothes.protocolstringreplacer.PSRLocalization;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplacesMode;
import me.rothes.protocolstringreplacer.api.user.User;
import me.rothes.protocolstringreplacer.utils.ColorUtils;
import me.rothes.protocolstringreplacer.commands.SubCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.neosearch.stringsearcher.Emit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parse extends SubCommand {

    public Parse() {
        super("parse", "protocolstringreplacer.command.parse",
                PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Description"));
    }

    @Override
    public void onExecute(@NotNull User user, @NotNull String[] args) {
        if (args.length == 5) {
            Player player;
            if ("null".equals(args[2])) {
                player = null;
            } else {
                player = Bukkit.getPlayer(args[2]);
                if (player == null) {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Variables.Player.Messages.Player-Is-Offline", args[2]));
                    return;
                }
            }
            ListenType listenType = ListenType.getType(args[3]);
            if (listenType == null) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Variables.Listen-Type.Messages.Invalid-Type", args[3]));
                return;
            }
            ReplacesMode replacesMode = null;
            for (ReplacesMode mode : ReplacesMode.values()) {
                if (mode.getNode().equalsIgnoreCase(args[4])) {
                    replacesMode = mode;
                    break;
                }
            }
            if (replacesMode == null) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Variables.Match-Mode.Messages.Invalid-Mode", args[4]));
                return;
            }

            user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage("Sender.Commands.Parse.Start-Parse"));
            ReplacesMode finalReplacesMode = replacesMode;
            Bukkit.getScheduler().runTaskAsynchronously(ProtocolStringReplacer.getInstance(), () ->
                    startParse(user, args[1], player, listenType, finalReplacesMode));
            return;
        }
        sendHelp(user);
    }

    private void startParse(@NotNull User user, @NotNull String originalText, Player player,
                            @NotNull ListenType listenType, @NotNull ReplacesMode replacesMode) {
        long startTime = System.nanoTime();
        String original = ColorUtils.getColored(originalText);
        String text = original;
        ArrayList<HoverEvent> results = new ArrayList<>();
        for (ReplacerConfig replacerConfig : ProtocolStringReplacer.getInstance().getReplacerManager().getReplacerConfigList()) {
            if (replacerConfig.getListenTypeList().contains(listenType)) {
                switch (replacerConfig.getMatchMode()) {
                    case CONTAIN:
                        text = containResult(results, text, replacerConfig, replacesMode);
                        break;
                    case EQUAL:
                        text = equalResult(results, text, replacerConfig, replacesMode);
                        break;
                    case REGEX:
                        text = regexResult(results, text, replacerConfig, replacesMode);
                        break;
                    default:
                }
            }
        }

        ComponentBuilder placeholderMessage = new ComponentBuilder(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Result.PAPI-Replace.Start-Prefix"));
        if (ProtocolStringReplacer.getInstance().getReplacerManager().hasPlaceholder(text)) {
            String original1 = text;
            User placeholderTarget = player == null ? ProtocolStringReplacer.getInstance().getUserManager().getConsoleUser() :
                    ProtocolStringReplacer.getInstance().getUserManager().getUser(player);
            text = ProtocolStringReplacer.getInstance().getReplacerManager().setPlaceholder(placeholderTarget, text);
            placeholderMessage.append(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Result.PAPI-Replace.Replaced")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(
                    PSRLocalization.getLocaledMessage("Sender.Commands.Parse.PAPI-Replace-Info",
                            placeholderTarget.getPlayer() == null ? "§7§onull" : placeholderTarget.getPlayer().getName(),
                            ColorUtils.showColorCodes(original1), ColorUtils.showColorCodes(text)))));
        } else {
            placeholderMessage.append(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Result.PAPI-Replace.Not-Replaced"));
        }

        double duration = (System.nanoTime() - startTime) / 1000000d;
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Result.Header"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Result.Duration", String.valueOf(duration)));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Result.Listen-Type", listenType.getName()));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Result.Match-Mode",
                PSRLocalization.getLocaledMessage(replacesMode.getLocaleKey())));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Result.Original-Text",
                ColorUtils.showColorCodes(original)));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Result.Final-Text",
                ColorUtils.showColorCodes(text)));
        ComponentBuilder componentBuilder = new ComponentBuilder(PSRLocalization
                .getLocaledMessage("Sender.Commands.Parse.Result.Steps.Start-Prefix"));
        if (results.isEmpty()) {
            componentBuilder.append(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Result.Steps.Noting-Replaced"));
        } else {
            for (int i = 0; i < results.size(); i++) {
                componentBuilder.append(" " + (i + 1) + " ");
                if (i % 2 == 1) {
                    componentBuilder.color(ChatColor.YELLOW);
                } else {
                    componentBuilder.color(ChatColor.GOLD);
                }
                componentBuilder.event(results.get(i)).append("|").reset();
            }
            componentBuilder.append(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Result.Steps.Hover-To-View-Info")).reset();
        }
        user.sendFilteredMessage(componentBuilder.create());
        user.sendFilteredMessage(placeholderMessage.create());
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Result.Footer"));
    }

    @NotNull
    private String containResult(@NotNull ArrayList<HoverEvent> results, @NotNull String text,
                               @NotNull ReplacerConfig replacerConfig, @NotNull ReplacesMode replacesMode) {
        replacerConfig.getReplacesStringSearcher(replacesMode);
        int i = 0;

        StringBuilder resultBuilder = new StringBuilder();
        for (Emit<String> emit : replacerConfig.getReplacesStringSearcher(replacesMode).parseText(text)) {
            if (emit.getStart() > i) {
                resultBuilder.append(text.subSequence(i, emit.getStart()));
            }
            resultBuilder.append(replacerConfig.getReplaces(replacesMode).get(emit.getSearchString()));
            i = emit.getEnd() + 1;
            results.add(new HoverEvent(HoverEvent.Action.SHOW_TEXT, createReplaceResultInfo(results, replacerConfig, replacesMode,
                    emit.getSearchString(), (String) replacerConfig.getReplaces(replacesMode).get(emit.getSearchString()),
                    resultBuilder + (i < text.length() ? text.substring(i) : ""))));
        }

        if (i < text.length()) {
            resultBuilder.append(text.subSequence(i, text.length()));
        }
        return resultBuilder.toString();
    }

    @NotNull
    private String equalResult(@NotNull ArrayList<HoverEvent> results, @NotNull String text,
                             @NotNull ReplacerConfig replacerConfig, @NotNull ReplacesMode replacesMode) {
        String result = text;
        Collection<Emit<String>> emits = replacerConfig.getReplacesStringSearcher(replacesMode).parseText(text);
        if (emits.size() == 1) {
            Emit<String> emit = emits.iterator().next();
            if (emit.getStart() == 0 && emit.getEnd() + 1 == text.length()) {
                result = (String) replacerConfig.getReplaces(replacesMode).get(emit.getSearchString());
                results.add(new HoverEvent(HoverEvent.Action.SHOW_TEXT, createReplaceResultInfo(results, replacerConfig, replacesMode,
                        emit.getSearchString(), result, result)));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private String regexResult(@NotNull ArrayList<HoverEvent> results, @NotNull String text,
                             @NotNull ReplacerConfig replacerConfig, @NotNull ReplacesMode replacesMode) {
        String result = text;
        Set<Map.Entry<Pattern, String>> containSet = (Set<Map.Entry<Pattern, String>>) replacerConfig.getReplaces(replacesMode).entrySet();
        for (Map.Entry<Pattern, String> entry : containSet) {
            Pattern key = entry.getKey();
            String value = entry.getValue();
            Matcher matcher = key.matcher(result);
            while (matcher.find()) {
                result = matcher.replaceAll(value);
                results.add(new HoverEvent(HoverEvent.Action.SHOW_TEXT, createReplaceResultInfo(results, replacerConfig, replacesMode,
                        key.toString(), value, result)));
            }
        }
        return result;
    }

    @NotNull
    private BaseComponent[] createReplaceResultInfo(@NotNull ArrayList<HoverEvent> results, @NotNull ReplacerConfig replacerConfig, @NotNull ReplacesMode replacesMode,
                                                  @NotNull String original, @NotNull String replacement, @NotNull String result) {
        return TextComponent.fromLegacyText(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Replace-Result-Info",
                String.valueOf(results.size() + 1), replacerConfig.getRelativePath(),
                PSRLocalization.getLocaledMessage(replacesMode.getLocaleKey()), ColorUtils.showColorCodes(original),
                ColorUtils.showColorCodes(replacement), ColorUtils.showColorCodes(result)));
    }

    @Override
    public List<String> onTab(@NotNull User user, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 2) {
            list.add(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Tab-Complete.String-To-Parse"));
        } else if (args.length == 3) {
            list.add(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Tab-Complete.PAPI-Target"));
            for (Player player : Bukkit.getOnlinePlayers()) {
                list.add(player.getName());
            }
            list.add("null");
        } else if (args.length == 4) {
            list.add("<" + PSRLocalization.getLocaledMessage("Variables.Listen-Type.Name") +">");
            for (ListenType listenType : ListenType.values()) {
                list.add(listenType.getName());
            }
        } else if (args.length == 5) {
            list.add("<" + PSRLocalization.getLocaledMessage("Variables.Match-Mode.Name") + ">");
            for (ReplacesMode replacesMode : ReplacesMode.values()) {
                list.add(replacesMode.getNode());
            }
        }
        return list;
    }

    @Override
    public void sendHelp(@NotNull User user) {
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Help.Header"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Help.Line-1"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Help.Line-2"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Help.Line-3"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Help.Line-4"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Parse.Help.Footer"));
    }

}
