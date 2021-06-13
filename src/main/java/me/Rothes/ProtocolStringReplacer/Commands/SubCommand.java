package me.Rothes.ProtocolStringReplacer.Commands;

import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public abstract class SubCommand {

    private String name;
    private String permission;
    private String description;

    @ParametersAreNonnullByDefault
    public SubCommand(String name, String permission, String description) {
        this.name = name;
        this.permission = permission;
        this.description = description;
    }

    public abstract void onExecute(@Nonnull CommandSender sender, @Nonnull String[] args);

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

}
