package me.rothes.protocolstringreplacer.commands.subcommands.editchildren;

import me.rothes.protocolstringreplacer.PSRLocalization;
import me.rothes.protocolstringreplacer.utils.ArgUtils;
import me.rothes.protocolstringreplacer.utils.ColorUtils;
import me.rothes.protocolstringreplacer.commands.SubCommand;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplacesMode;
import me.rothes.protocolstringreplacer.user.User;
import me.rothes.protocolstringreplacer.utils.MessageUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class Replace extends SubCommand {

    private static final Pattern commentValuePrefix = Pattern.compile("[0-9]+\\| ");

    public Replace() {
        super("replace", "protocolstringreplacer.command.edit",
                PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Description"));
    }

    @Override
    public void onExecute(@Nonnull User user, @NotNull String[] args) {
        if (user.getEditorReplacerConfig() == null) {
            user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage("Sender.Commands.Edit.Children.Replace.Not-Selected-Replacer-Config"));
            return;
        }

        if (args.length > 2) {
            if ("list".equalsIgnoreCase(args[2])) {
                listCommand(user, args);
                return;
            } else if ("set".equalsIgnoreCase(args[2])) {
                setCommand(user, args);
                return;

            } else if ("add".equalsIgnoreCase(args[2])) {
                addCommand(user, args);
                return;

            } else if ("remove".equalsIgnoreCase(args[2])) {
                removeCommand(user, args);
                return;
            }
        }
        sendHelp(user);
    }

    private void listCommand(@Nonnull User user, @NotNull String[] args) {
        if (args.length < 6 && args.length > 3) {
            int page = 1;
            ReplacesMode replacesMode = getReplacesMode(args[3]);
            if (replacesMode == null) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Enum.Match-Type.Messages.Invaild-Type", args[3]));
                return;
            }
            ListOrderedMap replaces = user.getEditorReplacerConfig().getReplaces(replacesMode);
            HashMap<Short, LinkedList<ReplacerConfig.CommentLine>> commentLinesMap = user.getEditorReplacerConfig().getCommentLines(replacesMode);
            int totalPage = (int) Math.ceil((float) replaces.size() / 5);
            if (args.length == 5) {
                if (StringUtils.isNumeric(args[4])) {
                    page = Integer.parseInt(args[4]);
                } else {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Error.Not-A-Positive-Integer", args[4]));
                    return;
                }
            }

            if (page > totalPage) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Error.Page-Exceed", String.valueOf(totalPage)));
                return;
            }
            if (page < 1) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage("Sender.Error.Page-Low"));
                return;
            }

            user.sendFilteredText("§7§m-----------§7§l §7[ §c§lP§6§lS§3§lR §7- §e替换项目列表§7 ]§l §7§m-----------");

            for (int i = (page - 1) * 5; i < replaces.size() && i < page * 5; i++) {
                String original = replaces.get(i).toString();
                String replacement = (String) replaces.get(replaces.get(i));
                LinkedList<ReplacerConfig.CommentLine> commentLines = commentLinesMap.get((short) i);
                ComponentBuilder hoverBuilder = new ComponentBuilder("§6§l注释内容:").color(ChatColor.GREEN);
                if (commentLines != null && !commentLines.isEmpty()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (ReplacerConfig.CommentLine commentLine : commentLines) {
                        stringBuilder.insert(0, commentValuePrefix.matcher(commentLine.getValue()).replaceFirst("")).insert(0, '\n');
                    }
                    hoverBuilder.append(stringBuilder.toString());
                } else {
                    hoverBuilder.append("\n§7无");
                }

                user.sendFilteredMessage(new ComponentBuilder("§6[+] ").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/psr edit replace add " + replacesMode.getNode() + " " + i + " <原文本> <新文本>")).
                        event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create())).
                        append("§6[编辑]").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/psr edit replace set " + replacesMode.getNode() + " " + i + " " + ArgUtils.formatWithQuotes(ColorUtils.restoreColored(original)) + " " + ArgUtils.formatWithQuotes(ColorUtils.restoreColored(replacement)))).append(" " + i + ". ").
                        reset().event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create())).
                        append(ColorUtils.showColorCodes(original)).color(ChatColor.AQUA).create());
                user.sendFilteredMessage(new ComponentBuilder("§c[删除]").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/psr edit replace remove " + replacesMode.getNode() + " " + i)).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create())).
                        append(" §7§o==> ").reset().event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create())).append(ColorUtils.showColorCodes(replacement)).color(ChatColor.BLUE).create());
            }

            MessageUtils.sendPageButtons(user, "/psr edit replace list " + replacesMode.getNode() + " ", page, totalPage);

            user.sendFilteredText("§7§m-----------------------------------------------");
        } else {
            user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.List.Detailed-Help"));
        }
    }

    private void setCommand(@Nonnull User user, @NotNull String[] args) {
        if (args.length > 4) {
            ReplacesMode replacesMode = getReplacesMode(args[3]);
            if (replacesMode == null) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Enum.Match-Type.Messages.Invaild-Type", args[3]));
                return;
            }
            if (!StringUtils.isNumeric(args[4])) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Error.Not-A-Positive-Integer", args[4]));
                return;
            }
            int index = Integer.parseInt(args[4]);
            ReplacerConfig editorReplacerConfig = user.getEditorReplacerConfig();
            if (index < 0) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage("Sender.Error.Index-Low"));
                return;
            }

            if (args.length == 6) {
                if (index >= editorReplacerConfig.getReplaces(replacesMode).size()) {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Error.Index-Exceed",
                            String.valueOf(editorReplacerConfig.getReplaces(replacesMode).size())));
                    return;
                }

                String replacement = ColorUtils.getColored(args[5]);
                editorReplacerConfig.setReplace(index, replacement, replacesMode);
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Edit.Children.Replace.Children.Set.Index-Set",
                                args[4],
                        (String) editorReplacerConfig.getReplaces(replacesMode).get(index),
                                replacement));

            } else if (args.length == 7) {
                if (index > editorReplacerConfig.getReplaces(replacesMode).size()) {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage("Sender.Error.Index-Exceed",
                                    String.valueOf(editorReplacerConfig.getReplaces(replacesMode).size())));
                    return;
                }
                String original = ColorUtils.getColored(args[5]);
                int i = editorReplacerConfig.checkReplaceKey(original, replacesMode);
                if (i == -1 || i == index) {
                    String replacement = ColorUtils.getColored(args[6]);
                    editorReplacerConfig.setReplace(index, original, replacement, replacesMode);
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Edit.Children.Replace.Children.Set.Successfully-Set-Replace",
                                    args[4], original, replacement));
                } else {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Edit.Children.Replace.Children.Set.Same-Original-Text", String.valueOf(i)));
                }
            } else {
                user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.Set.Detailed-Help"));
            }
        } else {
            user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.Set.Detailed-Help"));
        }
    }

    private void addCommand(@Nonnull User user, @NotNull String[] args) {
        if (args.length > 3) {
            ReplacesMode replacesMode = getReplacesMode(args[3]);
            if (replacesMode == null) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Enum.Match-Type.Messages.Invaild-Type", args[3]));
                return;
            }
            if (args.length == 6) {
                String original = ColorUtils.getColored(args[4]);
                ReplacerConfig editorReplacerConfig = user.getEditorReplacerConfig();
                int i = editorReplacerConfig.checkReplaceKey(original, replacesMode);
                if (i == -1) {
                    String replacement = ColorUtils.getColored(args[5]);
                    editorReplacerConfig.addReplace(original, replacement, replacesMode);
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Edit.Children.Replace.Children.Add.Successfully-Added-Replace",
                                    String.valueOf(editorReplacerConfig.getReplaces(replacesMode).size()),
                                    original,
                                    replacement));
                } else {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Edit.Children.Replace.Children.Add.Same-Original-Text"));
                }
            } else if (args.length == 7) {
                if (!StringUtils.isNumeric(args[4])) {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Error.Not-A-Positive-Integer", args[4]));
                    return;
                }
                int index = Integer.parseInt(args[4]);
                if (index < 0) {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage("Sender.Error.Index-Low"));
                    return;
                }
                String original = ColorUtils.getColored(args[5]);
                ReplacerConfig editorReplacerConfig = user.getEditorReplacerConfig();
                int i = editorReplacerConfig.checkReplaceKey(original, replacesMode);
                if (i == -1) {
                    String replacement = ColorUtils.getColored(args[6]);
                    user.getEditorReplacerConfig().addReplace(index, original, replacement, replacesMode);
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Edit.Children.Replace.Children.Add.Successfully-Added-Replace",
                                    String.valueOf(index),
                                    original,
                                    replacement));
                } else {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Edit.Children.Replace.Children.Add.Same-Original-Text", String.valueOf(i)));
                }
            }
        } else {
            user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.Add.Detailed-Help"));
        }
    }

    private void removeCommand(@Nonnull User user, @NotNull String[] args) {
        if (args.length > 3) {
            ReplacesMode replacesMode = getReplacesMode(args[3]);
            if (replacesMode == null) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Enum.Match-Type.Messages.Invaild-Type", args[3]));
                return;
            }
            if (args.length == 5) {
                if (!StringUtils.isNumeric(args[4])) {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Error.Not-A-Positive-Integer", args[4]));
                    return;
                }
                int index = Integer.parseInt(args[4]);
                if (index < 0) {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage("Sender.Error.Index-Low"));
                    return;
                }
                ReplacerConfig editorReplacerConfig = user.getEditorReplacerConfig();
                if (index > editorReplacerConfig.getReplaces(replacesMode).size()) {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage("Sender.Error.Index-Exceed",
                                    String.valueOf(editorReplacerConfig.getReplaces(replacesMode).size())));
                    return;
                }
                editorReplacerConfig.removeReplace(index, replacesMode);
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Edit.Children.Replace.Children.Remove.Sucessfully-Removed-Replace", String.valueOf(index)));
            }
        } else {
            user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.Remove.Detailed-Help"));
        }
    }

    @Override
    public List<String> onTab(@NotNull User user, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 3) {
            list = Arrays.asList("help", "list", "set", "add", "remove");
        } else if (args.length == 4
                && (args[2].equalsIgnoreCase("list") || args[2].equalsIgnoreCase("set")
                || args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("remove"))) {
            list.add("<" + PSRLocalization.getLocaledMessage("Enum.Match-Type.Name") + ">");
            for (ReplacesMode replacesMode : ReplacesMode.values()) {
                list.add(replacesMode.getNode());
            }
        } else if (args.length == 5) {
            if (args[2].equalsIgnoreCase("list")) {
                list.add("[" + PSRLocalization.getLocaledMessage("Enum.Page.Name") + "]");
            } else if (args[2].equalsIgnoreCase("set")) {
                list.add("<" + PSRLocalization.getLocaledMessage("Enum.Index.Name") + ">");
            } else if (args[2].equalsIgnoreCase("add")) {
                list.add("[" + PSRLocalization.getLocaledMessage("Enum.Index.Name") +"]|" +
                        "<" + PSRLocalization.getLocaledMessage("Enum.Original-Text.Name") + ">");
            } else if (args[2].equalsIgnoreCase("remove")) {
                list.add("<" + PSRLocalization.getLocaledMessage("Enum.Index.Name") + ">");
            }
        } else if (args.length == 6) {
            if (args[2].equalsIgnoreCase("set")) {
                list.add("<" + PSRLocalization.getLocaledMessage("Enum.Original-Text.Name") + ">");
            } else if (args[2].equalsIgnoreCase("add")) {
                if (StringUtils.isNumeric(args[3])) {
                    list.add("<" + PSRLocalization.getLocaledMessage("Enum.Original-Text.Name") + ">");
                } else {
                    list.add("<" + PSRLocalization.getLocaledMessage("Enum.Replacement-Text.Name") + ">");
                }
            }
        } else if (args.length == 7) {
            if (args[2].equalsIgnoreCase("set")) {
                list.add("<" + PSRLocalization.getLocaledMessage("Enum.Replacement-Text.Name") + ">");
            } else if (args[2].equalsIgnoreCase("add") && StringUtils.isNumeric(args[3])) {
                list.add("<" + PSRLocalization.getLocaledMessage("Enum.Replacement-Text.Name") + ">");
            }
        }
        return list;
    }

    @Override
    public void sendHelp(@Nonnull User user) {
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Help.Header"));
        user.sendFilteredText("§7 * §e/psr edit replace help §7- §b" +
                PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Help.Help-Description"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.List.Simple-Help"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.Set.Simple-Help"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.Add.Simple-Help"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.Remove.Simple-Help"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Help.Footer"));
    }

    private ReplacesMode getReplacesMode(@NotNull String string) {
        for (ReplacesMode type : ReplacesMode.values()) {
            if (type.getNode().equalsIgnoreCase(string)) {
                return type;
            }
        }
        return null;
    }

}
