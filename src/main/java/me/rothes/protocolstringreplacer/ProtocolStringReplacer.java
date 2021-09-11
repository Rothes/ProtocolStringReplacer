package me.rothes.protocolstringreplacer;

import com.sk89q.protocolstringreplacer.PSRDisguisePlugin;
import me.rothes.protocolstringreplacer.console.ConsoleReplaceManager;
import me.rothes.protocolstringreplacer.console.PSRMessage;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import me.rothes.protocolstringreplacer.upgrades.AbstractUpgradeHandler;
import me.rothes.protocolstringreplacer.user.User;
import me.rothes.protocolstringreplacer.user.UserManager;
import me.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import me.rothes.protocolstringreplacer.commands.CommandHandler;
import me.rothes.protocolstringreplacer.listeners.PlayerJoinListener;
import me.rothes.protocolstringreplacer.listeners.PlayerQuitListener;
import me.rothes.protocolstringreplacer.packetlisteners.PacketListenerManager;
import me.rothes.protocolstringreplacer.upgrades.UpgradeEnum;
import org.apache.commons.lang.Validate;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ProtocolStringReplacer extends JavaPlugin {

    private static ProtocolStringReplacer instance;
    private static Logger logger;
    private CommentYamlConfiguration config;
    private File configFile;
    private ReplacerManager replacerManager;
    private PacketListenerManager packetListenerManager;
    private ConsoleReplaceManager consoleReplaceManager;
    private UserManager userManager;
    private ConfigManager configManager;
    private byte serverMajorVersion;
    private boolean isSpigot;
    private boolean isPaper;
    private boolean hasPaperComponent;

    public ProtocolStringReplacer() {
        super();

        // Hack the prefix of the Logger of this plugin.
        try {
            Field logger = JavaPlugin.class.getDeclaredField("logger");
            logger.setAccessible(true);
            logger.set(this, new PluginLogger(new PSRDisguisePlugin(this)));
            logger.setAccessible(false);

            Field name = PluginLogger.class.getDeclaredField("pluginName");
            name.setAccessible(true);

            final String background = ";48;2;5;15;40";
            final String bracket = "\033[38;2;255;106;0" + background + "m";
            final String red = "\033[0;91" + background + "m";
            final String gold = "\033[0;33" + background + "m";
            final String brightGold = "\033[38;2;220;175;0" + background + "m";
            final String reset = "\033[0m";

            name.set(this.getLogger(), bracket + "[" + red + "Protocol"
                    + gold + "String" + brightGold + "Replacer" + bracket + "]" + reset + " ");
            name.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static ProtocolStringReplacer getInstance() {
        return instance;
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void warn(String message) {
        logger.warning(message);
    }

    public static void error(String message) {
        logger.severe(message);
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

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        loadConfig();
        PSRLocalization.initialize(instance);
        logger = this.getLogger();

        serverMajorVersion = Byte.parseByte(Bukkit.getServer().getBukkitVersion().split("\\.")[1].split("-")[0]);
//        info(PSRLocalization.getLocaledMessage("Console.Messages.Server-Version", String.valueOf(serverMajorVersion)));
        try {
            Class.forName("org.bukkit.entity.Player$Spigot");
            isSpigot = true;
        } catch (Throwable tr) {
            isSpigot = false;
            error(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Require-Spigot"));
            Bukkit.getPluginManager().disablePlugin(instance);
            return;
        }
        try {
            Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData");
            isPaper = true;
            try {
                Class.forName("io.papermc.paper.text.PaperComponents");
//                info(PSRLocalization.getLocaledMessage("Console.Messages.Paper-Component-Support"));
                hasPaperComponent = true;
            } catch (Throwable tr) {
                hasPaperComponent = false;
            }
        } catch (Throwable tr) {
            isPaper = false;
            if (serverMajorVersion > 12) {
                warn("\033[0;31m" + PSRLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Recommend-Paper") + "\033[0m");
            } else {
                warn("\033[0;31m" + PSRLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Out-Dated-Server") + "\033[0m");
            }
        }
        if (!checkDepends("PlaceholderAPI", "ProtocolLib")) {
            initialize();
        }
    }

    @Override
    public void onDisable() {
        if (consoleReplaceManager != null) {
            consoleReplaceManager.disable();
        }
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

    public boolean hasPaperComponent() {
        return hasPaperComponent;
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
        checkConfig();
        packetListenerManager = new PacketListenerManager();
        replacerManager = new ReplacerManager();
        CommandHandler commandHandler = new CommandHandler();
        userManager = new UserManager();
        if (serverMajorVersion >= 12) {
            PSRMessage.initialize(this);
        }
        consoleReplaceManager = new ConsoleReplaceManager(this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), instance);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), instance);
        packetListenerManager.initialize();
        consoleReplaceManager.initialize();
        commandHandler.initialize();
        replacerManager.initialize();
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
            if (pluginManager.getPlugin(depend) == null) {
                error(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Missing-Dependency", depend));
                missingDepend = true;
            }
        }
        if (missingDepend) {
            pluginManager.disablePlugin(instance);
        }
        return missingDepend;
    }

    private void loadConfig() {
        configFile = new File(instance.getDataFolder() + "/Config.yml");
        config = CommentYamlConfiguration.loadConfiguration(configFile);
    }

    private void checkConfig() {
        try {
            configFile = new File(instance.getDataFolder() + "/Config.yml");
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
                PSRLocalization.getDefaultLocaledConfig().save(configFile);
                configFile = new File(instance.getDataFolder() + "/Config.yml");
            }
            config = CommentYamlConfiguration.loadConfiguration(configFile);
            checkConfigsVersion();
            if (!new File(instance.getDataFolder() + "/Replacers/").exists()) {

                File exampleFile = new File(instance.getDataFolder() + "/Replacers/Example.yml");
                exampleFile.getParentFile().mkdirs();
                exampleFile.createNewFile();
                PSRLocalization.getDefaultLocaledExample()
                        .save(exampleFile);
                warn(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Created-Example-Replacer"));
            }
            configManager = new ConfigManager(instance);
        } catch (IOException e) {
            e.printStackTrace();
        }

        CommentYamlConfiguration configDefault = PSRLocalization.getDefaultLocaledConfig();

        Pattern commentKeyPattern = CommentYamlConfiguration.getCommentKeyPattern();
        boolean edited = false;
        LinkedList<String> comments = new LinkedList<>();
        for (String key : configDefault.getKeys(true)) {
            if (configDefault.get(key) instanceof ConfigurationSection) {
                continue;
            }
            if (commentKeyPattern.matcher(key).find()) {
                comments.add(key);
                continue;
            }

            if (key.equals("Configs-Version")) {
                comments.clear();
                continue;
            }

            if (!config.contains(key)) {
                for (String commentKey : comments) {
                    String[] split = commentKey.split("\\.");
                    StringBuilder stringBuilder = new StringBuilder(commentKey.length() + 4);
                    for (byte i = 0; i < split.length - 1; i++) {
                        stringBuilder.append(split[i]).append(".");
                    }
                    stringBuilder.append("2333").append(split[split.length - 1]);
                    config.set(stringBuilder.toString(), configDefault.getString(commentKey));
                }
                config.set(key, configDefault.get(key));
                warn(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Added-Missing-Config-Key", key));
                edited = true;
            }
            comments.clear();
        }

        if (edited) {
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void checkConfigsVersion() {
        HashMap<Short, AbstractUpgradeHandler> upgrades = new HashMap<>();
        for (UpgradeEnum upgrade : UpgradeEnum.values()) {
            upgrades.put(upgrade.getCurrentVersion(), upgrade.getUpgradeHandler());
        }
        for (short i = (short) config.getInt("Configs-Version", 1); i <= upgrades.size(); i++) {
            info(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Upgrading-Configs", String.valueOf(i), String.valueOf(i + 1)));
            upgrades.get(i).upgrade();
        }
    }

    public void reload(@Nonnull User user) {
        Validate.notNull(user, "user cannot be null");
        loadConfig();
        replacerManager.getCleanTask().cancel();
        replacerManager = new ReplacerManager();
        replacerManager.initialize();
        userManager = new UserManager();
        packetListenerManager.removeListeners();
        packetListenerManager.addListeners();
        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(player);
            player.updateInventory();
        }
        user.sendFilteredText(PSRLocalization.getPrefixedLocaledMessage("Sender.Commands.Reload.Complete"));
    }

}
