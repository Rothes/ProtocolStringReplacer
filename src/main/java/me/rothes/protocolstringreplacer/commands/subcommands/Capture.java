package me.rothes.protocolstringreplacer.commands.subcommands;

import me.rothes.protocolstringreplacer.user.User;
import me.rothes.protocolstringreplacer.commands.SubCommand;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class Capture extends SubCommand {

    public Capture() {
        super("capture", "protocolstringreplacer.command.capture", "todo");
    }

    @Override
    public void onExecute(@Nonnull User user, @Nonnull String[] args) {
        //TODO
    }

    @Override
    public List<String> onTab(@NotNull User user, @NotNull String[] args) {
        return null;
    }

    @Override
    public void sendHelp(@Nonnull User user) {
        //TODO
    }

}
