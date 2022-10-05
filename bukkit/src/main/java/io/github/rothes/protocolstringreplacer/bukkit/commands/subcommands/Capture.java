package io.github.rothes.protocolstringreplacer.bukkit.commands.subcommands;

import io.github.rothes.protocolstringreplacer.bukkit.PsrLocalization;
import io.github.rothes.protocolstringreplacer.bukkit.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.bukkit.api.capture.CaptureInfo;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.ListenType;
import io.github.rothes.protocolstringreplacer.bukkit.commands.SubCommand;
import io.github.rothes.protocolstringreplacer.bukkit.utils.MessageUtils;
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
                PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Description"));
    }

    @Override
    public void onExecute(@Nonnull PsrUser user, @Nonnull String[] args) {
        if (!user.isOnline()) {
            user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Console-Sender.Messages.Command-Not-Available"));
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
            } else if ("clipboard".equalsIgnoreCase(args[1])) {
                clipboardCommand(user, args);
                return;
            }
        }
        sendHelp(user);
    }

    private void addCommand(@Nonnull PsrUser user, @Nonnull String[] args) {
        if (args.length == 3) {
            ListenType listenType = ListenType.getType(args[2]);
            if (listenType == null) {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Variables.Listen-Type.Messages.Invalid-Type", args[2]));
                return;
            }
            if (!listenType.isCapturable()) {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Capture.Children.Add.Listen-Type-Cannot-Be-Captured"));
                return;
            }
            if (user.isCapturing(listenType)) {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Capture.Children.Add.Already-Capturing-Listen-Type", listenType.getName()));
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Capture.Remove-Capture-Tip", listenType.getName()));
                return;
            }
            user.addCaptureType(listenType);
            user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                    "Sender.Commands.Capture.Children.Add.Capture-Added", listenType.getName()));
        } else {
            user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Children.Add.Detailed-Help"));
        }
    }

    private void removeCommand(@Nonnull PsrUser user, @Nonnull String[] args) {
        if (args.length == 3) {
            ListenType listenType = ListenType.getType(args[2]);
            if (listenType == null) {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Variables.Listen-Type.Messages.Invalid-Type", args[2]));
                return;
            }
            if (!user.isCapturing(listenType)) {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Capture.Children.Remove.Already-Not-Capturing-Listen-Type", listenType.getName()));
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                        "Sender.Commands.Capture.Add-Capture-Tip", listenType.getName()));
                return;
            }
            user.removeCaptureType(listenType);
            user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                    "Sender.Commands.Capture.Children.Remove.Capture-Removed", listenType.getName()));
        } else {
            user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Children.Remove.Detailed-Help"));
        }
    }

    private void listCommand(@Nonnull PsrUser user, @Nonnull String[] args) {
        if (args.length == 3 || args.length == 4) {
            Bukkit.getScheduler().runTaskAsynchronously(ProtocolStringReplacer.getInstance(), () -> {
                ListenType listenType = ListenType.getType(args[2]);
                if (listenType == null) {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                            "Variables.Listen-Type.Messages.Invalid-Type", args[2]));
                    return;
                }
                if (!user.isCapturing(listenType)) {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Capture.Children.List.Not-Capturing-Listen-Type", listenType.getName()));
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Capture.Add-Capture-Tip", listenType.getName()));
                    return;
                }
                int page = 1;
                if (args.length == 4) {
                    if (StringUtils.isNumeric(args[3])) {
                        page = Integer.parseInt(args[3]);
                    } else {
                        user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                                "Sender.Error.Not-A-Positive-Integer", args[3]));
                        return;
                    }
                }
                user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Children.List.Results-Header"));

                List<CaptureInfo> captureMessages = user.getCaptureInfos(listenType);
                int size = captureMessages.size();
                int totalPage = (int) Math.ceil((float) size / 10);
                int sent = 0;
                for (int i = size - 1 - (page - 1) * 10; i >= 0; i--) {
                    sent++;
                    if (sent > 10) {
                        break;
                    }
                    MessageUtils.sendCaptureInfo(user, captureMessages.get(i), i);
                }

                MessageUtils.sendPageButtons(user, "/psr capture list " + args[2] + " ", page, totalPage);
                user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Children.List.Results-Footer"));
            });
        } else {
            user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Children.List.Detailed-Help"));
        }
    }

    private void clipboardCommand(@Nonnull PsrUser user, @Nonnull String[] args) {
        if (args.length == 4) {
            Bukkit.getScheduler().runTaskAsynchronously(ProtocolStringReplacer.getInstance(), () -> {
                ListenType listenType = ListenType.getType(args[2]);
                if (!user.isCapturing(listenType)) {
                    user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage(
                            "Sender.Commands.Capture.Children.List.Not-Capturing-Listen-Type", listenType.getName()));
                    return;
                }

                List<CaptureInfo> captureInfos = user.getCaptureInfos(listenType);
                int index = Integer.parseInt(args[3]);
                if (captureInfos.size() <= index) {
                    return;
                }
                MessageUtils.sendCaptureInfoClipboard(user, captureInfos.get(index));
                user.sendFilteredText("§aClick any entry to copy");
            });
        }
    }



    @Override
    public List<String> onTab(@NotNull PsrUser user, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 2) {
            list = Arrays.asList("add", "remove", "list");
        } else if (args.length == 3
                && args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("list")) {
            list.add("<" + PsrLocalization.getLocaledMessage("Variables.Listen-Type.Name") + ">");
            for (ListenType listenType : ListenType.values()) {
                if (listenType.isCapturable()) {
                    list.add(listenType.getName());
                }
            }
        } else if (args.length == 4
                && args[1].equalsIgnoreCase("list")) {
            list.add("[" + PsrLocalization.getLocaledMessage("Variables.Page.Name") + "]");
        }
        return list;
    }

    @Override
    public void sendHelp(@Nonnull PsrUser user) {
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Help.Header"));
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Children.Add.Simple-Help"));
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Children.Remove.Simple-Help"));
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Children.List.Simple-Help"));
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Help.Footer"));
    }

}
