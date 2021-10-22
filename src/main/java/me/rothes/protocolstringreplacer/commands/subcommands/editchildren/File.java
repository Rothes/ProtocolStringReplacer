package me.rothes.protocolstringreplacer.commands.subcommands.editchildren;

import me.rothes.protocolstringreplacer.PSRLocalization;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.api.user.User;
import me.rothes.protocolstringreplacer.utils.ArgUtils;
import me.rothes.protocolstringreplacer.api.configuration.DotYamlConfiguration;
import me.rothes.protocolstringreplacer.commands.SubCommand;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.utils.FileUtils;
import me.rothes.protocolstringreplacer.utils.MessageUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class File extends SubCommand {

    private static Pattern fileNamePrefix = Pattern.compile("(\")?Replacers/");

    public File() {
        super("file", "protocolstringreplacer.command.edit",
                PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Description"));
    }

    @Override
    public void onExecute(@Nonnull User user, @NotNull String[] args) {
        if (args.length > 2) {
            if ("list".equalsIgnoreCase(args[2])) {
                listCommand(user, args);
                return;

            } else if ("select".equalsIgnoreCase(args[2])) {
                selectCommand(user, args);
                return;

            } else if ("create".equalsIgnoreCase(args[2])) {
                createCommand(user, args);
                return;

            } else if ("delete".equalsIgnoreCase(args[2])) {
                deleteCommand(user, args);
                return;
            }
        }
        sendHelp(user);
    }

    private void listCommand(@Nonnull User user, @NotNull String[] args) {
        if (args.length < 5) {
            int page = 1;
            LinkedList<ReplacerConfig> replacerConfigList = ProtocolStringReplacer.getInstance().getReplacerManager().getReplacerConfigList();
            int totalPage = (int) Math.ceil((float) replacerConfigList.size() / 10);
            if (args.length == 4) {
                if (StringUtils.isNumeric(args[3])) {
                    page = Integer.parseInt(args[3]);
                } else {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Error.Not-A-Positive-Integer", args[3]));
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

            user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Children.List.Result.Header"));

            for (int i = (page - 1) * 10; i < replacerConfigList.size() && i < page * 10; i++) {
                ReplacerConfig replacerConfig = replacerConfigList.get(i);
                StringBuilder listens = new StringBuilder();
                for (ListenType listenType : replacerConfig.getListenTypeList()) {
                    listens.append("\n§7- §b").append(listenType.getName());
                }
                user.sendFilteredMessage(new ComponentBuilder(PSRLocalization.getLocaledMessage("Utils.Message.Buttons.Select"))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/psr edit file select " + i)).append(" " + i + ". ").reset().color(ChatColor.WHITE).
                        append(replacerConfig.getRelativePath()).color(ChatColor.AQUA)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT
                                , TextComponent.fromLegacyText(PSRLocalization.getLocaledMessage(
                                        "Sender.Commands.Edit.Children.File.Children.List.Result.Replacer-Info",
                                replacerConfig.getRelativePath(),
                                replacerConfig.isEnable() ?
                                        PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Children.List.Result.Enabled") :
                                        PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Children.List.Result.Not-Enabled"),
                                String.valueOf(replacerConfig.getPriority()),
                                replacerConfig.getVersion() == null ?
                                        PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Children.List.Result.Not-Configured") :
                                        replacerConfig.getVersion(),
                                replacerConfig.getAuthor() == null ?
                                        PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Children.List.Result.Not-Configured") :
                                        replacerConfig.getAuthor(),
                                PSRLocalization.getLocaledMessage(replacerConfig.getMatchMode().getLocaleKey()),
                                listens.length() == 0 ?
                                        PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Children.List.Result.Not-Configured") :
                                        listens.toString()
                        )))).create());
            }

            MessageUtils.sendPageButtons(user, "/psr edit file list ", page, totalPage);

            user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Children.List.Result.Footer"));
        } else {
            user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Children.List.Detailed-Help"));
        }

    }

    private void selectCommand(@Nonnull User user, @NotNull String[] args) {
        if (args.length == 4) {
            ReplacerConfig replacerConfig = getSpecifiedReplacerConfig(args[3]);
            if (replacerConfig != null) {
                user.setEditorReplacerConfig(replacerConfig);
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Edit.Children.File.Children.Select.Replacer-Config-Selected", replacerConfig.getRelativePath()));
            } else {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Edit.Children.File.Children.Cannot-Find-Replacer-Config", args[3]));
            }
        } else {
            user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Children.Select.Detailed-Help"));
        }
    }

    private void createCommand(@Nonnull User user, @NotNull String[] args) {
        if (args.length == 4) {
            if (args[3].startsWith("Replacers/")) {
                if (FileUtils.checkFileSuffix(args[3], ".yml")) {
                    java.io.File file = new java.io.File(ProtocolStringReplacer.getInstance().getDataFolder() + "/" + args[3]);
                    if (FileUtils.createFile(file)) {
                        DotYamlConfiguration configuration = DotYamlConfiguration.loadConfiguration(file);
                        ReplacerConfig replacerConfig = new ReplacerConfig(file, configuration);
                        replacerConfig.saveConfig();
                        ProtocolStringReplacer.getInstance().getReplacerManager().addReplacerConfig(replacerConfig);
                        user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                                "Sender.Commands.Edit.Children.File.Children.Create.File-Successfully-Created", replacerConfig.getRelativePath()));
                    } else {
                        user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                                "Sender.Commands.Edit.Children.File.Children.Create.File-Create-Failed", args[3]));
                    }
                } else {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage("Sender.Commands.Edit.Children.File.Children.Create.Not-Yml-File"));
                }
            } else {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage("Sender.Commands.Edit.Children.File.Children.Create.Not-In-Replacers-Folder"));
            }
        } else {
            user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Children.Create.Detailed-Help"));
        }
    }

    private void deleteCommand(@Nonnull User user, @NotNull String[] args) {
        if (args.length == 4) {
            ReplacerConfig replacerConfig = getSpecifiedReplacerConfig(args[3]);
            if (replacerConfig != null) {
                if (user.isConfirmed(args)) {
                    //noinspection ResultOfMethodCallIgnored
                    replacerConfig.getFile().delete();
                    ProtocolStringReplacer.getInstance().getReplacerManager().getReplacerConfigList().remove(replacerConfig);
                    user.clearCommandToConfirm();
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Edit.Children.File.Children.Delete.File-Successfully-Deleted", replacerConfig.getRelativePath()));
                } else {
                    user.setCommandToConfirm(args);
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Edit.Children.File.Children.Delete.Delete-Need-To-Confirm", replacerConfig.getRelativePath()));
                }
            } else {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Edit.Children.File.Children.Cannot-Find-Replacer-Config", args[3]));
            }
        } else {
            user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Children.Delete.Detailed-Help"));
        }
    }

    @Override
    public List<String> onTab(@NotNull User user, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 3) {
            list = Arrays.asList("help", "list", "select", "create", "delete");
        } else if (args.length == 4) {
            if (args[2].equalsIgnoreCase("list")) {
                list.add("[" + PSRLocalization.getLocaledMessage("Variables.Page.Name") + "]");
            } else if (args[2].equalsIgnoreCase("delete") || args[2].equalsIgnoreCase("select")) {
                list.add("<" + PSRLocalization.getLocaledMessage("Variables.Replacer-Config.Name") + "|"
                        + PSRLocalization.getLocaledMessage("Variables.Index.Name") + ">");
                for (ReplacerConfig replacerConfig : ProtocolStringReplacer.getInstance().getReplacerManager().getReplacerConfigList()) {
                    list.add(ArgUtils.formatWithQuotes(replacerConfig.getRelativePath()));
                }
            } else if (args[2].equalsIgnoreCase("create")) {
                String arg = args[3];
                Matcher matcher = fileNamePrefix.matcher(arg);
                if (matcher.find()) {
                    int length = arg.length();
                    String subfix = ".yml\"";
                    int i = 5;
                    while (i > 0) {
                        if (arg.substring(length - i).equalsIgnoreCase(subfix.substring(0, i))) {
                            break;
                        }
                        i--;
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(arg, 0, arg.length() - i).append(subfix);
                    if (matcher.group(1) == null) {
                        stringBuilder.insert(0, '\"');
                    }
                    list.add(stringBuilder.toString());
                } else {
                    list.add("\"Replacers/");
                }
            }
        }
        return list;
    }

    @Override
    public void sendHelp(@Nonnull User user) {
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Help.Header"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Children.List.Simple-Help"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Children.Select.Simple-Help"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Children.Create.Simple-Help"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Children.Delete.Simple-Help"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Children.File.Help.Footer"));
    }

    @Nullable
    private ReplacerConfig getSpecifiedReplacerConfig(@NotNull String string) {
        LinkedList<ReplacerConfig> replacerConfigList = ProtocolStringReplacer.getInstance().getReplacerManager().getReplacerConfigList();
        for (ReplacerConfig replacerConfig : replacerConfigList) {
            if (replacerConfig.getRelativePath().equals(string)) {
                return replacerConfig;
            }
        }
        if (StringUtils.isNumeric(string)) {
            int index = Integer.parseInt(string);
            if (replacerConfigList.size() > index) {
                return replacerConfigList.get(index);
            }
        }
        return null;
    }
}
