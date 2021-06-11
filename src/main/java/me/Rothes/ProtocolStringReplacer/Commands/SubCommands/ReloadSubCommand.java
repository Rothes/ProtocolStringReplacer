package me.Rothes.ProtocolStringReplacer.Commands.SubCommands;

import me.Rothes.ProtocolStringReplacer.Commands.SubCommand;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

public class ReloadSubCommand extends SubCommand {

    public ReloadSubCommand() {
        super("reload", "protocolstringreplacer.command.reload");
    }

    @Override
    public void onExecute(@Nonnull CommandSender sender, @Nonnull String[] args) {

    }

}
