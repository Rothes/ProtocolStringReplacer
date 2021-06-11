package me.Rothes.ProtocolStringReplacer.API.Configuration;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentYamlConfiguration extends YamlConfiguration {

    protected String commentPrefix = "'䳗䣞䑪这是注释': '";
    protected String commentSubfix = "'";

    protected String originalRegex = "^( *)'䳗䣞䑪这是注释': '";
    protected Pattern originalPattern = Pattern.compile(originalRegex);

    protected Pattern startedSpacePattern = Pattern.compile("^( *)");
    protected Pattern endedSpacePattern = Pattern.compile("( *)$");

    @Override
    public void loadFromString(@Nonnull String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null");

        String[] lines = contents.split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            stringBuilder.append(line).append("\n");
            if (line.contains("#")) {
                String startedSpace = getStartedSpace(line);
                int cursor = startedSpace.length();
                // Initialize with an impossible value.
                char quoteChar = '\n';
                boolean isInQuote = false;
                boolean commentFound = false;
                boolean isPath = false;
                char[] chars = line.toCharArray();
                for (; cursor < line.length(); cursor++) {
                    char charAtCursor = chars[cursor];
                    if (isPath && charAtCursor != ' ') {
                        isPath = false;
                    }
                    if (isInQuote) {
                        if (charAtCursor == quoteChar) {
                            if (chars[cursor + 1] == charAtCursor) {
                                cursor++;
                            } else {
                                isInQuote = false;
                            }
                        }
                    } else {
                        if (charAtCursor == '\'' || charAtCursor == '\"') {
                            quoteChar = charAtCursor;
                            isInQuote = true;
                        } else if (charAtCursor == ':') {
                            isPath = true;
                        } else if (charAtCursor == '#') {
                            commentFound = true;
                            break;
                        }
                    }
                }
                if (commentFound) {
                    if (isPath) {
                        startedSpace = getStartedSpace(lines[i + 1]);
                    }
                    // The comment behind the settings will be removed when saved, so we only need to do this.
                    stringBuilder.append(startedSpace).append(commentPrefix).append(getEndedSpace(line.substring(0, cursor))).append(line.substring(cursor).replace("'", "''")).append(commentSubfix).append("\n");
                }
            }
        }
        super.loadFromString(stringBuilder.toString());
    }

    @Override
    @Nonnull
    public String saveToString() {
        String contents = super.saveToString();
        String[] lines = contents.split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : lines) {
            Matcher matcher = originalPattern.matcher(line);
            if (matcher.find()) {
                stringBuilder.append(line.replaceFirst(matcher.group(0), matcher.group(1) + "#").replace("''", "'"))
                        .deleteCharAt(stringBuilder.length() - 1).append("\n");
            } else {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    @Nonnull
    public static CommentYamlConfiguration loadConfiguration(@Nonnull File file) {
        Validate.notNull(file, "File cannot be null");
        CommentYamlConfiguration config = new CommentYamlConfiguration();

        try {
            config.load(file);
        } catch (FileNotFoundException ignored) {
        } catch (IOException | InvalidConfigurationException var4) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, var4);
        }

        return config;
    }

    protected boolean isCorrectlyQuoted(@Nonnull String string) {
        Validate.notNull(string, "String cannot be null");

        string = string.trim();
        char quoteChar = string.charAt(0);
        if (quoteChar == '\'' || quoteChar == '\"') {
            int count = 0;
            for (char Char : string.toCharArray()) {
                if (Char == quoteChar) {
                    count++;
                }
            }
            return ((count % 2) == 0);
        } else {
            return true;
        }
    }

    protected String getStartedSpace(@Nonnull String string) {
        Validate.notNull(string, "String cannot be null");

        Matcher matcher = startedSpacePattern.matcher(string);
        matcher.find();
        return matcher.group(1);
    }

    protected String getEndedSpace(@Nonnull String string) {
        Validate.notNull(string, "String cannot be null");

        Matcher matcher = endedSpacePattern.matcher(string);
        matcher.find();
        return matcher.group(1);
    }

}
