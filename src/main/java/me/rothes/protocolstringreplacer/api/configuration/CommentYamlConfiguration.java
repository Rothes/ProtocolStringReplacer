package me.rothes.protocolstringreplacer.api.configuration;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentYamlConfiguration extends YamlConfiguration {

    protected static Pattern commentKeyPattern = Pattern.compile("([0-9]+)㩵遌㚳这是注释([是否])");
    protected static Pattern commentPattern = Pattern.compile("^( *)([0-9]+)㩵遌㚳这是注释([是否]): '([0-9]+)\\| ");

    protected final static Pattern startedSpacePattern = Pattern.compile("^( +)");
    protected final static Pattern endedSpacePattern = Pattern.compile("( +)$");

    private static class Comment {
        private int passedLines;
        private String commentString;
        private boolean plainComment;

        private Comment(String commentString, boolean isplainComment) {
            passedLines = -1;
            this.commentString = commentString;
            this.plainComment = isplainComment;
        }
    }

    public static Pattern getCommentKeyPattern() {
        return commentKeyPattern;
    }

    public CommentYamlConfiguration() {
        super();
        options().copyHeader(false);
        try {
            Field field;
            try {
                // Legacy support
                field = YamlConfiguration.class.getDeclaredField("yamlOptions");
            } catch (NoSuchFieldException e) {
                field = YamlConfiguration.class.getDeclaredField("yamlDumperOptions");
            }
            field.setAccessible(true);
            DumperOptions options = (DumperOptions) field.get(this);
            field.setAccessible(false);
            options.setWidth(10240);
            options.setSplitLines(false); // throws NoSuchMethodError on Legacy
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodError ignored) {

        }
    }

    @Override
    public void loadFromString(@Nonnull String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null");

        String[] lines = contents.split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        short commentIndex = 0;
        LinkedList<Comment> commentsToAdd = new LinkedList<>();
        for (String line : lines) {
            String startedSpace = getStartedSpace(line);
            int cursor = startedSpace.length();
            // Initialize with an impossible value.
            char quoteChar = '\n';
            boolean isPlainComment = true;
            boolean isInQuote = false;
            boolean isKey = false;
            boolean isList = false;
            boolean foundComment = false;
            if (cursor == line.length()) {
                line = line + "#";
            } else if (line.charAt(cursor) == '-') {
                isList = true;
            }
            while (cursor < line.length()) {
                char charAtCursor = line.charAt(cursor);
                if (isInQuote) {
                    if (charAtCursor == quoteChar) {
                        if (line.length() > cursor + 1 && line.charAt(cursor + 1) == charAtCursor) {
                            cursor++;
                        } else {
                            isInQuote = false;
                        }
                    }
                } else {
                    if (isPlainComment && " #".indexOf(charAtCursor) == -1) {
                        isPlainComment = false;
                    }
                    if (charAtCursor == '\'' || charAtCursor == '\"') {
                        quoteChar = charAtCursor;
                        isInQuote = true;
                    } else if (charAtCursor == ':') {
                        isKey = true;
                    } else if (charAtCursor == '#' && (cursor == 0 || " '\":".indexOf(line.charAt(cursor - 1)) != -1)) {
                        foundComment = true;
                        break;
                    }
                }
                cursor++;
            }
            // Convenient to edit comments in the configurations.
            if (isKey && !isList) {
                commentIndex = addComments(getStartedSpace(line), commentsToAdd, stringBuilder, commentIndex);
            }
            if (foundComment) {
                commentsToAdd.add(0, new Comment(getEndedSpace(line.substring(0, cursor)) + line.substring(cursor).replace("'", "''"),
                        isPlainComment));
            }

            stringBuilder.append(line).append("\n");
            for (Comment comment : commentsToAdd) {
                comment.passedLines++;
            }

        }
        if (!commentsToAdd.isEmpty()) {
            addComments("", commentsToAdd, stringBuilder, commentIndex);
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
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
            Matcher matcher = commentPattern.matcher(line);
            if (matcher.find()) {
                short passedLines = Short.parseShort(matcher.group(4));
                int index = stringBuilder.length() - 1;
                for (int i = 0; i < passedLines; i++) {
                    index = stringBuilder.lastIndexOf("\n", --index);
                }
                if (index == -1) {
                    index = 0;
                    stringBuilder.insert(index, '\n');
                }
                stringBuilder.insert(index, StringUtils.replace(line.substring(matcher.group(0).length(), line.length() - 1), "''", "'"));
                if (index != 0 && matcher.group(3).equals("是")) {
                    stringBuilder.insert(index, '\n');
                }
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

    @Nonnull
    public static CommentYamlConfiguration loadConfiguration(@Nonnull Reader reader) {
        Validate.notNull(reader, "Stream cannot be null");

        CommentYamlConfiguration config = new CommentYamlConfiguration();

        try {
            config.load(reader);
        } catch (IOException | InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", ex);
        }

        return config;
    }

    /**
     * Override.
     * Disable Header for legacy servers.
     */
    @NotNull
    protected String parseHeader(@NotNull String input) {
        return "";
    }

    protected static String getStartedSpace(@Nonnull String string) {
        Validate.notNull(string, "String cannot be null");

        Matcher matcher = startedSpacePattern.matcher(string);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    protected static String getEndedSpace(@NotNull String string) {
        Validate.notNull(string, "String cannot be null");

        Matcher matcher = endedSpacePattern.matcher(string);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    protected static short addComments(@NotNull String space,@NotNull List<Comment> commentsToAdd,
                                      @NotNull StringBuilder stringBuilder, short commentIndex) {
        short resultIndex = commentIndex;
        for (Comment comment : commentsToAdd) {
            stringBuilder.append(space).append(resultIndex++).append("㩵遌㚳这是注释");
            if (comment.plainComment) {
                stringBuilder.append("是");
            } else {
                stringBuilder.append("否");
            }
            stringBuilder.append(": '").append(comment.passedLines).append("| ").append(comment.commentString).append("'\n");
        }
        commentsToAdd.clear();
        return resultIndex;
    }

}
