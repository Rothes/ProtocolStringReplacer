package me.rothes.protocolstringreplacer.commands.subcommands;

import me.rothes.protocolstringreplacer.PsrLocalization;
import me.rothes.protocolstringreplacer.commands.subcommands.editchildren.Block;
import me.rothes.protocolstringreplacer.commands.subcommands.editchildren.File;
import me.rothes.protocolstringreplacer.commands.subcommands.editchildren.Replace;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
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
                PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Description"));

        childCommands.add(new File());
        childCommands.add(new Replace());
        childCommands.add(new Block());
// TODO:        childCommands.add(new Option());
    }

    @Override
    public void onExecute(@Nonnull PsrUser user, @Nonnull String[] args) {
        if (args.length > 1) {
            for (SubCommand childCommand : childCommands) {
                if (childCommand.getName().equalsIgnoreCase(args[1])) {
                    if (user.hasPermission(childCommand.getPermission())) {
                        childCommand.onExecute(user, args);
                    } else {
                        user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Commands.No-Permission"));
                    }
                    return;
                }
            }
        }
        sendHelp(user);
    }

    @Override
    public List<String> onTab(@NotNull PsrUser user, @NotNull String[] args) {
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
    public void sendHelp(@Nonnull PsrUser user) {
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Help.Header"));
        for (SubCommand childCommand : childCommands) {
            user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Subcommand-Help-Format",
                    "/psr edit " + childCommand.getName(), childCommand.getDescription()));
        }
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Edit.Help.Footer"));
    }

}
