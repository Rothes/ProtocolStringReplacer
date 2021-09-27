package me.rothes.protocolstringreplacer.commands.subcommands.editchildren;

import me.rothes.protocolstringreplacer.PSRLocalization;
import me.rothes.protocolstringreplacer.commands.SubCommand;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplacesMode;
import me.rothes.protocolstringreplacer.user.User;
import me.rothes.protocolstringreplacer.utils.ArgUtils;
import me.rothes.protocolstringreplacer.utils.ColorUtils;
import me.rothes.protocolstringreplacer.utils.MessageUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Block extends SubCommand {

    public Block() {
        super("block", "protocolstringreplacer.command.edit",
                PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Description"));
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
                        "Enum.Match-Mode.Messages.Invaild-Mode", args[3]));
                return;
            }
            List<Object> blocks = user.getEditorReplacerConfig().getBlocks(replacesMode);
            int totalPage = (int) Math.ceil((float) blocks.size() / 10);
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

            user.sendFilteredText("§7§m-----------§7§l §7[ §c§lP§6§lS§3§lR §7- §e屏蔽项目列表§7 ]§l §7§m-----------");

            for (int i = (page - 1) * 10; i < blocks.size() && i < page * 10; i++) {
                String block = blocks.get(i).toString();

                user.sendFilteredMessage(new ComponentBuilder("§6[+] ").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/psr edit block add " + replacesMode.getNode() + " " + i + " <屏蔽文本>"))
                        .append("§6[编辑] ").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/psr edit block set " + replacesMode.getNode() + " " + i + " "
                                        + ArgUtils.formatWithQuotes(ColorUtils.restoreColored(block))))
                                .append("§c[删除]").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/psr edit block remove " + replacesMode.getNode() + " " + i))
                        .append(" " + i + ". ").reset().append(ColorUtils.showColorCodes(block)).color(ChatColor.AQUA).create());
            }

            MessageUtils.sendPageButtons(user, "/psr edit block list " + replacesMode.getNode() + " ", page, totalPage);

            user.sendFilteredText("§7§m-----------------------------------------------");
        } else {
            user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.List.Detailed-Help"));
        }
    }

    private void setCommand(@Nonnull User user, @NotNull String[] args) {
        if (args.length > 4) {
            ReplacesMode replacesMode = getReplacesMode(args[3]);
            if (replacesMode == null) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Enum.Match-Mode.Messages.Invaild-Mode", args[3]));
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
                if (index >= editorReplacerConfig.getBlocks(replacesMode).size()) {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Error.Index-Exceed",
                            String.valueOf(editorReplacerConfig.getBlocks(replacesMode).size())));
                    return;
                }

                String block = ColorUtils.getColored(args[5]);
                editorReplacerConfig.setBlock(index, block, replacesMode);
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Edit.Children.Block.Children.Set.Successfully-Set-Block",
                        args[4], ColorUtils.showColorCodes(block)));

                return;
            }
        }
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.Set.Detailed-Help"));
    }

    private void addCommand(@Nonnull User user, @NotNull String[] args) {
        if (args.length > 3) {
            ReplacesMode replacesMode = getReplacesMode(args[3]);
            if (replacesMode == null) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Enum.Match-Mode.Messages.Invaild-Mode", args[3]));
                return;
            }
            if (args.length == 5) {
                String block = ColorUtils.getColored(args[4]);
                ReplacerConfig editorReplacerConfig = user.getEditorReplacerConfig();
                editorReplacerConfig.addBlock(block, replacesMode);
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Edit.Children.Block.Children.Add.Successfully-Added-Block",
                        String.valueOf(editorReplacerConfig.getBlocks(replacesMode).size()),
                        ColorUtils.showColorCodes(block)));
            } else if (args.length == 6) {
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
                String block = ColorUtils.getColored(args[5]);
                user.getEditorReplacerConfig().addBlock(index, block, replacesMode);
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Edit.Children.Block.Children.Add.Successfully-Added-Block",
                        String.valueOf(index),
                        ColorUtils.showColorCodes(block)));
            }
        } else {
            user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.Add.Detailed-Help"));
        }
    }

    private void removeCommand(@Nonnull User user, @NotNull String[] args) {
        if (args.length > 3) {
            ReplacesMode replacesMode = getReplacesMode(args[3]);
            if (replacesMode == null) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Enum.Match-Mode.Messages.Invaild-Mode", args[3]));
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
                if (index > editorReplacerConfig.getBlocks(replacesMode).size()) {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage("Sender.Error.Index-Exceed",
                            String.valueOf(editorReplacerConfig.getBlocks(replacesMode).size())));
                    return;
                }
                editorReplacerConfig.removeBlock(index, replacesMode);
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Edit.Children.Block.Children.Remove.Sucessfully-Removed-Block", String.valueOf(index)));
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
            list.add("<" + PSRLocalization.getLocaledMessage("Enum.Match-Mode.Name") + ">");
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
                        "<" + PSRLocalization.getLocaledMessage("Enum.Block-Text.Name") + ">");
            } else if (args[2].equalsIgnoreCase("remove")) {
                list.add("<" + PSRLocalization.getLocaledMessage("Enum.Index.Name") + ">");
            }
        } else if (args.length == 6) {
            if (args[2].equalsIgnoreCase("set")) {
                list.add("<" + PSRLocalization.getLocaledMessage("Enum.Block-Text.Name") + ">");
            } else if (args[2].equalsIgnoreCase("add") && StringUtils.isNumeric(args[3])) {
                list.add("<" + PSRLocalization.getLocaledMessage("Enum.Block-Text.Name") + ">");
            }
        }
        return list;
    }

    @Override
    public void sendHelp(@Nonnull User user) {
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Help.Header"));
        user.sendFilteredText("§7 * §e/psr edit block help §7- §b" +
                PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Help.Help-Description"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.List.Simple-Help"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.Set.Simple-Help"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.Add.Simple-Help"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Children.Remove.Simple-Help"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.Block.Help.Footer"));
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
