package me.Rothes.ProtocolStringReplacer;

import me.Rothes.ProtocolStringReplacer.API.Configuration.CommentYamlConfiguration;
import me.Rothes.ProtocolStringReplacer.Commands.CommandHandler;
import me.Rothes.ProtocolStringReplacer.Listeners.PlayerJoinListener;
import me.Rothes.ProtocolStringReplacer.Listeners.PlayerQuitListener;
import me.Rothes.ProtocolStringReplacer.PacketListeners.PacketListenerManager;
import me.Rothes.ProtocolStringReplacer.Replacer.ReplacerManager;
import me.Rothes.ProtocolStringReplacer.Upgrades.UpgradeEnum;
import me.Rothes.ProtocolStringReplacer.User.User;
import me.Rothes.ProtocolStringReplacer.User.UserManager;
import org.apache.commons.lang.Validate;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
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
    private byte serverMajorVersion;
    private boolean isSpigot;
    private boolean isPaper;

    public static ProtocolStringReplacer getInstance() {
        return instance;
    }

    @NotNull
    @Override
    public CommentYamlConfiguration getConfig() {
        return config;
    }

    @NotNull
    public File getConfigFile() {
        return configFile;
    }

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §a正在加载插件...");
        instance = this;
        serverMajorVersion = Byte.parseByte(Bukkit.getServer().getBukkitVersion().split("\\.")[1].split("-")[0]);
        Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §a服务端 Minecraft 版本: 1." + serverMajorVersion + ".");
        try {
            Class.forName("org.bukkit.entity.Player$Spigot");
            isSpigot = true;
        } catch (Throwable tr) {
            isSpigot = false;
            Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §c本插件需要使用 Spigot 或其衍生服务端.");
            Bukkit.getPluginManager().disablePlugin(instance);
            return;
        }
        try {
            Class.forName("io.papermc.paper.text.PaperComponents");
            Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §3启用 Paper 1.17+ 支持.");
            isPaper = true;
        } catch (Throwable tr) {
            isPaper = false;
        }
        if (!checkDepends("PlaceholderAPI", "ProtocolLib")) {
            initialize();
            Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §a插件已成功加载");
        }
    }

    @Override
    public void onDisable() {
        if (packetListenerManager != null) {
            packetListenerManager.removeListeners();
        }
        if (replacerManager != null) {
            replacerManager.saveReplacerConfigs();
        }
    }

    public byte getServerMajorVersion() {
        return serverMajorVersion;
    }

    public boolean isSpigot() {
        return isSpigot;
    }

    public boolean isPaper() {
        return isPaper;
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

    private void initialize() {
        loadConfig();
        packetListenerManager = new PacketListenerManager();
        replacerManager = new ReplacerManager();
        CommandHandler commandHandler = new CommandHandler();
        userManager = new UserManager();
        checkConfigsVersion();
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), instance);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), instance);
        packetListenerManager.initialize();
        replacerManager.initialize();
        commandHandler.initialize();
        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(player);
            player.updateInventory();
        }
        Metrics metrics = new Metrics(this, 11740);
        metrics.addCustomChart(new SimplePie("replaces_count", () -> String.valueOf(replacerManager.getReplacesCount())));
        metrics.addCustomChart(new SimplePie("replace_configs_count", () -> String.valueOf(replacerManager.getReplacerConfigList().size())));
    }

    private boolean checkDepends(String... depends) {
        boolean missingDepend = false;
        PluginManager pluginManager = Bukkit.getPluginManager();
        for (String depend : depends) {
            if (!pluginManager.isPluginEnabled(depend)) {
                Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §c未检测到前置插件 " + depend + "，请安装后再使用本插件.");
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
        configFile = new File(instance.getDataFolder() + "/Config.yml");
        if (!configFile.exists()) {
            instance.saveResource("Config.yml", true);
            configFile = new File(instance.getDataFolder() + "/Config.yml");
        }
        config = CommentYamlConfiguration.loadConfiguration(configFile);
    }

    private void checkConfigsVersion() {
        short configsVersion = (short) config.getInt("Configs-Version", 1);
        for (var upgrade : UpgradeEnum.values()) {
            if (upgrade.getCurrentVersion() == configsVersion) {
                Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §a正在升级配置文件版本 " + configsVersion + " -> " + (configsVersion + 1));
                upgrade.getUpgradeHandler().upgrade();
                configsVersion = (short) config.getInt("Configs-Version");
            }
        }
    }

    public void reload(@Nonnull User user) {
        Validate.notNull(user, "User cannot be null");
        loadConfig();
        replacerManager.getCleanTask().cancel();
        replacerManager = new ReplacerManager();
        checkConfigsVersion();
        replacerManager.initialize();
        userManager = new UserManager();
        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(player);
            player.updateInventory();
        }
        user.sendFilteredText("§c§lP§6§lS§3§lR §e> §a插件已重载完毕.");
    }

}
