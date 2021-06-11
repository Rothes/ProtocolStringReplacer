package me.Rothes.ProtocolStringReplacer.Commands;

import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public abstract class SubCommand {

    private final String name;
    private final String permission;

    @ParametersAreNonnullByDefault
    public SubCommand(String name, String permission) {
        this.name = name;
        this.permission = permission;
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

}
