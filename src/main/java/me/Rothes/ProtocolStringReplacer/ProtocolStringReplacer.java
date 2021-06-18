package me.Rothes.ProtocolStringReplacer;

import me.Rothes.ProtocolStringReplacer.API.Configuration.CommentYamlConfiguration;
import me.Rothes.ProtocolStringReplacer.Commands.CommandHandler;
import me.Rothes.ProtocolStringReplacer.Listeners.PlayerJoinListener;
import me.Rothes.ProtocolStringReplacer.Listeners.PlayerQuitListener;
import me.Rothes.ProtocolStringReplacer.PacketListeners.PacketListenerManager;
import me.Rothes.ProtocolStringReplacer.Replacer.ReplacerManager;
import me.Rothes.ProtocolStringReplacer.User.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.File;

public class ProtocolStringReplacer extends JavaPlugin {

    private static ProtocolStringReplacer instance;
    private CommentYamlConfiguration config;
    private File configFile;
    private ReplacerManager replacerManager;
    private PacketListenerManager packetListenerManager;
    private UserManager userManager;
    private CommandHandler commandHandler;
    private byte serverMajorVersion;

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
        serverMajorVersion = Byte.parseByte(Bukkit.getServer().getBukkitVersion().split("\\.")[1].split("-")[0]);
        if (!checkDepends("PlaceholderAPI", "ProtocolLib")) {
            initialize();
            Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §a插件已成功加载");
        }
    }

    public void onDisable() {
        if (packetListenerManager != null) {
            packetListenerManager.removeListeners();
        }
    }

    public byte getServerMajorVersion() {
        return serverMajorVersion;
    }

    @Nonnull
    public ReplacerManager getReplacerManager() {
        return replacerManager;
    }

    @Nonnull
    public UserManager getUserManager() {
        return userManager;
    }

    @Nonnull
    public PacketListenerManager getPacketListenerManager() {
        return packetListenerManager;
    }

    @Nonnull
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    private void initialize() {
        loadConfig();
        packetListenerManager = new PacketListenerManager();
        replacerManager = new ReplacerManager();
        commandHandler = new CommandHandler();
        userManager = new UserManager();
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), instance);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), instance);
        packetListenerManager.initialize();
        replacerManager.initialize();
        commandHandler.initialize();
        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(player);
            player.updateInventory();
        }
    }

    private boolean checkDepends(String... depends) {
        boolean missingDepend = false;
        PluginManager pluginManager = Bukkit.getPluginManager();
        for (String depend : depends) {
            if (!pluginManager.isPluginEnabled(depend)) {
                Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §c未检测到前置插件 " + depend + "，禁用插件.");
                missingDepend = true;
            }
        }
        if (missingDepend) {
            pluginManager.disablePlugin(instance);
        }
        return missingDepend;
    }

    private void loadConfig() {
        if (!new File(instance.getDataFolder() + "/Replacers/").exists()) {
            instance.saveResource("Replacers/Example.yml", true);
        }
        File configFile = new File(instance.getDataFolder() + "/Config.yml");
        if (!configFile.exists()) {
            instance.saveResource("Config.yml", true);
            configFile = new File(instance.getDataFolder() + "/Config.yml");
        }
        config = CommentYamlConfiguration.loadConfiguration(configFile);
    }

    public void reload(CommandSender sender) {
        if (sender == null) {
            sender = Bukkit.getConsoleSender();
        }
        loadConfig();
        replacerManager.getCleanTask().cancel();
        replacerManager = new ReplacerManager();
        userManager = new UserManager();
        replacerManager.initialize();
        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(player);
            player.updateInventory();
        }
        sender.sendMessage("§c§lP§6§lS§3§lR §e> §a插件已重载完毕.");
    }

}
