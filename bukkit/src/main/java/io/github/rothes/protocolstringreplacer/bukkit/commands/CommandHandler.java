package io.github.rothes.protocolstringreplacer.bukkit.commands;

import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.bukkit.commands.subcommands.Capture;
import io.github.rothes.protocolstringreplacer.bukkit.commands.subcommands.Edit;
import io.github.rothes.protocolstringreplacer.bukkit.commands.subcommands.Parse;
import io.github.rothes.protocolstringreplacer.bukkit.commands.subcommands.Reload;
import io.github.rothes.protocolstringreplacer.bukkit.PsrLocalization;
import io.github.rothes.protocolstringreplacer.bukkit.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.bukkit.utils.ArgUtils;
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
        subCommands.add(new Parse());
        subCommands.add(new Capture());
        subCommands.add(new Reload());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof CommandBlock) {
            sender.sendMessage(PsrLocalization.getLocaledMessage("Command-Block-Sender.Messages.Command-Not-Available"));
        } else {
            PsrUser user = ProtocolStringReplacer.getInstance().getUserManager().getUser(sender);
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
                        if (user.hasPermission(subCommand.getPermission())) {
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
        if (sender instanceof Player && ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 9) {
            Player player = (Player) sender;
            player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 80.0F, 1.0F);
        }

        String[] mergedArgs = ArgUtils.mergeQuotes(args);

        List<String> list = new ArrayList<>();
        PsrUser user = ProtocolStringReplacer.getInstance().getUserManager().getUser(sender);
        if (mergedArgs.length == 1) {
            list.add("help");
            if (user.hasCommandToConfirm() && !user.isConfirmExpired()) {
                list.add("confirm");
            }
            for (SubCommand subCommand : subCommands) {
                if (user.hasPermission(subCommand.getPermission())) {
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
