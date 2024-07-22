package io.github.rothes.protocolstringreplacer.api.user;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.UUID;

public class PsrUserManager {

    private final HashMap<UUID, PsrUser> users = new HashMap<>();
    private final PsrUser console = new PsrUser(Bukkit.getConsoleSender());

    public PsrUser getUser(@NotNull UUID uuid) {
        return users.getOrDefault(uuid, loadUser(uuid));
    }

    @Nonnull
    public PsrUser getUser(@NotNull Player player) {
        return users.getOrDefault(player.getUniqueId(), loadUser(player));
    }

    @Nonnull
    public PsrUser getUser(@NotNull CommandSender sender) {
        return sender instanceof Player? getUser((Player) sender) : console;
    }

    public PsrUser getConsoleUser() {
        return console;
    }

    public PsrUser loadUser(@NotNull UUID uuid) {
        return users.putIfAbsent(uuid, new PsrUser(uuid));
    }

    public PsrUser loadUser(@NotNull Player player) {
        return users.putIfAbsent(player.getUniqueId(), new PsrUser(player));
    }

    public void unloadUser(@NotNull UUID uuid) {
        users.remove(uuid);
    }

    public void unloadUser(@NotNull Player player) {
        unloadUser(player.getUniqueId());
    }

}
