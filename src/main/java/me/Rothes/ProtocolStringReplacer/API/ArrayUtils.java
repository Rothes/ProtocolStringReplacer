package me.Rothes.ProtocolStringReplacer.API;

import org.apache.commons.lang.Validate;

import javax.annotation.Nonnull;
import java.util.List;

public class ArrayUtils {

    @Nonnull
    public static String[] mergeQuotes(@Nonnull String[] strings) {
        Validate.notNull(strings, "String Arrays cannot be null");

        // -1 if no quote at start was delected.
        int startIndex = -1;
        List<String> merged = null;
        for (int i = 0; i < strings.length; i++) {
            String arg = strings[i];
            if (arg.startsWith("\"")) {
                startIndex = i;
            }
            if (startIndex == -1) {
                merged.add(arg);
            } else if (arg.endsWith("\"")) {
                StringBuilder stringBuilder = null;
                arg = strings[startIndex++].substring(1);
                stringBuilder.append(arg).append(" ");
                for (; startIndex < i; arg = strings[++startIndex]) {
                    stringBuilder.append(arg).append(" ");
                }
                stringBuilder.append(arg, 0, arg.length() - 1);
                merged.add(stringBuilder.toString());
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
