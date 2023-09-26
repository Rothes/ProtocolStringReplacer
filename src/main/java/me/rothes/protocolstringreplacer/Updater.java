package me.rothes.protocolstringreplacer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplaceMode;
import me.rothes.protocolstringreplacer.scheduler.PsrScheduler;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Updater implements Listener {

    private static final String VERSION_CHANNEL = "Stable";
    private static final int VERSION_NUMBER = 115;
    private final HashMap<String, Integer> msgTimesMap = new HashMap<>();
    private final List<String> messages = new ArrayList<>();
    private final ProtocolStringReplacer plugin;

    Updater(ProtocolStringReplacer plugin) {
        this.plugin = plugin;
    }

    public void start() {
        initMetrics();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        PsrScheduler.runTaskTimerAsynchronously(() -> {
            try {
                String json = getJson();
                if (json == null) {
                    return;
                }
                checkJson(json);
            } catch (IllegalStateException | NullPointerException e) {
                ProtocolStringReplacer.error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Updater.Error-Parsing-Json", e.toString()));
                e.printStackTrace();
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
        Metrics metrics = new Metrics(plugin, 11740);
        metrics.addCustomChart(new DrilldownPie("Replaces_Count", () -> {
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
        metrics.addCustomChart(new DrilldownPie("Blocks_Count", () -> {
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

    private String getJson() {
        try (
                InputStream stream = new URL("https://" + plugin.getConfigManager().gitRawHost + "/Rothes/ProtocolStringReplacer/master/Version%20Infos.json")
                        .openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
        ){
            StringBuilder jsonBuilder = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                jsonBuilder.append(line).append("\n");
            }
            return jsonBuilder.toString();
        } catch (IOException ignored) {
            // error(PsrLocalization.getLocaledMessage("Console-Sender.Messages.Updater.Error-Checking-Version", e.toString()));
        }
        return null;
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
        String msg;
        if (messageJson.has(PsrLocalization.getLocale())) {
            msg = messageJson.get(PsrLocalization.getLocale()).getAsString();
        } else {
            msg = messageJson.get("en-US").getAsString();
        }
        return msg;
    }


}
