package me.rothes.protocolstringreplacer.commands.subcommands.editchildren;

import me.rothes.protocolstringreplacer.PsrLocalization;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.utils.ArgUtils;
import me.rothes.protocolstringreplacer.utils.ColorUtils;
import me.rothes.protocolstringreplacer.commands.SubCommand;
import me.rothes.protocolstringreplacer.replacer.FileReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplaceMode;
import me.rothes.protocolstringreplacer.utils.MessageUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Replace extends SubCommand {

    public Replace() {
        super("replace", "protocolstringreplacer.command.edit",
                PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Description"));
    }

    @Override
    public void onExecute(@Nonnull PsrUser user, @NotNull String[] args) {
        if (user.getEditorReplacerConfig() == null) {
            user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Commands.Edit.Children.Replace.Not-Selected-Replacer-Config"));
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

    private void listCommand(@Nonnull PsrUser user, @NotNull String[] args) {
        if (args.length < 6 && args.length > 3) {
            int page = 1;
            ReplaceMode replaceMode = getReplacesMode(args[3]);
            if (replaceMode == null) {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Variables.Match-Mode.Messages.Invalid-Mode", args[3]));
                return;
            }
            ListOrderedMap replaces = user.getEditorReplacerConfig().getReplaces(replaceMode);
            int totalPage = (int) Math.ceil((float) replaces.size() / 5);
            if (args.length == 5) {
                if (StringUtils.isNumeric(args[4])) {
                    page = Integer.parseInt(args[4]);
                } else {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                            "Sender.Error.Not-A-Positive-Integer", args[4]));
                    return;
                }
            }

            if (page > totalPage) {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Sender.Error.Page-Exceed", String.valueOf(totalPage)));
                return;
            }
            if (page < 1) {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Error.Page-Low"));
                return;
            }

            user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.List.Result.Header"));

            for (int i = (page - 1) * 5; i < replaces.size() && i < page * 5; i++) {
                String original = replaces.get(i).toString();
                String replacement = (String) replaces.get(replaces.get(i));

                user.sendFilteredMessage(new ComponentBuilder(PsrLocalization.getLocaledMessage("Utils.Message.Buttons.Add"))
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                        "/psr edit replace add " + replaceMode.getNode() + " " + i + " <" +
                                PsrLocalization.getLocaledMessage("Variables.Original-Text") + "> <" +
                                PsrLocalization.getLocaledMessage("Variables.Replacement-Text") + ">"))
                        .append(PsrLocalization.getLocaledMessage("Utils.Message.Buttons.Edit"))
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/psr edit replace set " + replaceMode.getNode() + " " + i + " "
                                        + ArgUtils.formatWithQuotes(ColorUtils.restoreColored(original))
                                        + " " + ArgUtils.formatWithQuotes(ColorUtils.restoreColored(replacement))))
                        .append(" " + i + ". ").reset().append(ColorUtils.showColorCodes(original)).color(ChatColor.AQUA).create());
                user.sendFilteredMessage(new ComponentBuilder(PsrLocalization.getLocaledMessage("Utils.Message.Buttons.Delete"))
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/psr edit replace remove " + replaceMode.getNode() + " " + i))
                        .append(" §7§o==> ").reset().append(ColorUtils.showColorCodes(replacement)).color(ChatColor.BLUE).create());
            }

            MessageUtils.sendPageButtons(user, "/psr edit replace list " + replaceMode.getNode() + " ", page, totalPage);

            user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.List.Result.Footer"));
        } else {
            user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.List.Detailed-Help"));
        }
    }

    private void setCommand(@Nonnull PsrUser user, @NotNull String[] args) {
        if (args.length > 4) {
            ReplaceMode replaceMode = getReplacesMode(args[3]);
            if (replaceMode == null) {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Variables.Match-Mode.Messages.Invalid-Mode", args[3]));
                return;
            }
            if (!StringUtils.isNumeric(args[4])) {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Sender.Error.Not-A-Positive-Integer", args[4]));
                return;
            }
            int index = Integer.parseInt(args[4]);
            FileReplacerConfig editorReplacerConfig = user.getEditorReplacerConfig();
            if (index < 0) {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Error.Index-Low"));
                return;
            }

            if (args.length == 6) {
                if (index >= editorReplacerConfig.getReplaces(replaceMode).size()) {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                            "Sender.Error.Index-Exceed",
                            String.valueOf(editorReplacerConfig.getReplaces(replaceMode).size())));
                    return;
                }

                String replacement = ColorUtils.getColored(args[5]);
                editorReplacerConfig.setReplace(index, replacement, replaceMode);
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Edit.Children.Replace.Children.Set.Successfully-Set-Replace", args[4],
                        ColorUtils.showColorCodes((String) editorReplacerConfig.getReplaces(replaceMode).get(index)),
                        ColorUtils.showColorCodes(replacement)));

            } else if (args.length == 7) {
                if (index > editorReplacerConfig.getReplaces(replaceMode).size()) {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Error.Index-Exceed",
                            String.valueOf(editorReplacerConfig.getReplaces(replaceMode).size())));
                    return;
                }
                String original = ColorUtils.getColored(args[5]);
                int i = editorReplacerConfig.checkReplaceKey(original, replaceMode);
                if (i == -1 || i == index) {
                    String replacement = ColorUtils.getColored(args[6]);
                    editorReplacerConfig.setReplace(index, original, replacement, replaceMode);
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Edit.Children.Replace.Children.Set.Successfully-Set-Replace",
                                    args[4], ColorUtils.showColorCodes(original), ColorUtils.showColorCodes(replacement)));
                } else {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Edit.Children.Replace.Children.Same-Original-Text", String.valueOf(i)));
                }
            } else {
                user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.Set.Detailed-Help"));
            }
        } else {
            user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.Set.Detailed-Help"));
        }
    }

    private void addCommand(@Nonnull PsrUser user, @NotNull String[] args) {
        if (args.length > 3) {
            ReplaceMode replaceMode = getReplacesMode(args[3]);
            if (replaceMode == null) {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Variables.Match-Mode.Messages.Invalid-Mode", args[3]));
                return;
            }
            if (args.length == 6) {
                String original = ColorUtils.getColored(args[4]);
                FileReplacerConfig editorReplacerConfig = user.getEditorReplacerConfig();
                int i = editorReplacerConfig.checkReplaceKey(original, replaceMode);
                if (i == -1) {
                    String replacement = ColorUtils.getColored(args[5]);
                    editorReplacerConfig.addReplace(original, replacement, replaceMode);
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Edit.Children.Replace.Children.Add.Successfully-Added-Replace",
                                    String.valueOf(editorReplacerConfig.getReplaces(replaceMode).size()),
                                    ColorUtils.showColorCodes(original),
                                    ColorUtils.showColorCodes(replacement)));
                } else {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Edit.Children.Replace.Children.Same-Original-Text"));
                }
            } else if (args.length == 7) {
                if (!StringUtils.isNumeric(args[4])) {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                            "Sender.Error.Not-A-Positive-Integer", args[4]));
                    return;
                }
                int index = Integer.parseInt(args[4]);
                if (index < 0) {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Error.Index-Low"));
                    return;
                }
                String original = ColorUtils.getColored(args[5]);
                FileReplacerConfig editorReplacerConfig = user.getEditorReplacerConfig();
                int i = editorReplacerConfig.checkReplaceKey(original, replaceMode);
                if (i == -1) {
                    String replacement = ColorUtils.getColored(args[6]);
                    editorReplacerConfig.addReplace(index, original, replacement, replaceMode);
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Edit.Children.Replace.Children.Add.Successfully-Added-Replace",
                                    String.valueOf(index),
                                    ColorUtils.showColorCodes(original),
                                    ColorUtils.showColorCodes(replacement)));
                } else {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Edit.Children.Replace.Children.Same-Original-Text", String.valueOf(i)));
                }
            }
        } else {
            user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.Add.Detailed-Help"));
        }
    }

    private void removeCommand(@Nonnull PsrUser user, @NotNull String[] args) {
        if (args.length > 3) {
            ReplaceMode replaceMode = getReplacesMode(args[3]);
            if (replaceMode == null) {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Variables.Match-Mode.Messages.Invalid-Mode", args[3]));
                return;
            }
            if (args.length == 5) {
                if (!StringUtils.isNumeric(args[4])) {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                            "Sender.Error.Not-A-Positive-Integer", args[4]));
                    return;
                }
                int index = Integer.parseInt(args[4]);
                if (index < 0) {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Error.Index-Low"));
                    return;
                }
                FileReplacerConfig editorReplacerConfig = user.getEditorReplacerConfig();
                if (index > editorReplacerConfig.getReplaces(replaceMode).size()) {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Error.Index-Exceed",
                                    String.valueOf(editorReplacerConfig.getReplaces(replaceMode).size())));
                    return;
                }
                editorReplacerConfig.removeReplace(index, replaceMode);
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Edit.Children.Replace.Children.Remove.Sucessfully-Removed-Replace", String.valueOf(index)));
            }
        } else {
            user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.Remove.Detailed-Help"));
        }
    }

    @Override
    public List<String> onTab(@NotNull PsrUser user, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 3) {
            list = Arrays.asList("help", "list", "set", "add", "remove");
        } else if (args.length == 4
                && (args[2].equalsIgnoreCase("list") || args[2].equalsIgnoreCase("set")
                || args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("remove"))) {
            list.add("<" + PsrLocalization.getLocaledMessage("Variables.Match-Mode.Name") + ">");
            for (ReplaceMode replaceMode : ReplaceMode.values()) {
                list.add(replaceMode.getNode());
            }
        } else if (args.length == 5) {
            if (args[2].equalsIgnoreCase("list")) {
                list.add("[" + PsrLocalization.getLocaledMessage("Variables.Page.Name") + "]");
            } else if (args[2].equalsIgnoreCase("set")) {
                list.add("<" + PsrLocalization.getLocaledMessage("Variables.Index.Name") + ">");
            } else if (args[2].equalsIgnoreCase("add")) {
                list.add("[" + PsrLocalization.getLocaledMessage("Variables.Index.Name") +"]|" +
                        "<" + PsrLocalization.getLocaledMessage("Variables.Original-Text.Name") + ">");
            } else if (args[2].equalsIgnoreCase("remove")) {
                list.add("<" + PsrLocalization.getLocaledMessage("Variables.Index.Name") + ">");
            }
        } else if (args.length == 6) {
            if (args[2].equalsIgnoreCase("set")) {
                list.add("<" + PsrLocalization.getLocaledMessage("Variables.Original-Text.Name") + ">");
            } else if (args[2].equalsIgnoreCase("add")) {
                if (StringUtils.isNumeric(args[3])) {
                    list.add("<" + PsrLocalization.getLocaledMessage("Variables.Original-Text.Name") + ">");
                } else {
                    list.add("<" + PsrLocalization.getLocaledMessage("Variables.Replacement-Text.Name") + ">");
                }
            }
        } else if (args.length == 7) {
            if (args[2].equalsIgnoreCase("set")) {
                list.add("<" + PsrLocalization.getLocaledMessage("Variables.Replacement-Text.Name") + ">");
            } else if (args[2].equalsIgnoreCase("add") && StringUtils.isNumeric(args[3])) {
                list.add("<" + PsrLocalization.getLocaledMessage("Variables.Replacement-Text.Name") + ">");
            }
        }
        return list;
    }

    @Override
    public void sendHelp(@Nonnull PsrUser user) {
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Help.Header"));
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.List.Simple-Help"));
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.Set.Simple-Help"));
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.Add.Simple-Help"));
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Children.Remove.Simple-Help"));
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Replace.Help.Footer"));
    }

    private ReplaceMode getReplacesMode(@NotNull String string) {
        for (ReplaceMode type : ReplaceMode.values()) {
            if (type.getNode().equalsIgnoreCase(string)) {
                return type;
            }
        }
        return null;
    }

}
