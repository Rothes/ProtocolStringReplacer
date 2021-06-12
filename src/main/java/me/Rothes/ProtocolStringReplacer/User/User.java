package me.Rothes.ProtocolStringReplacer.User;

import me.Rothes.ProtocolStringReplacer.Replacer.ReplacerConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;

import java.util.HashMap;
import java.util.UUID;

public class User {

    private UUID uuid;
    private Player player;

    private String currentlyWindowTitle;
    private HashMap<Short, ItemMeta> metaCache = new HashMap<>();
    private Short uniqueCacheKey = 0;
    private ReplacerConfig editorFile;
    private int editorIndex;
    private String editorPattern;
    private String editorReplacement;

    public User(Player player) {
        this.player = player;
        uuid = player.getUniqueId();
    }

    public User(UUID uuid) {
        this.uuid = uuid;
        player = Bukkit.getPlayer(uuid);
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isOnline() {
        return player != null && player.isOnline();
    }

    public boolean hasPermission(String permission) {
        return (isOnline() && player.hasPermission(permission));
    }

    public boolean hasPermission(Permission permission) {
        return (isOnline() && player.hasPermission(permission));
    }

    public HashMap<Short, ItemMeta> getMetaCache() {
        return metaCache;
    }

    public Short nextUniqueCacheKey() {
        return ++uniqueCacheKey;
    }

    public String getCurrentlyWindowTitle() {
        return currentlyWindowTitle;
    }

    public void setCurrentlyWindowTitle(String title) {
        currentlyWindowTitle = title;
    }

    @Override
    public String toString() {
        return "User{" +
                "uuid=" + uuid +
                ", player=" + player +
                ", currentlyWindowTitle='" + currentlyWindowTitle + '\'' +
                ", metaCache=" + metaCache +
                ", uniqueCacheKey=" + uniqueCacheKey +
                ", editorFile=" + editorFile +
                ", editorIndex=" + editorIndex +
                ", editorPattern='" + editorPattern + '\'' +
                ", editorReplacement='" + editorReplacement + '\'' +
                '}';
    }

}
