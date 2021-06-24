package me.Rothes.ProtocolStringReplacer.Commands.SubCommands;

import me.Rothes.ProtocolStringReplacer.Commands.SubCommand;
import me.Rothes.ProtocolStringReplacer.Commands.SubCommands.EditChildren.File;
import me.Rothes.ProtocolStringReplacer.Commands.SubCommands.EditChildren.Replace;
import me.Rothes.ProtocolStringReplacer.User.User;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Edit extends SubCommand {

    private LinkedList<SubCommand> childCommands = new LinkedList<>();

    public Edit() {
        super("edit", "protocolstringreplacer.command.edit", "替换配置编辑器");

        childCommands.add(new File());
        childCommands.add(new Replace());
// TODO        childCommands.add(new Option());
    }

    @Override
    public void onExecute(@Nonnull User user, @Nonnull String[] args) {
        if (args.length > 1) {
            for (var childCommand : childCommands) {
                if (childCommand.getName().equalsIgnoreCase(args[1])) {
                    if (user.hasPermission(childCommand.getPermission())) {
                        childCommand.onExecute(user, args);
                    } else {
                        user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c您没有权限这么做.");
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
            for (var childCommand : childCommands) {
                if (user.hasPermission(childCommand.getPermission())) {
                    list.add(childCommand.getName());
                }
            }
        } else {
            for (var childCommand : childCommands) {
                if (childCommand.getName().equalsIgnoreCase(args[1])) {
                    list = childCommand.onTab(user, args);
                }
            }
        }
        return list;
    }

    @Override
    public void sendHelp(@Nonnull User user) {
        user.sendFilteredText("§7§m-----------§7§l §7[ §c§lP§6§lS§3§lR §7- §e编辑器§7 ]§l §7§m-----------");
        user.sendFilteredText("§7 * §e/psr edit help §7- §b编辑器指令列表");
        for (var childCommand : childCommands) {
            user.sendFilteredText("§7 * §e/psr edit " + childCommand.getName() + " §7- §b" + childCommand.getDescription());
        }
        user.sendFilteredText("§7§m-----------------------------------------");
    }

}
