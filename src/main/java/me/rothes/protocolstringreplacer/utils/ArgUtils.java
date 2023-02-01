package me.rothes.protocolstringreplacer.utils;

import org.apache.commons.lang.Validate;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgUtils {

    private static final Pattern lastQuotes = Pattern.compile("\"+$");

    @Nonnull
    public static String formatWithQuotes(@Nonnull String string) {
        Validate.notNull(string, "String cannot be null");

        if (string.isEmpty()) {
            return string;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append('\"');
            String[] args = string.split(" ");
            for (String arg : args) {
                stringBuilder.append(" ").append(arg);
                char[] chars = arg.toCharArray();
                for (int i1 = chars.length - 1; i1 >= 0; i1--) {
                    if (chars[i1] == '\"') {
                        stringBuilder.append('\"');
                    } else {
                        break;
                    }
                }
            }
            stringBuilder.deleteCharAt(1).append('\"');
            return stringBuilder.toString();
        }
    }

    @Nonnull
    public static String[] mergeQuotes(@Nonnull String[] strings) {
        Validate.notNull(strings, "String Arrays cannot be null");

        // -1 if no quote at start was delected.
        int startIndex = -1;
        LinkedList<String> merged = new LinkedList<>();
        for (int i = 0; i < strings.length; i++) {
            String arg = strings[i];
            int length = arg.length();
            if (startIndex == -1) {
                if (length > 0 && arg.charAt(0) == '\"') {
                    startIndex = i;
                } else {
                    merged.add(arg);
                }
            }
            if (startIndex != -1 && ((startIndex != i && length == 1 && arg.charAt(0) == '\"')
                    || (length > 1 && arg.charAt(length - 1) == '\"'))) {
                Matcher matcher = lastQuotes.matcher(strings[i]);
                //noinspection ResultOfMethodCallIgnored
                matcher.find();
                String quotes = matcher.group(0);
                if ((quotes.length() % 2) == 1) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(strings[startIndex]);
                    addArgument(stringBuilder, strings[startIndex++]);
                    while (startIndex <= i) {
                        stringBuilder.append(" ").append(strings[startIndex]);
                        addArgument(stringBuilder, strings[startIndex++]);
                    }
                    stringBuilder.deleteCharAt(0).deleteCharAt(stringBuilder.length() - 1);
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
        return merged.toArray(new String[0]);
    }

    private static void addArgument(StringBuilder stringBuilder, String arg) {
        Matcher matcher = lastQuotes.matcher(arg);

        if (matcher.find()) {
            String quotes = matcher.group(0);
            int length = stringBuilder.length();
            stringBuilder.delete(length - quotes.length() / 2, length);
        }
    }

}
