package me.rothes.protocolstringreplacer.commands;

import me.rothes.protocolstringreplacer.PsrLocalization;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.utils.ArgUtils;
import me.rothes.protocolstringreplacer.commands.subcommands.Capture;
import me.rothes.protocolstringreplacer.commands.subcommands.Edit;
import me.rothes.protocolstringreplacer.commands.subcommands.Parse;
import me.rothes.protocolstringreplacer.commands.subcommands.Reload;
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

    private final LinkedList<SubCommand> subCommands = new LinkedList<>();
    private final ProtocolStringReplacer plugin = ProtocolStringReplacer.getInstance();

    public void initialize() {
        plugin.getCommand("ProtocolStringReplacer").setExecutor(this);
        plugin.getCommand("ProtocolStringReplacer").setTabCompleter(this);

        subCommands.add(new Edit());
        subCommands.add(new Parse());
        subCommands.add(new Capture());
        subCommands.add(new Reload());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof CommandBlock) {
            sender.sendMessage(PsrLocalization.getLocaledMessage("Command-Block-Sender.Messages.Command-Not-Available"));
        } else {
            PsrUser user = plugin.getUserManager().getUser(sender);
            if (args.length > 0) {
                String[] mergedArgs = ArgUtils.mergeQuotes(args);

                if (mergedArgs[0].equalsIgnoreCase("confirm")) {
                    if (user.hasCommandToConfirm()) {
                        if (user.isConfirmExpired()) {
                            user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Commands.Confirm.Expired"));
                            user.clearCommandToConfirm();
                            return true;
                        } else {
                            mergedArgs = user.getCommandToConfirm();
                        }
                    } else {
                        user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Commands.Confirm.Nothing-To-Confirm"));
                        return true;
                    }
                }

                for (SubCommand subCommand : subCommands) {
                    if (mergedArgs[0].equalsIgnoreCase(subCommand.getName())) {
                        if (user.hasPermissionOrOp(subCommand.getPermission())) {
                            subCommand.onExecute(user, mergedArgs);
                        } else {
                            user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Commands.No-Permission"));
                        }
                        return true;
                    }
                }
            }
            sendHelp(user);

        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (plugin.getConfigManager().cmdTypingSound
                && sender instanceof Player && plugin.getServerMajorVersion() >= 9) {
            Player player = (Player) sender;
            player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 80.0F, 1.0F);
        }

        String[] mergedArgs = ArgUtils.mergeQuotes(args);

        List<String> list = new ArrayList<>();
        PsrUser user = plugin.getUserManager().getUser(sender);
        if (mergedArgs.length == 1) {
            list.add("help");
            if (user.hasCommandToConfirm() && !user.isConfirmExpired()) {
                list.add("confirm");
            }
            for (SubCommand subCommand : subCommands) {
                if (user.hasPermissionOrOp(subCommand.getPermission())) {
                    list.add(subCommand.getName());
                }
            }
        } else {
            for (SubCommand subCommand : subCommands) {
                if (subCommand.getName().equalsIgnoreCase(mergedArgs[0])) {
                    list = subCommand.onTab(user, mergedArgs);
                }
            }
        }
        return list;
    }

    public void sendHelp(@Nonnull PsrUser user) {
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Help.Header"));
        for (SubCommand subCommand : subCommands) {
            user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Subcommand-Help-Format",
                    "/psr " + subCommand.getName(), subCommand.getDescription()));
        }
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Help.Footer"));
    }

}
