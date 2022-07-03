package me.rothes.protocolstringreplacer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sk89q.protocolstringreplacer.PsrDisguisePlugin;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.console.ConsoleReplaceManager;
import me.rothes.protocolstringreplacer.console.PsrMessage;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import me.rothes.protocolstringreplacer.replacer.ReplacesMode;
import me.rothes.protocolstringreplacer.upgrades.AbstractUpgradeHandler;
import me.rothes.protocolstringreplacer.api.user.PsrUserManager;
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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ProtocolStringReplacer extends JavaPlugin {

    public static final String VERSION_CHANNEL = "Stable";
    public static final int VERSION_NUMBER = 92;
    private static ProtocolStringReplacer instance;
    private static Logger logger;
    private final HashMap<String, Integer> msgTimes = new HashMap<>();
    private CommentYamlConfiguration config;
    private File configFile;
    private ReplacerManager replacerManager;
    private PacketListenerManager packetListenerManager;
    private ConsoleReplaceManager consoleReplaceManager;
    private PsrUserManager userManager;
    private ConfigManager configManager;
    private byte serverMajorVersion;
    private boolean isSpigot;
    private boolean isPaper;
    private boolean hasPaperComponent;
    private boolean hasStarted;

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
        PsrMessage.initialize(instance);
        serverMajorVersion = Byte.parseByte(Bukkit.getServer().getBukkitVersion().split("\\.")[1].split("-")[0]);
        consoleReplaceManager = new ConsoleReplaceManager(this);
        consoleReplaceManager.initialize();
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

    public static void warn(String message, Throwable throwable) {
        logger.log(Level.WARNING, message, throwable);
    }

    public static void error(String message) {
        logger.severe(message);
    }

    public boolean hasStarted() {
        return hasStarted;
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
        loadConfig();
        PsrLocalization.initialize(instance);

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
    public PsrUserManager getUserManager() {
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
        userManager = new PsrUserManager();
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), instance);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), instance);
        packetListenerManager.initialize();
        commandHandler.initialize();
        replacerManager.initialize();
        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(player);
            player.updateInventory();
        }
        this.hasStarted = true;
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
                error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Missing-Dependency", depend));
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
                PsrLocalization.getDefaultLocaledConfig().save(configFile);
                configFile = new File(instance.getDataFolder() + "/Config.yml");
            }
            config = CommentYamlConfiguration.loadConfiguration(configFile);
            checkConfigsVersion();
            if (!new File(instance.getDataFolder() + "/Replacers/").exists()) {

                File exampleFile = new File(instance.getDataFolder() + "/Replacers/Example.yml");
                FileUtils.createFile(exampleFile);
                PsrLocalization.getDefaultLocaledExample()
                        .save(exampleFile);
                warn(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Created-Example-Replacer"));
            }
            configManager = new ConfigManager(instance);
        } catch (IOException e) {
            e.printStackTrace();
        }

        checkConfigKeys();
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
        for (short i = (short) config.getInt("Configs-Version", 1); i <= upgrades.size(); i++) {
            info(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Initialize.Upgrading-Configs", String.valueOf(i), String.valueOf(i + 1)));
            upgrades.get(i).upgrade();
        }
    }

    /**
     * @return false if plugin doesn't pass the check.
     * @since 2.0.0
     */
    public boolean checkPluginVersion() {
        try {
            final URL url = new URL("https://" + getConfigManager().gitRawHost + "/Rothes/ProtocolStringReplacer/master/Version%20Infos.json");
            final InputStream stream = url.openStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            final StringBuilder jsonBuilder = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                jsonBuilder.append(line).append("\n");
            }
            stream.close();
            reader.close();
            try {
                final JsonElement element = new JsonParser().parse(jsonBuilder.toString());
                final JsonObject root = element.getAsJsonObject();
                JsonObject channel = root.getAsJsonObject("Version_Channels").getAsJsonObject(VERSION_CHANNEL);
                if (channel == null) {
                    warn(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Updater.Invalid-Channel"));
                } else if (channel.has("Message")){
                    if (Integer.parseInt(channel.getAsJsonPrimitive("Latest_Version_Number").getAsString())
                        > VERSION_NUMBER) {

                        for (String s : getLocaledJsonMessage(channel.getAsJsonObject("Message")).split("\n")) {
                            warn(s);
                        }
                    }
                }

                boolean prohibit = false;
                for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("Version_Actions").entrySet()) {
                    String[] split = entry.getKey().split("-");
                    if (Integer.parseInt(split[1]) > VERSION_NUMBER
                            && VERSION_NUMBER > Integer.parseInt(split[0])) {
                        JsonObject json = (JsonObject) entry.getValue();
                        if (json.has("Message")) {
                            JsonElement temp = json.get("Message_Times");
                            final int msgTimes = temp == null ? -1 : temp.getAsInt();
                            final int curTimes = this.msgTimes.get(entry.getKey()) == null ? 0 : this.msgTimes.get(entry.getKey());
                            if (msgTimes == -1 || curTimes < msgTimes) {
                                temp = json.get("Log_Level");
                                for (String s : getLocaledJsonMessage(json.getAsJsonObject("Message")).split("\n")) {
                                    switch (temp == null ? "default maybe" : temp.getAsString()) {
                                        case "Error":
                                            error(s);
                                            break;
                                        case "Warn":
                                            warn(s);
                                            break;
                                        case "Info":
                                        default:
                                            info(s);
                                            break;
                                    }
                                }
                                this.msgTimes.put(entry.getKey(), curTimes + 1);
                            }
                        }
                        for (JsonElement action : json.getAsJsonArray("Actions")) {
                            prohibit = prohibit || action.getAsString().equals("Prohibit");
                        }
                    }
                }
                return !prohibit;
            } catch (IllegalStateException | NullPointerException e) {
                error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Updater.Error-Parsing-Json", e.toString()));
                e.printStackTrace();
            }
        } catch (IOException e) {
            // error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Updater.Error-Checking-Version", e.toString()));
        }
        return true;
    }

    public static String getLocaledJsonMessage(@NotNull JsonObject messageJson) {
        String msg;
        if (messageJson.has(PsrLocalization.getLocale())) {
            msg = messageJson.get(PsrLocalization.getLocale()).getAsString();
        } else {
            msg = messageJson.get("en-US").getAsString();
        }
        return msg;
    }

    public void reload(@Nonnull PsrUser user) {
        Validate.notNull(user, "user cannot be null");
        loadConfig();
        replacerManager.getCleanTask().cancel();
        replacerManager = new ReplacerManager();
        replacerManager.initialize();
        userManager = new PsrUserManager();
        packetListenerManager.removeListeners();
        packetListenerManager.addListeners();
        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(player);
            player.updateInventory();
        }
        user.sendFilteredText(PsrLocalization.getPrefixedLocaledMessage("Sender.Commands.Reload.Complete"));
    }

}
