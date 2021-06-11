package me.Rothes.ProtocolStringReplacer;

import me.Rothes.ProtocolStringReplacer.Listeners.PlayerJoinListener;
import me.Rothes.ProtocolStringReplacer.Listeners.PlayerQuitListener;
import me.Rothes.ProtocolStringReplacer.PacketListeners.PacketListenerManager;
import me.Rothes.ProtocolStringReplacer.User.UserManager;
import me.Rothes.ProtocolStringReplacer.Replacer.ReplacerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;

public class ProtocolStringReplacer extends JavaPlugin {

    private static ProtocolStringReplacer instance;
    private static ReplacerManager replacerManager;
    private static PacketListenerManager packetListenerManager;
    private static UserManager userManager;

    public static ProtocolStringReplacer getInstance() {
        return instance;
    }

    public void onEnable() {
        instance = this;
        initialize();
        Bukkit.getConsoleSender().sendMessage("§7[§6ProtocolStringReplacer§7] §a插件已成功加载");
    }

    public void onDisable() {
        packetListenerManager.removeListeners();
    }

    @Nonnull
    public static ReplacerManager getReplacerManager() {
        return replacerManager;
    }

    @Nonnull
    public static UserManager getUserManager() {
        return userManager;
    }

    @Nonnull
    public static PacketListenerManager getPacketListenerManager() {
        return packetListenerManager;
    }

    private static void initialize() {
        replacerManager = new ReplacerManager();
        userManager = new UserManager();
        packetListenerManager = new PacketListenerManager();
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), instance);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), instance);
        packetListenerManager.initialize();
        replacerManager.initialize();
        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(player);
            player.updateInventory();
        }
    }

}