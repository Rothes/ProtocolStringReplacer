package io.github.rothes.protocolstringreplacer.bukkit.commands.subcommands;

import io.github.rothes.protocolstringreplacer.bukkit.PsrLocalization;
import io.github.rothes.protocolstringreplacer.bukkit.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.bukkit.commands.SubCommand;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class Reload extends SubCommand {

    public Reload() {
        super("reload", "protocolstringreplacer.command.reload", PsrLocalization
                .getLocaledMessage("Sender.Commands.Reload.Description"));
    }

    @Override
    public void onExecute(@Nonnull PsrUser user, @Nonnull String[] args) {
        if (args.length == 1) {
            if (ProtocolStringReplacer.getInstance().isReloading()) {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Commands.Reload.Already-Reloading"));
                return;
            }
            ProtocolStringReplacer.getInstance().reload(user);
        } else {
            sendHelp(user);
        }
    }

    @Override
    public List<String> onTab(@NotNull PsrUser user, @NotNull String[] args) {
        return null;
    }

    @Override
    public void sendHelp(@Nonnull PsrUser user) {
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Reload.Help.Header"));
        user.sendFilteredText("§7 * §e/psr reload§7- §b " + this.getDescription());
        user.sendFilteredText(PsrLocalization.getLocaledMessage("Sender.Commands.Reload.Help.Footer"));
    }

}
