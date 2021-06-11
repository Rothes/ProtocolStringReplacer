package me.Rothes.ProtocolStringReplacer.API;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatColors {

    private static String RGBRegex = "&#([0-9a-fA-F]{6})";
    private static Pattern pattern = Pattern.compile(RGBRegex);

    @Nonnull
    public static String getColored(@Nonnull String textToTranslate) {
        Validate.notNull(textToTranslate, "Text cannot be null");
        return translateRGB(ChatColor.translateAlternateColorCodes('&', textToTranslate));
    }

    @Nonnull
    public static String getColored(@Nonnull String textToTranslate, boolean translateColor, boolean translateFormat, boolean translateRGB) {
        Validate.notNull(textToTranslate, "Text cannot be null");
        char[] chars = textToTranslate.toCharArray();

        for(int i = 0; i < chars.length - 1; ++i) {
            if (chars[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoXxRr".indexOf(chars[i + 1]) > -1) {
                chars[i] = '§';
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        String translated = new String(chars);
        if (translateRGB) {
            translated = translateRGB(translated);
        }
        return translated;
    }

    @Nonnull
    private static String translateRGB(@Nonnull String string) {
        Validate.notNull(string, "String cannot be null");

        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            StringBuilder stringBuilder = new StringBuilder(matcher.group(1));
            int i = stringBuilder.length();
            while (i > 0) {
                stringBuilder.insert(--i, '§');
            }
            stringBuilder.insert(0, "§x");
            string = string.replaceFirst(matcher.group(0), stringBuilder.toString());
        }
        return string;
    }

}
