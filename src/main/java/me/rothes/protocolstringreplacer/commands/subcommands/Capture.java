package me.rothes.protocolstringreplacer.commands.subcommands;

import me.rothes.protocolstringreplacer.PSRLocalization;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.capture.CaptureInfo;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.api.user.User;
import me.rothes.protocolstringreplacer.commands.SubCommand;
import me.rothes.protocolstringreplacer.utils.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Capture extends SubCommand {

    public Capture() {
        super("capture", "protocolstringreplacer.command.capture",
                PSRLocalization.getLocaledMessage("Sender.Commands.Capture.Description"));
    }

    @Override
    public void onExecute(@Nonnull User user, @Nonnull String[] args) {
        if (!user.isOnline()) {
            user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage("Console-Sender.Messages.Command-Not-Available"));
            return;
        }
        if (args.length > 1) {
            if ("add".equalsIgnoreCase(args[1])) {
                addCommand(user, args);
                return;
            } else if ("remove".equalsIgnoreCase(args[1])) {
                removeCommand(user, args);
                return;
            } else if ("list".equalsIgnoreCase(args[1])) {
                listCommand(user, args);
                return;
            }
        }
        sendHelp(user);
    }

    private void addCommand(@Nonnull User user, @Nonnull String[] args) {
        if (args.length == 3) {
            ListenType listenType = ListenType.getType(args[2]);
            if (listenType == null) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Variables.Listen-Type.Messages.Invalid-Mode", args[2]));
                return;
            }
            if (!listenType.isCapturable()) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Capture.Children.Add.Listen-Type-Cannot-Be-Captured"));
                return;
            }
            if (user.isCapturing(listenType)) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Capture.Children.Add.Already-Capturing-Listen-Type", listenType.getName()));
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Capture.Remove-Capture-Tip", listenType.getName()));
                return;
            }
            user.addCaptureType(listenType);
            user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                    "Sender.Commands.Capture.Children.Add.Capture-Added", listenType.getName()));
        } else {
            user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Capture.Children.Add.Detailed-Help"));
        }
    }

    private void removeCommand(@Nonnull User user, @Nonnull String[] args) {
        if (args.length == 3) {
            ListenType listenType = ListenType.getType(args[2]);
            if (listenType == null) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Variables.Listen-Type.Messages.Invalid-Type", args[2]));
                return;
            }
            if (!user.isCapturing(listenType)) {
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Capture.Children.Remove.Already-Not-Capturing-Listen-Type", listenType.getName()));
                user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Capture.Add-Capture-Tip", listenType.getName()));
                return;
            }
            user.removeCaptureType(listenType);
            user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                    "Sender.Commands.Capture.Children.Remove.Capture-Removed", listenType.getName()));
        } else {
            user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Capture.Children.Remove.Detailed-Help"));
        }
    }

    private void listCommand(@Nonnull User user, @Nonnull String[] args) {
        if (args.length == 3 || args.length == 4) {
            Bukkit.getScheduler().runTaskAsynchronously(ProtocolStringReplacer.getInstance(), () -> {
                ListenType listenType = ListenType.getType(args[2]);
                if (listenType == null) {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Variables.Listen-Type.Messages.Invalid-Type", args[2]));
                    return;
                }
                if (!user.isCapturing(listenType)) {
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Capture.Children.List.Not-Capturing-Listen-Type", listenType.getName()));
                    user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Capture.Add-Capture-Tip", listenType.getName()));
                    return;
                }
                int page = 1;
                if (args.length == 4) {
                    if (StringUtils.isNumeric(args[3])) {
                        page = Integer.parseInt(args[3]);
                    } else {
                        user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage(
                                "Sender.Error.Not-A-Positive-Integer", args[3]));
                        return;
                    }
                }
                user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Capture.Children.List.Results-Header"));

                List<CaptureInfo> captureMessages = user.getCaptureInfos(listenType);
                int totalPage = (int) Math.ceil((float) captureMessages.size() / 10);
                for (int i = (page - 1) * 10; i < captureMessages.size() && i < page * 10; i++) {
                    MessageUtils.sendCaptureInfo(user, captureMessages.get(i));
                }

                MessageUtils.sendPageButtons(user, "/psr capture list " + args[2] + " ", page, totalPage);
                user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Capture.Children.List.Results-Footer"));
            });
        } else {
            user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Capture.Children.List.Detailed-Help"));
        }
    }



    @Override
    public List<String> onTab(@NotNull User user, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 2) {
            list = Arrays.asList("add", "remove", "list");
        } else if (args.length == 3
                && args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("list")) {
            list.add("<" + PSRLocalization.getLocaledMessage("Variables.Listen-Type.Name") + ">");
            for (ListenType listenType : ListenType.values()) {
                if (listenType.isCapturable()) {
                    list.add(listenType.getName());
                }
            }
        } else if (args.length == 4
                && args[1].equalsIgnoreCase("list")) {
            list.add("[" + PSRLocalization.getLocaledMessage("Variables.Page.Name") + "]");
        }
        return list;
    }

    @Override
    public void sendHelp(@Nonnull User user) {
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Capture.Help.Header"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Capture.Children.Add.Simple-Help"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Capture.Children.Remove.Simple-Help"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Capture.Children.List.Simple-Help"));
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Capture.Help.Footer"));
    }

}
