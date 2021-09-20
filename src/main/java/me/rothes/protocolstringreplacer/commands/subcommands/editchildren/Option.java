package me.rothes.protocolstringreplacer.commands.subcommands.editchildren;

import me.rothes.protocolstringreplacer.user.User;
import me.rothes.protocolstringreplacer.commands.SubCommand;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class Option extends SubCommand {

    public Option() {
        super("option", "protocolstringreplacer.command.edit", "替换配置设定相关指令");
    }

    @Override
    public void onExecute(@Nonnull User user, @NotNull String[] args) {
        if ("add".equalsIgnoreCase(args[2])) {

        } else if ("set".equalsIgnoreCase(args[2])) {

        } else if ("remove".equalsIgnoreCase(args[2])) {

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

    }

}
