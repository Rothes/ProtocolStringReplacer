package me.Rothes.ProtocolStringReplacer.Commands.SubCommands;

import me.Rothes.ProtocolStringReplacer.Commands.SubCommand;
import me.Rothes.ProtocolStringReplacer.User.User;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class Capture extends SubCommand {

    public Capture() {
        super("capture", "protocolstringreplacer.command.capture", "todo");
    }

    @Override
    public void onExecute(@Nonnull User user, @Nonnull String[] args) {

    }

    @Override
    public List<String> onTab(@NotNull User user, @NotNull String[] args) {
        return null;
    }

    @Override
    public void sendHelp(@Nonnull User user) {

    }

}
