package me.rothes.protocolstringreplacer.commands.subcommands;

import me.rothes.protocolstringreplacer.PSRLocalization;
import me.rothes.protocolstringreplacer.commands.subcommands.editchildren.Block;
import me.rothes.protocolstringreplacer.commands.subcommands.editchildren.File;
import me.rothes.protocolstringreplacer.commands.subcommands.editchildren.Replace;
import me.rothes.protocolstringreplacer.user.User;
import me.rothes.protocolstringreplacer.commands.SubCommand;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Edit extends SubCommand {

    private LinkedList<SubCommand> childCommands = new LinkedList<>();

    public Edit() {
        super("edit", "protocolstringreplacer.command.edit",
                PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Description"));

        childCommands.add(new File());
        childCommands.add(new Replace());
        childCommands.add(new Block());
// TODO:        childCommands.add(new Option());
    }

    @Override
    public void onExecute(@Nonnull User user, @Nonnull String[] args) {
        if (args.length > 1) {
            for (SubCommand childCommand : childCommands) {
                if (childCommand.getName().equalsIgnoreCase(args[1])) {
                    if (user.hasPermission(childCommand.getPermission())) {
                        childCommand.onExecute(user, args);
                    } else {
                        user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage("Sender.Commands.No-Permission"));
                    }
                    return;
                }
            }
        }
        sendHelp(user);
    }

    @Override
    public List<String> onTab(@NotNull User user, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 2) {
            list.add("help");
            for (SubCommand childCommand : childCommands) {
                if (user.hasPermission(childCommand.getPermission())) {
                    list.add(childCommand.getName());
                }
            }
        } else {
            for (SubCommand childCommand : childCommands) {
                if (childCommand.getName().equalsIgnoreCase(args[1])) {
                    list = childCommand.onTab(user, args);
                }
            }
        }
        return list;
    }

    @Override
    public void sendHelp(@Nonnull User user) {
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Help.Header"));
        user.sendFilteredText("§7 * §e/psr edit help §7- §b" + PSRLocalization.getLocaledMessage(
                "Sender.Commands.Edit.Help.Help-Description"));
        for (SubCommand childCommand : childCommands) {
            user.sendFilteredText("§7 * §e/psr edit " + childCommand.getName() + " §7- §b" + childCommand.getDescription());
        }
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Edit.Help.Footer"));
    }

}
