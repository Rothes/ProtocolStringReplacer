package me.Rothes.ProtocolStringReplacer.Commands;

import me.Rothes.ProtocolStringReplacer.API.ArgumentsUtils;
import me.Rothes.ProtocolStringReplacer.Commands.SubCommands.Edit;
import me.Rothes.ProtocolStringReplacer.Commands.SubCommands.Reload;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.User.User;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CommandHandler implements TabCompleter, CommandExecutor {

    private LinkedList<SubCommand> subCommands = new LinkedList<>();

    public void initialize() {
        ProtocolStringReplacer.getInstance().getCommand("ProtocolStringReplacer").setExecutor(this);
        Bukkit.getServer().getPluginCommand("ProtocolStringReplacer").setTabCompleter(this);

        subCommands.add(new Edit());
        subCommands.add(new Reload());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof CommandBlock) {
            sender.sendMessage("§7[§cProtocol§6String§3Replacer§7] §c仅控制台和玩家可使用PSR的指令.");
        } else {
            User user = ProtocolStringReplacer.getInstance().getUserManager().getUser(sender);
            if (args.length > 0) {
                args = ArgumentsUtils.mergeQuotes(args);

                if (args[0].equalsIgnoreCase("confirm")) {
                    if (user.hasCommandToConfirm()) {
                        if (user.isConfirmExpired()) {
                            user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c确认操作已超时. 请重新执行.");
                            user.clearCommandToConfirm();
                            return true;
                        } else {
                            args = user.getCommandToConfirm();
                        }
                    } else {
                        user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c您没有待确认的操作.");
                        return true;
                    }
                }

                for (var subCommand : subCommands) {
                    if (args[0].equalsIgnoreCase(subCommand.getName())) {
                        if (user.hasPermission(subCommand.getPermission())) {
                            subCommand.onExecute(user, args);
                        } else {
                            user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c您没有权限这么做.");
                        }
                        return true;
                    }
                }
            }
            sendHelp(user);

        }
        return true;
    }

    public void sendHelp(@Nonnull User user) {
        user.sendFilteredText("§7§m------§7§l §7[ §c§lProtocol§6§lString§3§lReplacer§7 ]§l §7§m------");
        user.sendFilteredText("§7 * §e/psr help §7- §b插件指令列表");
        for (var subCommand : subCommands) {
            user.sendFilteredText("§7 * §e/psr " + subCommand.getName() + " §7- §b" + subCommand.getDescription());
        }
        user.sendFilteredText("§7§m-----------------------------------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 80.0F, 1.0F);
        }

        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("help");
            User user = ProtocolStringReplacer.getInstance().getUserManager().getUser(sender);
            if (user.hasCommandToConfirm() && !user.isConfirmExpired()) {
                list.add("confirm");
            }
            for (var subCommand : subCommands) {
                if (sender.hasPermission(subCommand.getPermission())) {
                    list.add(subCommand.getName());
                }
            }
        } else {
            for (var subCommand : subCommands) {
                if (subCommand.getName().equalsIgnoreCase(args[0])) {
                    list = subCommand.onTab(ProtocolStringReplacer.getInstance().getUserManager().getUser(sender), args);
                }
            }
        }
        return list;
    }

}
