package me.rothes.protocolstringreplacer.commands;

import me.rothes.protocolstringreplacer.api.user.PsrUser;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class SubCommand {

    private final String name;
    private final String permission;
    private final String description;

    public SubCommand(@Nonnull String name, @Nonnull String permission, @Nonnull String description) {
        this.name = name;
        this.permission = permission;
        this.description = description;
    }

    public abstract void onExecute(@Nonnull PsrUser user, @Nonnull String[] args);

    public abstract List<String> onTab(@Nonnull PsrUser user, @Nonnull String[] args);

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

    public abstract void sendHelp(@Nonnull PsrUser user);
}
