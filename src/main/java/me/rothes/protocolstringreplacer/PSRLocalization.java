package me.rothes.protocolstringreplacer;

import me.rothes.protocolstringreplacer.utils.ColorUtils;
import me.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import me.rothes.protocolstringreplacer.api.exceptions.MissingInitialResourceException;
import me.rothes.protocolstringreplacer.utils.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

public class PSRLocalization {

    private static ProtocolStringReplacer plugin = null;
    private static String systemLocale = null;
    private static String locale = null;
    private static HashMap<String, String> localedMessages = null;
    private static String[] replaceHolders = new String[10];

    public static void initialize(@Nonnull ProtocolStringReplacer plugin) {
        PSRLocalization.plugin = plugin;

        systemLocale = System.getProperty("user.language", Locale.getDefault().getLanguage());
        systemLocale += '-';
        systemLocale += System.getProperty("user.country", Locale.getDefault().getCountry());

        locale = plugin.getConfig().getString("Options.Localization", systemLocale);

        localedMessages = new HashMap<>();
        loadLocale();

        for (byte i = 0; i < 10; i ++) {
            replaceHolders[i] = "%" + i + '%';
        }

    }

    @NotNull
    public static String getLocaledMessage(@Nonnull String key, @Nonnull String... replacements) {
        Validate.notNull(key, "Key cannot be null");
        Validate.notNull(replacements, "Replacements Array cannot be null");

        String result = localedMessages.getOrDefault(key, "§cMissing localization key: " + key);
        byte length = (byte) replacements.length;
        return length > 0 ? StringUtils.replaceEach(result, Arrays.copyOf(replaceHolders, length), replacements) : result;
    }

    @NotNull
    public static String getPrefixedLocaledMessage(@Nonnull String key, @Nonnull String... replacements) {
        Validate.notNull(key, "Key cannot be null");
        Validate.notNull(replacements, "Replacements Array cannot be null");

        return getLocaledMessage("Sender.Prefix") + getLocaledMessage(key, replacements);
    }

    @NotNull
    public static CommentYamlConfiguration getDefaultLocaledConfig() {
        InputStream resource = getLocaledResource("/Configs/Config.yml");
        return CommentYamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8));
    }

    @NotNull
    public static CommentYamlConfiguration getDefaultLocale() {
        InputStream resource = getLocaledResource("/Locales/Locale.yml");
        return CommentYamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8));
    }

    @NotNull
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
            FileUtils.createFile(localeFile);
            try {
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
            localedMessages.put(key, ColorUtils.getColored(locale.getString(key)));
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

    @NotNull
    private static InputStream getLocaledResource(@NotNull String file) {
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
