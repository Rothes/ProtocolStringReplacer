package me.Rothes.ProtocolStringReplacer.API;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArrayUtils {
    private static Pattern lastQuotes = Pattern.compile("\"+$");

    @Nonnull
    public static String[] mergeQuotes(@Nonnull String[] strings) {
        Validate.notNull(strings, "String Arrays cannot be null");

        // -1 if no quote at start was delected.
        int startIndex = -1;
        LinkedList<String> merged = new LinkedList<>();
        for (int i = 0; i < strings.length; i++) {
            String arg = strings[i];
            if (startIndex == -1) {
                if (arg.charAt(0) == '\"') {
                    startIndex = i;
                } else {
                    merged.add(arg);
                }
            }
            if (startIndex != -1
                    && ((startIndex != i && arg.length() == 1 && arg.charAt(0) == '\"')
                          || (arg.length() > 2 /* To avoid empty String */ && arg.charAt(arg.length() - 1) == '\"'))) {
                long start = System.currentTimeMillis();
                Matcher matcher = lastQuotes.matcher(strings[i]);
                //noinspection ResultOfMethodCallIgnored
                matcher.find();
                String quotes = matcher.group(0);
                if ((quotes.length() % 2) == 1) {
                    StringBuilder stringBuilder = new StringBuilder(i - startIndex);
                    stringBuilder.append(strings[startIndex++]);
                    while (startIndex <= i) {
                        stringBuilder.append(" ").append(strings[startIndex++]);
                    }
                    int length = stringBuilder.length();
                    stringBuilder.delete(length - quotes.length() / 2 - 1, length)
                            .deleteCharAt(0);
                    merged.add(stringBuilder.toString());
                    startIndex = -1;
                }
            }
        }
        if (startIndex != -1) {
            while (startIndex < strings.length) {
                merged.add(strings[startIndex++]);
            }
        }
        Bukkit.getConsoleSender().sendMessage(merged.toString());
        return merged.toArray(new String[0]);
    }

}
