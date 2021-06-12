package me.Rothes.ProtocolStringReplacer;

import me.Rothes.ProtocolStringReplacer.API.Configuration.CommentYamlConfiguration;
import me.Rothes.ProtocolStringReplacer.Listeners.PlayerJoinListener;
import me.Rothes.ProtocolStringReplacer.Listeners.PlayerQuitListener;
import me.Rothes.ProtocolStringReplacer.PacketListeners.PacketListenerManager;
import me.Rothes.ProtocolStringReplacer.Replacer.ReplacerManager;
import me.Rothes.ProtocolStringReplacer.User.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

public class ProtocolStringReplacer extends JavaPlugin {

    private static CommentYamlConfiguration config = new CommentYamlConfiguration();
    private static File configFile;
    private static ProtocolStringReplacer instance;
    private static ReplacerManager replacerManager;
    private static PacketListenerManager packetListenerManager;
    private static UserManager userManager;

    public static ProtocolStringReplacer getInstance() {
        return instance;
    }

    @NotNull
    @Override
    public CommentYamlConfiguration getConfig() {
        return config;
    }

    public void onEnable() {
        instance = this;
        if (!checkDepends("PlaceholderAPI", "ProtocolLib")) {
            initialize();
            Bukkit.getConsoleSender().sendMessage("§7[§6ProtocolStringReplacer§7] §a插件已成功加载");
        }
    }

    public void onDisable() {
        if (packetListenerManager != null) {
            packetListenerManager.removeListeners();
        }
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
        if (!new File(instance.getDataFolder() + "/Replacers/").exists()) {
            instance.saveResource("Replacers/Example.yml", true);
        }
        File configFile = new File(instance.getDataFolder() + "/Config.yml");
        if (!configFile.exists()) {
            instance.saveResource("Config.yml", true);
            configFile = new File(instance.getDataFolder() + "/Config.yml");
        }
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), instance);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), instance);
        packetListenerManager.initialize();
        replacerManager.initialize();
        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(player);
            player.updateInventory();
        }
    }

    private boolean checkDepends(String... depends) {
        boolean missingDepend = false;
        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin dependPlugin;
        for (String depend : depends) {
            dependPlugin = pluginManager.getPlugin(depend);
            if (dependPlugin == null || !dependPlugin.isEnabled()) {
                Bukkit.getConsoleSender().sendMessage("§7[§6ProtocolStringReplacer§7] §c未检测到" + depend + "，禁用插件.");
                missingDepend = true;
            }
        }
        if (missingDepend) {
            pluginManager.disablePlugin(instance);
        }
        return missingDepend;
    }

}