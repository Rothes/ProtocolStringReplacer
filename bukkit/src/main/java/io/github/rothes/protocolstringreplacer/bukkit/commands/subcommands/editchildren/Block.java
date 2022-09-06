package io.github.rothes.protocolstringreplacer.bukkit.commands.subcommands.editchildren;

import io.github.rothes.protocolstringreplacer.bukkit.PsrLocalization;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.bukkit.commands.SubCommand;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.FileReplacerConfig;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.ReplaceMode;
import io.github.rothes.protocolstringreplacer.bukkit.utils.ArgUtils;
import io.github.rothes.protocolstringreplacer.bukkit.utils.ColorUtils;
import io.github.rothes.protocolstringreplacer.bukkit.utils.MessageUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Block extends SubCommand {

    public Block() {
        super("block", "protocolstringreplacer.command.edit",
                PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Description"));
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
            List<Object> blocks = user.getEditorReplacerConfig().getBlocks(replaceMode);
            int totalPage = (int) Math.ceil((float) blocks.size() / 10);
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

            user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.List.Result.Header"));

            for (int i = (page - 1) * 10; i < blocks.size() && i < page * 10; i++) {
                String block = blocks.get(i).toString();

                user.sendFilteredMessage(new ComponentBuilder(PsrLocalization.getLocaledMessage("Utils.Message.Buttons.Add"))
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/psr edit block add " + replaceMode.getNode() + " " + i + " <"
                                        + PsrLocalization.getLocaledMessage("Variables.Block-Text") + ">"))
                        .append(PsrLocalization.getLocaledMessage("Utils.Message.Buttons.Edit"))
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/psr edit block set " + replaceMode.getNode() + " " + i + " "
                                        + ArgUtils.formatWithQuotes(ColorUtils.restoreColored(block))))
                                .append(PsrLocalization.getLocaledMessage("Utils.Message.Buttons.Delete"))
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/psr edit block remove " + replaceMode.getNode() + " " + i))
                        .append(" " + i + ". ").reset().append(ColorUtils.showColorCodes(block)).color(ChatColor.AQUA).create());
            }

            MessageUtils.sendPageButtons(user, "/psr edit block list " + replaceMode.getNode() + " ", page, totalPage);

            user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.List.Result.Footer"));
        } else {
            user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.List.Detailed-Help"));
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
                if (index >= editorReplacerConfig.getBlocks(replaceMode).size()) {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                            "Sender.Error.Index-Exceed",
                            String.valueOf(editorReplacerConfig.getBlocks(replaceMode).size())));
                    return;
                }

                String block = ColorUtils.getColored(args[5]);
                editorReplacerConfig.setBlock(index, block, replaceMode);
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Edit.Children.Block.Children.Set.Successfully-Set-Block",
                        args[4], ColorUtils.showColorCodes(block)));

                return;
            }
        }
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.Set.Detailed-Help"));
    }

    private void addCommand(@Nonnull PsrUser user, @NotNull String[] args) {
        if (args.length > 3) {
            ReplaceMode replaceMode = getReplacesMode(args[3]);
            if (replaceMode == null) {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Variables.Match-Mode.Messages.Invalid-Mode", args[3]));
                return;
            }
            if (args.length == 5) {
                String block = ColorUtils.getColored(args[4]);
                FileReplacerConfig editorReplacerConfig = user.getEditorReplacerConfig();
                editorReplacerConfig.addBlock(block, replaceMode);
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Edit.Children.Block.Children.Add.Successfully-Added-Block",
                        String.valueOf(editorReplacerConfig.getBlocks(replaceMode).size()),
                        ColorUtils.showColorCodes(block)));
            } else if (args.length == 6) {
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
                String block = ColorUtils.getColored(args[5]);
                user.getEditorReplacerConfig().addBlock(index, block, replaceMode);
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Edit.Children.Block.Children.Add.Successfully-Added-Block",
                        String.valueOf(index),
                        ColorUtils.showColorCodes(block)));
            }
        } else {
            user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.Add.Detailed-Help"));
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
                if (index > editorReplacerConfig.getBlocks(replaceMode).size()) {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Error.Index-Exceed",
                            String.valueOf(editorReplacerConfig.getBlocks(replaceMode).size())));
                    return;
                }
                editorReplacerConfig.removeBlock(index, replaceMode);
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Edit.Children.Block.Children.Remove.Sucessfully-Removed-Block", String.valueOf(index)));
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
                        "<" + PsrLocalization.getLocaledMessage("Variables.Block-Text.Name") + ">");
            } else if (args[2].equalsIgnoreCase("remove")) {
                list.add("<" + PsrLocalization.getLocaledMessage("Variables.Index.Name") + ">");
            }
        } else if (args.length == 6) {
            if (args[2].equalsIgnoreCase("set")) {
                list.add("<" + PsrLocalization.getLocaledMessage("Variables.Block-Text.Name") + ">");
            } else if (args[2].equalsIgnoreCase("add") && StringUtils.isNumeric(args[3])) {
                list.add("<" + PsrLocalization.getLocaledMessage("Variables.Block-Text.Name") + ">");
            }
        }
        return list;
    }

    @Override
    public void sendHelp(@Nonnull PsrUser user) {
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Help.Header"));
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.List.Simple-Help"));
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.Set.Simple-Help"));
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.Add.Simple-Help"));
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.Remove.Simple-Help"));
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Help.Footer"));
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
