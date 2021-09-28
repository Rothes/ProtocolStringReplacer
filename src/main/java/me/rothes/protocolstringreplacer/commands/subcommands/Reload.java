package me.rothes.protocolstringreplacer.commands.subcommands;

import me.rothes.protocolstringreplacer.PSRLocalization;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.user.User;
import me.rothes.protocolstringreplacer.commands.SubCommand;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class Reload extends SubCommand {

    public Reload() {
        super("reload", "protocolstringreplacer.command.reload", PSRLocalization
                .getLocaledMessage("Sender.Commands.Reload.Description"));
    }

    @Override
    public void onExecute(@Nonnull User user, @Nonnull String[] args) {
        if (args.length == 1) {
            user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage("Sender.Commands.Reload.Async-Reloading"));
            Bukkit.getScheduler().runTaskAsynchronously(ProtocolStringReplacer.getInstance(), () -> ProtocolStringReplacer.getInstance().reload(user));
        } else {
            sendHelp(user);
        }
    }

    @Override
    public List<String> onTab(@NotNull User user, @NotNull String[] args) {
        return null;
    }

    @Override
    public void sendHelp(@Nonnull User user) {
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Reload.Help.Header"));
        user.sendFilteredText("§7 * §e/psr reload§7- §b" + this.getDescription());
        user.sendFilteredText(PSRLocalization.getLocaledMessage("Sender.Commands.Reload.Help.Footer"));
    }

}
