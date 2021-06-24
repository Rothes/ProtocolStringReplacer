package me.Rothes.ProtocolStringReplacer.Commands;

import me.Rothes.ProtocolStringReplacer.User.User;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedList;
import java.util.List;

public abstract class SubCommand {

    private String name;
    private String permission;
    private String description;

    public SubCommand(@Nonnull String name, @Nonnull String permission, @Nonnull String description) {
        this.name = name;
        this.permission = permission;
        this.description = description;
    }

    public abstract void onExecute(@Nonnull User user, @Nonnull String[] args);

    public abstract List<String> onTab(@Nonnull User user, @Nonnull String[] args);

    @Nonnull
    public final String getName() {
        return name;
    }

    @Nonnull
    public String getPermission() {
        return permission;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    public abstract void sendHelp(@Nonnull User user);
}
