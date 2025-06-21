package io.github.rothes.protocolstringreplacer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.replacer.ReplaceMode;
import io.github.rothes.protocolstringreplacer.util.scheduler.PsrScheduler;
import org.bstats.MetricsBase;
import org.bstats.charts.CustomChart;
import org.bstats.charts.DrilldownPie;
import org.bstats.json.JsonObjectBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class Updater implements Listener {

    private final HashMap<String, Integer> msgTimesMap = new HashMap<>();
    private final List<String> messages = new ArrayList<>();
    private final ProtocolStringReplacer plugin;
    private final String VERSION_CHANNEL;
    private final int VERSION_NUMBER;
    private int errorCount = 0;

    Updater(ProtocolStringReplacer plugin) {
        this.plugin = plugin;
        YamlConfiguration meta = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("metadata.yml"), StandardCharsets.UTF_8));
        VERSION_CHANNEL = meta.getString("version-channel", "unknown");
        VERSION_NUMBER = meta.getInt("version-id");
    }

    public void start() {
        initMetrics();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        PsrScheduler.runTaskTimerAsynchronously(() -> {
            try {
                String json = getJson(plugin.getConfigManager().gitRawHost, 0);
                if (json == null) {
                    return;
                }
                checkJson(json);
            } catch (Throwable e) {
                ProtocolStringReplacer.error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Updater.Error-Parsing-Json"), e);
            }
        }, 0L, 72000L);
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        PsrScheduler.runTaskAsynchronously(() -> {
            if (e.getPlayer().hasPermission("protocolstringreplacer.updater.notify")) {
                for (String message : messages) {
                    e.getPlayer().sendMessage(PsrLocalization.getLocaledMessage("Sender.Prefix") + message);
                }

            }
        });
    }

    private void initMetrics() {
        BStatsMetrics bStatsMetrics = new BStatsMetrics(plugin, 11740);
        bStatsMetrics.addCustomChart(new DrilldownPie("Replaces_Count", () -> {
            int configs = 0;
            int replaces = 0;
            for (ReplacerConfig replacerConfig : plugin.getReplacerManager().getReplacerConfigList()) {
                configs++;
                for (ReplaceMode mode : ReplaceMode.values()) {
                    replaces += replacerConfig.getReplaces(mode).size();
                }
            }
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();
            entry.put(replaces + (replaces > 1 ? " Replaces" : " Replace"), 1);
            map.put(configs + (configs > 1 ? " Configs" : " Config"), entry);
            return map;
        }));
        bStatsMetrics.addCustomChart(new DrilldownPie("Blocks_Count", () -> {
            int configs = 0;
            int blocks = 0;
            for (ReplacerConfig replacerConfig : plugin.getReplacerManager().getReplacerConfigList()) {
                configs++;
                for (ReplaceMode mode : ReplaceMode.values()) {
                    blocks += replacerConfig.getBlocks(mode).size();
                }
            }
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();
            entry.put(blocks + (blocks > 1 ? " Blocks" : " Block"), 1);
            map.put(configs + (configs > 1 ? " Configs" : " Config"), entry);
            return map;
        }));
    }

    private String getJson(String domain, int tryTime) {
        try (
                InputStream stream = new URL("https://" + domain + "/Rothes/ProtocolStringReplacer/master/Version_Info.json")
                        .openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
        ){
            StringBuilder jsonBuilder = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                jsonBuilder.append(line).append("\n");
            }
            // noinspection deprecation // For legacy server supports
            new JsonParser().parse(jsonBuilder.toString()); // Try parse
            errorCount = Math.max(errorCount - 1, 0);
            return jsonBuilder.toString();
        } catch (Throwable e) {
            if (tryTime == 0) {
                return getJson("ghfast.top/https://raw.githubusercontent.com", ++tryTime);
            } else if (tryTime == 1) {
                return getJson("raw.githubusercontent.com", ++tryTime);
            }
            if (errorCount < 3) {
                errorCount++;
                ProtocolStringReplacer.error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Updater.Error-Checking-Version", e.toString()));
            }
            return null;
        }
    }

    private void checkJson(String json) {
        @SuppressWarnings("deprecation") // For legacy server supports
        JsonElement element = new JsonParser().parse(json);
        JsonObject root = element.getAsJsonObject();
        JsonObject channels = root.getAsJsonObject("Version_Channels");

        messages.clear();
        if (channels.has(VERSION_CHANNEL)) {
            JsonObject channel = channels.getAsJsonObject(VERSION_CHANNEL);
            if (channel.has("Message")
                    && channel.getAsJsonPrimitive("Latest_Version_Number").getAsInt() > VERSION_NUMBER) {
                sendJsonMessage(channel, "updater");
            }
        } else {
            ProtocolStringReplacer.warn(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Updater.Invalid-Channel", VERSION_CHANNEL));
            messages.add(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Updater.Invalid-Channel", VERSION_CHANNEL));
            return;
        }

        for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("Version_Actions").entrySet()) {
            String[] split = entry.getKey().split("-");
            if (Integer.parseInt(split[1]) > VERSION_NUMBER && VERSION_NUMBER > Integer.parseInt(split[0])) {
                JsonObject message = (JsonObject) entry.getValue();
                if (message.has("Message"))
                    sendJsonMessage(message, entry.getKey());
            }
        }

    }

    private void sendJsonMessage(JsonObject json, String id) {
        JsonObject msgJson = json.getAsJsonObject("Message");
        String msg = getLocaledJsonMessage(msgJson);
        if (msg == null) {
            return;
        }

        int msgTimes = json.has("Message_Times") ? json.get("Message_Times").getAsInt() : -1;
        int curTimes = msgTimesMap.get(id) == null ? 0 : msgTimesMap.get(id);

        if (msgTimes == -1 || curTimes < msgTimes) {

            String logLevel = json.has("Log_Level") ? json.get("Log_Level").getAsString() : "default maybe";

            for (String s : msg.split("\n")) {
                if (!json.has("Notify_In_Game") || json.get("Notify_In_Game").getAsBoolean()) {
                    messages.add(s);
                }
                switch (logLevel) {
                    case "Error":
                        ProtocolStringReplacer.error(s);
                        break;
                    case "Warn":
                        ProtocolStringReplacer.warn(s);
                        break;
                    case "Info":
                    default:
                        ProtocolStringReplacer.info(s);
                        break;
                }
            }
            msgTimesMap.put(id, ++curTimes);
        }

        checkActions(json.getAsJsonArray("Actions"));
    }

    private void checkActions(JsonArray actions) {
        if (actions == null) {
            return;
        }
        for (JsonElement action : actions) {
            if (action.getAsString().equals("Prohibit")) {
                Bukkit.getPluginManager().disablePlugin(plugin);
                return;
            }
        }
    }

    private String getLocaledJsonMessage(@NotNull JsonObject messageJson) {
        String msg = null;
        if (messageJson.has(PsrLocalization.getLocale())) {
            msg = messageJson.get(PsrLocalization.getLocale()).getAsString();
        } else if (messageJson.has("en-US")) {
            msg = messageJson.get("en-US").getAsString();
        }
        return msg;
    }

    static final class BStatsMetrics {

        private final Plugin plugin;
        private final MetricsBase metricsBase;

        /**
         * Creates a new Metrics instance.
         *
         * @param plugin Your plugin instance.
         * @param serviceId The id of the service.
         *                  It can be found at <a href="https://bstats.org/what-is-my-plugin-id">What is my plugin id?</a>
         */
        public BStatsMetrics(JavaPlugin plugin, int serviceId) {
            this.plugin = plugin;

            // Get the config file
            File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
            File configFile = new File(bStatsFolder, "config.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

            if (!config.isSet("serverUuid")) {
                config.addDefault("enabled", true);
                config.addDefault("serverUuid", UUID.randomUUID().toString());
                config.addDefault("logFailedRequests", false);
                config.addDefault("logSentData", false);
                config.addDefault("logResponseStatusText", false);

                // Inform the server owners about bStats
                config.options().header(
                        "bStats (https://bStats.org) collects some basic information for plugin authors, like how\n" +
                                "many people use their plugin and their total player count. It's recommended to keep bStats\n" +
                                "enabled, but if you're not comfortable with this, you can turn this setting off. There is no\n" +
                                "performance penalty associated with having metrics enabled, and data sent to bStats is fully\n" +
                                "anonymous."
                ).copyDefaults(true);
                try {
                    config.save(configFile);
                } catch (IOException ignored) { }
            }

            // Load the data
            boolean enabled = config.getBoolean("enabled", true);
            String serverUUID = config.getString("serverUuid");
            boolean logErrors = config.getBoolean("logFailedRequests", false);
            boolean logSentData = config.getBoolean("logSentData", false);
            boolean logResponseStatusText = config.getBoolean("logResponseStatusText", false);

            metricsBase = new MetricsBase(
                    "bukkit",
                    serverUUID,
                    serviceId,
                    enabled,
                    this::appendPlatformData,
                    this::appendServiceData,
                    PsrScheduler::runTask,
                    plugin::isEnabled,
                    (message, error) -> this.plugin.getLogger().log(Level.WARNING, message, error),
                    (message) -> this.plugin.getLogger().log(Level.INFO, message),
                    logErrors,
                    logSentData,
                    logResponseStatusText
            );
        }

        /**
         * Shuts down the underlying scheduler service.
         */
        public void shutdown() {
            metricsBase.shutdown();
        }

        /**
         * Adds a custom chart.
         *
         * @param chart The chart to add.
         */
        public void addCustomChart(CustomChart chart) {
            metricsBase.addCustomChart(chart);
        }

        private void appendPlatformData(JsonObjectBuilder builder) {
            builder.appendField("playerAmount", getPlayerAmount());
            builder.appendField("onlineMode", Bukkit.getOnlineMode() ? 1 : 0);
            builder.appendField("bukkitVersion", Bukkit.getVersion());
            builder.appendField("bukkitName", Bukkit.getName());

            builder.appendField("javaVersion", System.getProperty("java.version"));
            builder.appendField("osName", System.getProperty("os.name"));
            builder.appendField("osArch", System.getProperty("os.arch"));
            builder.appendField("osVersion", System.getProperty("os.version"));
            builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
        }

        private void appendServiceData(JsonObjectBuilder builder) {
            builder.appendField("pluginVersion", plugin.getDescription().getVersion());
        }

        private int getPlayerAmount() {
            try {
                // Around MC 1.8 the return type was changed from an array to a collection,
                // This fixes java.lang.NoSuchMethodError: org.bukkit.Bukkit.getOnlinePlayers()Ljava/util/Collection;
                Method onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
                return onlinePlayersMethod.getReturnType().equals(Collection.class)
                        ? ((Collection<?>) onlinePlayersMethod.invoke(Bukkit.getServer())).size()
                        : ((Player[]) onlinePlayersMethod.invoke(Bukkit.getServer())).length;
            } catch (Exception e) {
                return Bukkit.getOnlinePlayers().size(); // Just use the new method if the reflection failed
            }
        }

    }

}
