package me.rothes.protocolstringreplacer;

import me.rothes.protocolstringreplacer.api.ChatColors;
import me.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import me.rothes.protocolstringreplacer.api.exceptions.MissingInitialResourceException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Pattern;

public class PSRLocalization {

    private static ProtocolStringReplacer plugin = null;
    private static String systemLocale = null;
    private static String locale = null;
    private static HashMap<String, String> localedMessages = null;

    public static void initialize(@Nonnull ProtocolStringReplacer plugin) {
        PSRLocalization.plugin = plugin;

        systemLocale = System.getProperty("user.language", "en");
        systemLocale += '-';
        systemLocale += System.getProperty("user.country", "US");

        locale = plugin.getConfig().getString("Options.Localization");
        if (locale == null) {
            locale = systemLocale;
        }

        localedMessages = new HashMap<>();
        loadLocale();
    }

    @Nonnull
    public static String getLocaledMessage(@Nonnull String key, @Nonnull String... replacements) {
        String result = localedMessages.getOrDefault(key, "§cMissing localization key: " + key);
        byte length = (byte) replacements.length;
        if (length > 0) {
            for (byte i = 0; i < length; i++) {
                result = StringUtils.replace(result, "%" + i + '%', replacements[i]);
            }
        }
        return result;
    }

    @Nonnull
    public static String getPrefixedLocaledMessage(@Nonnull String key, @Nonnull String... replacements) {
        return getLocaledMessage("Sender.Prefix") + getLocaledMessage(key, replacements);
    }

    @Nonnull
    public static CommentYamlConfiguration getDefaultLocaledConfig() {
        InputStream resource = getLocaledResource("/Configs/Config.yml");
        return CommentYamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8));
    }

    @Nonnull
    public static CommentYamlConfiguration getDefaultLocale() {
        InputStream resource = getLocaledResource("/Locales/Locale.yml");
        return CommentYamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8));
    }

    public static CommentYamlConfiguration getDefaultLocaledExample() {
        InputStream resource = getLocaledResource("/Replacers/Example.yml");
        return CommentYamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8));
    }

    private static void loadLocale() {
        File localeFile = new File(plugin.getDataFolder() + "/Locale/" + locale + ".yml");
        CommentYamlConfiguration locale;
        if (localeFile.exists()) {
            checkLocaleKeys(localeFile);
            locale = CommentYamlConfiguration.loadConfiguration(localeFile);
        } else {
            locale = getDefaultLocale();
            try {
                localeFile.getParentFile().mkdirs();
                localeFile.createNewFile();
                locale.save(localeFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Pattern commentKeyPattern = CommentYamlConfiguration.getCommentKeyPattern();
        for (String key : locale.getKeys(true)) {
            if (commentKeyPattern.matcher(key).find()) {
                continue;
            }
            localedMessages.put(key, ChatColors.getColored(locale.getString(key)));
        }
    }

    private static void checkLocaleKeys(@NotNull File localeFile) {
        CommentYamlConfiguration defaultLocale = getDefaultLocale();
        CommentYamlConfiguration locale = CommentYamlConfiguration.loadConfiguration(localeFile);
        boolean checked = false;
        Pattern commentKeyPattern = CommentYamlConfiguration.getCommentKeyPattern();
        for (String key : defaultLocale.getKeys(true)) {
            if (!commentKeyPattern.matcher(key).find() && !locale.contains(key)) {
                locale.set(key, defaultLocale.get(key));
                checked = true;
            }
        }
        if (checked) {
            try {
                locale.save(localeFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static InputStream getLocaledResource(String file) {
        InputStream resource = plugin.getResource("Languages/" + locale + file);
        if (resource == null) {
            resource = plugin.getResource("Languages/" + systemLocale + file);
            if (resource == null) {
                resource = plugin.getResource("Languages/" + "en-US" + file);
                if (resource == null) {
                    throw new MissingInitialResourceException("Languages/" + "en-US" + file);
                }
            }
        }
        return resource;
    }

}
