// wssb

package me.Rothes.ProtocolStringReplacer.Replacer;

import me.Rothes.ProtocolStringReplacer.API.ChatColors;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.replacer.Replacer;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class PAPIReplacer implements Replacer {

    private final char head = ProtocolStringReplacer.getInstance().getConfig().getString("Options.Feature.Placeholder.Placeholder-Head", "｛").charAt(0);
    private final char tail = ProtocolStringReplacer.getInstance().getConfig().getString("Options.Feature.Placeholder.Placeholder-Tail", "｝").charAt(0);


    @NotNull
    @Override
    public String apply(@NotNull final String text, @Nullable final OfflinePlayer player,
                        @NotNull final Function<String, @Nullable PlaceholderExpansion> lookup) {
        final char[] chars = text.toCharArray();
        final StringBuilder builder = new StringBuilder(text.length());

        final StringBuilder identifier = new StringBuilder();
        final StringBuilder parameters = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            final char l = chars[i];

            if (l == '&' && ++i < chars.length) {
                final char c = Character.toLowerCase(chars[i]);

                if (c != '0' && c != '1' && c != '2' && c != '3' && c != '4' && c != '5' && c != '6'
                        && c != '7' && c != '8' && c != '9' && c != 'a' && c != 'b' && c != 'c' && c != 'd'
                        && c != 'e' && c != 'f' && c != 'k' && c != 'l' && c != 'm' && c != 'n' && c != 'o' && c != 'r'
                        && c != 'x') {
                    builder.append(l).append(chars[i]);
                } else {
                    builder.append(ChatColor.COLOR_CHAR);

                    if (c != 'x') {
                        builder.append(chars[i]);
                        continue;
                    }

                    if ((i > 1 && chars[i - 2] == '\\') /*allow escaping &x*/) {
                        builder.setLength(builder.length() - 2);
                        builder.append('&').append(chars[i]);
                        continue;
                    }

                    builder.append(c);

                    int j = 0;
                    while (++j <= 6) {
                        if (i + j >= chars.length) {
                            break;
                        }

                        final char x = chars[i + j];
                        builder.append(ChatColor.COLOR_CHAR).append(x);
                    }

                    if (j == 7) {
                        i += 6;
                    } else {
                        builder.setLength(builder.length() - (j * 2)); // undo &x parsing
                    }
                }
                continue;
            }

            if (l != head || i + 1 >= chars.length) {
                builder.append(l);
                continue;
            }

            boolean identified = false;
            boolean oopsitsbad = true;
            boolean hadSpace = false;

            while (++i < chars.length) {
                final char p = chars[i];

                if (p == ' ' && !identified) {
                    hadSpace = true;
                    break;
                }
                if (p == tail) {
                    oopsitsbad = false;
                    break;
                }

                if (p == '_' && !identified) {
                    identified = true;
                    continue;
                }

                if (identified) {
                    parameters.append(p);
                } else {
                    identifier.append(p);
                }
            }

            final String identifierString = identifier.toString().toLowerCase();
            final String parametersString = parameters.toString();

            identifier.setLength(0);
            parameters.setLength(0);

            if (oopsitsbad) {
                builder.append(head).append(identifierString);

                if (identified) {
                    builder.append('_').append(parametersString);
                }

                if (hadSpace) {
                    builder.append(' ');
                }
                continue;
            }

            final PlaceholderExpansion placeholder = lookup.apply(identifierString);
            if (placeholder == null) {
                builder.append(head).append(identifierString);

                if (identified) {
                    builder.append('_');
                }

                builder.append(parametersString).append(tail);
                continue;
            }

            final String replacement = placeholder.onRequest(player, parametersString);
            if (replacement == null) {
                builder.append(head).append(identifierString);

                if (identified) {
                    builder.append('_');
                }

                builder.append(parametersString).append(tail);
                continue;
            }

            builder.append(ChatColors.getColored(replacement));
        }

        return builder.toString();
    }

}
