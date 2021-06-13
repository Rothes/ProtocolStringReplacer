package me.Rothes.ProtocolStringReplacer.Commands;

import me.Rothes.ProtocolStringReplacer.API.ArrayUtils;
import me.Rothes.ProtocolStringReplacer.Commands.SubCommands.ReloadSubCommand;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CommandHandler implements TabCompleter, CommandExecutor {

    private List<SubCommand> subCommands = new LinkedList<>();

    public void initialize() {
        ProtocolStringReplacer.getInstance().getCommand("ProtocolStringReplacer").setExecutor(this);
        Bukkit.getServer().getPluginCommand("ProtocolStringReplacer").setTabCompleter(this);

        registerSubCommand(new ReloadSubCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            args = ArrayUtils.mergeQuotes(args);
            for (SubCommand subCommand : subCommands) {
                if (args[0].equalsIgnoreCase(subCommand.getName())) {
                    if (sender.isOp() || sender.hasPermission(subCommand.getPermission())) {
                        subCommand.onExecute(sender, args);
                        return true;
                    } else {
                        sender.sendMessage("§c§lP§6§lS§3§lR §e> §c您没有权限这么做.");
                        return true;
                    }
                }
            }
        }

        sendHelp(sender);
        return true;
    }

    public void sendHelp(@Nonnull CommandSender sender) {
        sender.sendMessage("§7§m------§b§l §b[ §c§lProtocol§6§lString§3§lReplacer§b ]§l §7§m------");
        sender.sendMessage("§7 * §e/psr help §7- §b插件指令列表");
        for (SubCommand subCommand : subCommands) {
            sender.sendMessage("§7 * §e/psr " + subCommand.getName() + " §7- §b" + subCommand.getDescription());
        }
        sender.sendMessage("§7§m-----------------------------------------");
    }

    public void registerSubCommand(@Nonnull SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    public void unregisterSubCommand(@Nonnull SubCommand subCommand) {
        subCommands.remove(subCommand);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("help");
            for (SubCommand subCommand : subCommands) {
                list.add(subCommand.getName());
            }
        }
        return list;
    }

}
