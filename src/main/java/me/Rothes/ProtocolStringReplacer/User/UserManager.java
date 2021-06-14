package me.Rothes.ProtocolStringReplacer.User;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.UUID;

public class UserManager {

    private HashMap<UUID, User> users = new HashMap<>();

    public User getUser(@NotNull UUID uuid) {
        return users.get(uuid);
    }

    @Nonnull
    public User getUser(@NotNull Player player) {
        return getUser(player.getUniqueId());
    }

    public void loadUser(@NotNull UUID uuid) {
        users.put(uuid, new User(uuid));
    }

    public void loadUser(@NotNull Player player) {
        users.put(player.getUniqueId(), new User(player));
    }

    public void unloadUser(@NotNull UUID uuid) {
        users.remove(uuid);
    }

    public void unloadUser(@NotNull Player player) {
        unloadUser(player.getUniqueId());
    }

}
