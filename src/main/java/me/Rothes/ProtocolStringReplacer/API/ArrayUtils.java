package me.Rothes.ProtocolStringReplacer.API;

import org.apache.commons.lang.Validate;

import javax.annotation.Nonnull;
import java.util.LinkedList;

public class ArrayUtils {

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
                StringBuilder stringBuilder = new StringBuilder(i - startIndex);
                stringBuilder.append(strings[startIndex++]);
                while (startIndex <= i) {
                    stringBuilder.append(" ").append(strings[startIndex++]);
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1).deleteCharAt(0);
                merged.add(stringBuilder.toString());
                startIndex = -1;
            }
        }
        if (startIndex != -1) {
            while (startIndex < strings.length) {
                merged.add(strings[startIndex++]);
            }
        }
        return merged.toArray(new String[0]);
    }

}
