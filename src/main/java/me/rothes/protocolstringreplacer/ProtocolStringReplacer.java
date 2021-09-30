package me.rothes.protocolstringreplacer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sk89q.protocolstringreplacer.PSRDisguisePlugin;
import me.rothes.protocolstringreplacer.console.ConsoleReplaceManager;
import me.rothes.protocolstringreplacer.console.PSRMessage;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import me.rothes.protocolstringreplacer.replacer.ReplacesMode;
import me.rothes.protocolstringreplacer.upgrades.AbstractUpgradeHandler;
import me.rothes.protocolstringreplacer.user.User;
import me.rothes.protocolstringreplacer.user.UserManager;
import me.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import me.rothes.protocolstringreplacer.commands.CommandHandler;
import me.rothes.protocolstringreplacer.listeners.PlayerJoinListener;
import me.rothes.protocolstringreplacer.listeners.PlayerQuitListener;
import me.rothes.protocolstringreplacer.packetlisteners.PacketListenerManager;
import me.rothes.protocolstringreplacer.upgrades.UpgradeEnum;
import me.rothes.protocolstringreplacer.utils.FileUtils;
import org.apache.commons.lang.Validate;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
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
    private Pattern digits = Pattern.compile("[^0-9]+");

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
        Bukkit.getScheduler().cancelTasks(instance);
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
        PSRMessage.initialize(this);
        consoleReplaceManager = new ConsoleReplaceManager(this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), instance);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), instance);
        packetListenerManager.initialize();
        consoleReplaceManager.initialize();
        commandHandler.initialize();
        replacerManager.initialize();
        consoleReplaceManager.getPsrFilter().start();
        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(player);
            player.updateInventory();
        }
        initMetrics();
        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            if (!checkPluginVersion()) {
                Bukkit.getPluginManager().disablePlugin(instance);
            }
        }, 0L, 72000L);
    }

    private void initMetrics() {
        Metrics metrics = new Metrics(this, 11740);
        metrics.addCustomChart(new DrilldownPie("Replaces_Count", () -> {
            int configs = 0;
            int replaces = 0;
            for (ReplacerConfig replacerConfig : replacerManager.getReplacerConfigList()) {
                configs++;
                for (ReplacesMode mode : ReplacesMode.values()) {
                    replaces += replacerConfig.getReplaces(mode).size();
                }
            }
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();
            entry.put(replaces + (replaces >= 1 ? " Replaces" : " Replace"), 1);
            map.put(configs + (configs >= 1 ? " Configs" : " Config"), entry);
            return map;
        }));
        metrics.addCustomChart(new DrilldownPie("Blocks_Count", () -> {
            int configs = 0;
            int blocks = 0;
            for (ReplacerConfig replacerConfig : replacerManager.getReplacerConfigList()) {
                configs++;
                for (ReplacesMode mode : ReplacesMode.values()) {
                    blocks += replacerConfig.getBlocks(mode).size();
                }
            }
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();
            entry.put(blocks + (blocks >= 1 ? " Blocks" : " Block"), 1);
            map.put(configs + (configs >= 1 ? " Configs" : " Config"), entry);
            return map;
        }));
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
                FileUtils.createFile(configFile);
                PSRLocalization.getDefaultLocaledConfig().save(configFile);
                configFile = new File(instance.getDataFolder() + "/Config.yml");
            }
            config = CommentYamlConfiguration.loadConfiguration(configFile);
            checkConfigsVersion();
            if (!new File(instance.getDataFolder() + "/Replacers/").exists()) {

                File exampleFile = new File(instance.getDataFolder() + "/Replacers/Example.yml");
                FileUtils.createFile(exampleFile);
                PSRLocalization.getDefaultLocaledExample()
                        .save(exampleFile);
                warn(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Created-Example-Replacer"));
            }
            configManager = new ConfigManager(instance);
        } catch (IOException e) {
            e.printStackTrace();
        }

        checkConfigKeys();
    }

    private void checkConfigKeys() {
        CommentYamlConfiguration configDefault = PSRLocalization.getDefaultLocaledConfig();

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
            try {
                upgrades.put(upgrade.getCurrentVersion(), upgrade.getUpgradeHandler().getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        for (short i = (short) config.getInt("Configs-Version", 1); i <= upgrades.size(); i++) {
            info(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Upgrading-Configs", String.valueOf(i), String.valueOf(i + 1)));
            upgrades.get(i).upgrade();
        }
    }

    /**
     * @return false if plugin doesn't pass the check.
     * @since 2.0.0
     */
    private boolean checkPluginVersion() {
        try {
            final InputStream stream = new URL("https://raw.githubusercontent.com/Rothes/ProtocolStringReplacer/master/Version%20Infos.json").openStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder jsonBuilder = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                jsonBuilder.append("\n").append(line);
            }
            if (jsonBuilder == null) {
                error(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Version-Checker.Error-Checking-Version"));
                return true;
            }
            try {
                final JsonElement element = new JsonParser().parse(jsonBuilder.toString());
                final JsonObject root = element.getAsJsonObject();
                String latestVersion = root.getAsJsonPrimitive("Latest_Version").getAsString();
                if (!compareVersion(latestVersion)) {
                    // Only to notice server admins to update the plugin here.
                    warn(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Version-Checker.New-Version-Available", latestVersion));
                }
                for (JsonElement version : root.getAsJsonArray("Prohibit_Versions")) {
                    if (!compareVersion(version.getAsJsonPrimitive().getAsString())) {
                        error(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Version-Checker.Prohibited-Version"));
                        return false;
                    }
                }
                return true;
            } catch (IllegalStateException | NullPointerException e) {
                error(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Version-Checker.Error-Parsing-Json", e.toString()));
                return true;
            }
        } catch (IOException e) {
            error(PSRLocalization.getLocaledMessage("Console-Sender.Messages.Version-Checker.Error-Checking-Version", e.toString()));
            return true;
        }
    }

    /**
     * @param version The version to check.
     * @return false if version is newer than the current.
     * @since 2.0.0
     */
    private boolean compareVersion(@NotNull String version) {
        String[] ver = version.split("\\.");
        String[] current = getDescription().getVersion().split("\\.");
        for (byte i = 0 ; i < ver.length; i++) {
            String s = getDigits(ver[i]);
            if (s.isEmpty()) {
                return true;
            }
            if (current.length <= i) {
                return false;
            }
            String cur = getDigits(current[i]);
            if (cur.isEmpty()) {
                return false;
            }
            int verInt = Integer.parseInt(s);
            int curInt = Integer.parseInt(cur);
            if (verInt > curInt) {
                return false;
            }
        }
        return true;
    }

    private String getDigits(@NotNull String string) {
        Matcher matcher = digits.matcher(string);
        if (matcher.matches()) {
            return matcher.replaceAll("");
        }
        return string;
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
