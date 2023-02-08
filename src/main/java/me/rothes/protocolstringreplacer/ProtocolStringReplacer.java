package me.rothes.protocolstringreplacer;

import com.sk89q.protocolstringreplacer.PsrDisguisePlugin;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import me.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.api.user.PsrUserManager;
import me.rothes.protocolstringreplacer.commands.CommandHandler;
import me.rothes.protocolstringreplacer.console.ConsoleReplaceManager;
import me.rothes.protocolstringreplacer.events.PsrReloadEvent;
import me.rothes.protocolstringreplacer.listeners.PlayerJoinListener;
import me.rothes.protocolstringreplacer.listeners.PlayerQuitListener;
import me.rothes.protocolstringreplacer.packetlisteners.PacketListenerManager;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import me.rothes.protocolstringreplacer.upgrades.AbstractUpgradeHandler;
import me.rothes.protocolstringreplacer.upgrades.UpgradeEnum;
import me.rothes.protocolstringreplacer.utils.FileUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
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
    private PsrUserManager userManager;
    private ConfigManager configManager;
    private byte serverMajorVersion;
    private byte serverMinorVersion;
    private boolean isSpigot;
    private boolean isPaper;
    private boolean hasPaperComponent;
    private boolean hasStarted;
    private boolean reloading;

    public ProtocolStringReplacer() {
        super();
        instance = this;
        // Hack the prefix of the Logger of this plugin.
        try {
            Field logger = JavaPlugin.class.getDeclaredField("logger");
            logger.setAccessible(true);
            logger.set(this, new PluginLogger(new PsrDisguisePlugin(this)));
            logger.setAccessible(false);

            Field name = PluginLogger.class.getDeclaredField("pluginName");
            name.setAccessible(true);

            final String background = ";48;2;5;15;40";
            final String bracket = "\u001b[38;2;255;106;0" + background + "m";
            final String red = "\u001b[0;91" + background + "m";
            final String gold = "\u001b[0;33" + background + "m";
            final String brightGold = "\u001b[38;2;220;175;0" + background + "m";
            final String reset = "\u001b[0m";

            name.set(this.getLogger(), bracket + "[" + red + "Protocol"
                    + gold + "String" + brightGold + "Replacer" + bracket + "]" + reset + " ");
            name.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        logger = this.getLogger();

        // Start Console Replacer first to remove the Ansi in log files.
        String[] split = Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.");
        serverMajorVersion = Byte.parseByte(split[1]);
        serverMinorVersion = split.length > 2 ? Byte.parseByte(split[2]) : 0;
        consoleReplaceManager = new ConsoleReplaceManager(this);
        consoleReplaceManager.initialize();

        loadConfigAndLocale();
        checkConfig();
        enableModify(ConfigManager.LifeCycle.INIT);
    }

    public static ProtocolStringReplacer getInstance() {
        return instance;
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void warn(String message) {
        logger.warning("\u001b[0;93m" + message + "\u001b[m");
    }

    public static void warn(String message, Throwable throwable) {
        logger.log(Level.WARNING, "\u001b[0;93m" + message + "\u001b[m", throwable);
    }

    public static void error(String message) {
        logger.severe("\u001b[0;91m" + message + "\u001b[m");
    }

    public static void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, "\u001b[0;91m" + message + "\u001b[m", throwable);
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public boolean isReloading() {
        return reloading;
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
    public void onLoad() {
        enableModify(ConfigManager.LifeCycle.LOAD);
    }

    @Override
    public void onEnable() {
        try {
            Class.forName("org.bukkit.entity.Player$Spigot");
            isSpigot = true;
        } catch (Throwable tr) {
            isSpigot = false;
            error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Require-Spigot"));
            Bukkit.getPluginManager().disablePlugin(instance);
            return;
        }
        try {
            Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData");
            isPaper = true;
            try {
                Class.forName("io.papermc.paper.text.PaperComponents");
                hasPaperComponent = true;
            } catch (Throwable tr) {
                hasPaperComponent = false;
            }
        } catch (Throwable tr) {
            isPaper = false;
            if (serverMajorVersion >= 12) {
                warn("\033[0;31m" + PsrLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Recommend-Paper") + "\033[0m");
            }
        }
        if (!checkDepends("PlaceholderAPI", "ProtocolLib")) {
            initialize();
            new Updater(this).start();
        }
    }

    @Override
    public void onDisable() {
        if (consoleReplaceManager != null) {
            consoleReplaceManager.disable();
        }
        if (packetListenerManager != null) {
            packetListenerManager.removeListeners();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.updateInventory();
            }
        }
        if (replacerManager != null) {
            replacerManager.saveReplacerConfigs();
        }
        Bukkit.getScheduler().cancelTasks(instance);
    }

    public byte getServerMajorVersion() {
        return serverMajorVersion;
    }

    public byte getServerMinorVersion() {
        return serverMinorVersion;
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
    public PsrUserManager getUserManager() {
        return userManager;
    }

    @Nonnull
    public PacketListenerManager getPacketListenerManager() {
        return packetListenerManager;
    }

    private void initialize() {
        enableModify(ConfigManager.LifeCycle.ENABLE);
        replacerManager.registerTask();
        CommandHandler commandHandler = new CommandHandler();
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), instance);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), instance);
        packetListenerManager = new PacketListenerManager();
        packetListenerManager.initialize();
        commandHandler.initialize();
        // init NBT-API
        MinecraftVersion.getVersion();
        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(player);
            player.updateInventory();
        }
    }

    private boolean checkDepends(String... depends) {
        boolean missingDepend = false;
        PluginManager pluginManager = Bukkit.getPluginManager();
        for (String depend : depends) {
            if (pluginManager.getPlugin(depend) == null) {
                error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Missing-Dependency", depend));
                missingDepend = true;
            }
        }
        if (missingDepend) {
            pluginManager.disablePlugin(instance);
        }
        return missingDepend;
    }

    private void enableModify(ConfigManager.LifeCycle lifeCycle) {
        if (lifeCycle == getConfigManager().loadConfigLifeCycle) {
            userManager = new PsrUserManager();
            replacerManager = new ReplacerManager();
            replacerManager.initialize();
            this.hasStarted = true;
        }
    }

    private void loadConfigAndLocale() {
        configFile = new File(instance.getDataFolder() + "/Config.yml");
        config = new CommentYamlConfiguration();
        if (!configFile.exists()) {
            PsrLocalization.initialize(instance);
            return;
        }

        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            PsrLocalization.initialize(instance);
            error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Config-Failed-To-Load"), e);
            config = PsrLocalization.getDefaultLocaledConfig();
            return;
        }
        PsrLocalization.initialize(instance);
    }

    private void checkConfig() {
        try {
            configFile = new File(instance.getDataFolder() + "/Config.yml");
            if (!configFile.exists()) {
                FileUtils.createFile(configFile);
                PsrLocalization.getDefaultLocaledConfig().save(configFile);
                configFile = new File(instance.getDataFolder() + "/Config.yml");
            }
            config = CommentYamlConfiguration.loadConfiguration(configFile);
            checkConfigsVersion();
            saveExampleReplacers();
        } catch (IOException e) {
            e.printStackTrace();
        }

        configManager = new ConfigManager(instance);
        checkConfigKeys();
    }

    private void saveExampleReplacers() throws IOException {
        if (!new File(instance.getDataFolder() + "/Replacers/").exists()) {
            saveResource("/Replacers/Example.yml");
            saveResource("/Replacers/ConsoleColor.yml");
            warn(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Created-Example-Replacers"));
        }
    }

    private void saveResource(String path) throws IOException {
        File file = new File(instance.getDataFolder(), path);
        FileUtils.createFile(file);
        try (InputStream inputStream = PsrLocalization.getLocaledResource(path)) {
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void checkConfigKeys() {
        CommentYamlConfiguration configDefault = PsrLocalization.getDefaultLocaledConfig();

        Pattern commentKeyPattern = CommentYamlConfiguration.getCommentKeyPattern();
        boolean edited = false;
        LinkedList<String> comments = new LinkedList<>();
        int index = 2333;
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
                    stringBuilder.append(index++).append(split[split.length - 1]);
                    config.set(stringBuilder.toString(), configDefault.getString(commentKey));
                }
                config.set(key, configDefault.get(key));
                warn(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Added-Missing-Config-Key", key));
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

    public void checkConfigsVersion() {
        HashMap<Short, AbstractUpgradeHandler> upgrades = new HashMap<>();
        for (UpgradeEnum upgrade : UpgradeEnum.values()) {
            try {
                upgrades.put(upgrade.getCurrentVersion(), upgrade.getUpgradeHandler().getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        boolean backup = true;
        for (short i = (short) config.getInt("Configs-Version", 1); i <= upgrades.size(); i++) {
            if (backup) {
                String path = "/backups/" + System.currentTimeMillis();
                info(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Backing-Up-Configs", path.substring(1)));
                backupConfigs(path);
                backup = false;
            }
            info(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Upgrading-Configs", String.valueOf(i), String.valueOf(i + 1)));
            upgrades.get(i).upgrade();
        }
    }

    public void backupConfigs(String path) {
        File bkp = new File(instance.getDataFolder(), path);
        bkp.mkdirs();
        try {
            FileUtils.copyDirectoryOrFile(new File(instance.getDataFolder(), "/Locale"), new File(bkp, "/Locale"));
            FileUtils.copyDirectoryOrFile(new File(instance.getDataFolder(), "/Replacers"), new File(bkp, "/Replacers"));
            FileUtils.copyDirectoryOrFile(new File(instance.getDataFolder(), "Config.yml"), new File(bkp, "Config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload(@Nonnull PsrUser user) {
        reloading = true;
        Validate.notNull(user, "user cannot be null");
        PsrReloadEvent event = new PsrReloadEvent(PsrReloadEvent.ReloadState.BEFORE, user);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            reloading = false;
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try {
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Commands.Reload.Async-Reloading"));
                loadConfigAndLocale();
                checkConfig();
                replacerManager.cancelCleanTask();
                replacerManager.saveReplacerConfigs();
                replacerManager = new ReplacerManager();
                replacerManager.initialize();
                replacerManager.registerTask();
                packetListenerManager.removeListeners();
                packetListenerManager.initialize();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    userManager.getUser(player).clearUserMetaCache();
                    player.updateInventory();
                }
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Commands.Reload.Complete"));
                Bukkit.getScheduler().runTask(instance, () -> {
                    // Don't need to check cancelled here
                    Bukkit.getServer().getPluginManager().callEvent(new PsrReloadEvent(PsrReloadEvent.ReloadState.FINISH, user));
                });
            } catch (Throwable t) {
                t.printStackTrace();
                user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Commands.Reload.Error-Occurred"));
            } finally {
                reloading = false;
            }
        });
    }

}
