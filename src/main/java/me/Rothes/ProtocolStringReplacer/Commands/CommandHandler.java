package me.Rothes.ProtocolStringReplacer.Commands;

import me.Rothes.ProtocolStringReplacer.API.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public class CommandHandler implements TabCompleter, CommandExecutor {

    private static List<SubCommand> subCommands = new LinkedList<>();

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            args = ArrayUtils.mergeQuotes(args);
            for (SubCommand command : subCommands) {
                if (args[0].equalsIgnoreCase(command.getName())) {
                    if (sender.isOp() || sender.hasPermission(command.getPermission())) {
                        command.onExecute(sender, args);
                        return true;
                    } else {

                    }
                }
            }
        }

        sendHelp(sender);
        return true;
    }

    public void sendHelp(@Nonnull CommandSender sender) {

    }

    public static void registerSubCommand(@Nonnull SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    public static void unregisterSubCommand(@Nonnull SubCommand subCommand) {
        subCommands.remove(subCommand);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }

}
