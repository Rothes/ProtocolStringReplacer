package me.rothes.protocolstringreplacer.commands.subcommands.editchildren;

import me.rothes.protocolstringreplacer.api.ArgUtils;
import me.rothes.protocolstringreplacer.api.ChatColors;
import me.rothes.protocolstringreplacer.commands.SubCommand;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplacesMode;
import me.rothes.protocolstringreplacer.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class Replace extends SubCommand {

    private static final Pattern commentValuePrefix = Pattern.compile("[0-9]+\\| ");

    public Replace() {
        super("replace", "protocolstringreplacer.command.edit", "替换项目相关指令");
    }

    @Override
    public void onExecute(@Nonnull User user, @NotNull String[] args) {
        if (user.getEditorReplacerConfig() == null) {
            user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c请先选定一个替换配置文件, 再使用本指令.");
            return;
        }

        if (args.length > 2) {
            if ("list".equalsIgnoreCase(args[2])) {
                listCommand(user, args);
                return;
            } else if ("set".equalsIgnoreCase(args[2])) {
                setCommand(user, args);
                return;

            } else if ("add".equalsIgnoreCase(args[2])) {
                addCommand(user, args);
                return;

            } else if ("remove".equalsIgnoreCase(args[2])) {
                removeCommand(user, args);
                return;
            }
        }
        sendHelp(user);
    }

    private void listCommand(@Nonnull User user, @NotNull String[] args) {
        if (args.length < 6 && args.length > 3) {
            int page = 1;
            ReplacesMode replacesMode = getReplacesMode(args[3]);
            if (replacesMode == null) {
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c替换模式 §f" + args[3] + " §c不存在.");
                return;
            }
            ListOrderedMap replaces = user.getEditorReplacerConfig().getReplaces(replacesMode);
            HashMap<Short, LinkedList<ReplacerConfig.CommentLine>> commentLinesMap = user.getEditorReplacerConfig().getCommentLines(replacesMode);
            int totalPage = (int) Math.ceil((float) replaces.size() / 5);
            if (args.length == 5) {
                if (StringUtils.isNumeric(args[4])) {
                    page = Integer.parseInt(args[4]);
                } else {
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §f" + args[4] + " §c不是一个有效的正整数!");
                    return;
                }
            }

            if (page > totalPage) {
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c请指定小于总页码 §f" + totalPage + " §c的页码.");
                return;
            }
            if (page < 1) {
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c请指定大于 §f0 §c的页码.");
                return;
            }

            user.sendFilteredText("§7§m-----------§7§l §7[ §c§lP§6§lS§3§lR §7- §e替换项目列表§7 ]§l §7§m-----------");

            for (int i = (page - 1) * 5; i < replaces.size() && i < page * 5; i++) {
                String original = replaces.get(i).toString();
                String replacement = (String) replaces.get(replaces.get(i));
                LinkedList<ReplacerConfig.CommentLine> commentLines = commentLinesMap.get((short) i);
                ComponentBuilder hoverBuilder = new ComponentBuilder("§6§l注释内容:").color(ChatColor.GREEN);
                if (commentLines != null && !commentLines.isEmpty()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (ReplacerConfig.CommentLine commentLine : commentLines) {
                        stringBuilder.insert(0, commentValuePrefix.matcher(commentLine.getValue()).replaceFirst("")).insert(0, '\n');
                    }
                    hoverBuilder.append(stringBuilder.toString());
                } else {
                    hoverBuilder.append("\n§7无");
                }

                user.sendFilteredMessage(new ComponentBuilder("§6[+] ").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/psr edit replace add " + replacesMode.getNode() + " " + i + " <原文本> <新文本>")).
                        event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create())).
                        append("§6[编辑]").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/psr edit replace set " + replacesMode.getNode() + " " + i + " " + ArgUtils.formatWithQuotes(ChatColors.restoreColored(original)) + " " + ArgUtils.formatWithQuotes(ChatColors.restoreColored(replacement)))).append(" " + i + ". ").
                        reset().event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create())).
                        append(ChatColors.showColorCodes(original)).color(ChatColor.AQUA).create());
                user.sendFilteredMessage(new ComponentBuilder("§c[删除]").event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/psr edit replace remove " + replacesMode.getNode() + " " + i)).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create())).
                        append(" §7§o==> ").reset().event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create())).append(ChatColors.showColorCodes(replacement)).color(ChatColor.BLUE).create());
            }

            ComponentBuilder pageComponent = new ComponentBuilder("");
            if (page > 1) {
                pageComponent.append(" ◀ ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/psr edit replace list " + replacesMode.getNode() + " " + (page - 1))).color(ChatColor.YELLOW);
            } else {
                pageComponent.append("   ");
            }
            pageComponent.append("第").reset().color(ChatColor.DARK_AQUA).append(" " + page + " ").color(ChatColor.WHITE).append("页").color(ChatColor.DARK_AQUA).append(" | ").color(ChatColor.GRAY).append("共").
                    color(ChatColor.DARK_AQUA).append(" " + totalPage + " ").color(ChatColor.WHITE).append("页").color(ChatColor.DARK_AQUA);
            if (page < totalPage) {
                pageComponent.append(" ▶ ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/psr edit replace list " + replacesMode.getNode() + " " + (page + 1))).color(ChatColor.YELLOW);
            }
            user.sendFilteredMessage(pageComponent.create());

            user.sendFilteredText("§7§m-----------------------------------------------");
        } else {
            user.sendFilteredText("§7 * §e/psr edit file list <替换模式> [页码] §7- §b查看所有加载的替换配置文件");
        }
    }

    private void setCommand(@Nonnull User user, @NotNull String[] args) {
        if (args.length > 4) {
            ReplacesMode replacesMode = getReplacesMode(args[3]);
            if (replacesMode == null) {
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c替换模式 §f" + args[3] + " §c不存在.");
                return;
            }
            if (!StringUtils.isNumeric(args[4])) {
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §f" + args[4] + " §c不是一个有效的正整数!");
                return;
            }
            int index = Integer.parseInt(args[4]);
            ReplacerConfig editorReplacerConfig = user.getEditorReplacerConfig();
            if (index < 0) {
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c请指定大于或等于 §f0 §c的索引.");
                return;
            }

            if (args.length == 6) {
                if (index >= editorReplacerConfig.getReplaces(replacesMode).size()) {
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c请指定小于总大小 §f" + editorReplacerConfig.getReplaces(replacesMode).size() + " §c的索引.");
                    return;
                }

                String replacement = ChatColors.getColored(args[5]);
                editorReplacerConfig.setReplace(index, replacement, replacesMode);
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §a已修改索引 §f" + args[4] + " §a为 §f" + editorReplacerConfig.getReplaces(replacesMode).get(index) + " §7§o> §f" + replacement);

            } else if (args.length == 7) {
                if (index > editorReplacerConfig.getReplaces(replacesMode).size()) {
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c请指定小于或等于总大小 §f" + editorReplacerConfig.getReplaces(replacesMode).size() + " §c的索引.");
                    return;
                }
                String original = ChatColors.getColored(args[5]);
                int i = editorReplacerConfig.checkReplaceKey(original, replacesMode);
                if (i == -1 || i == index) {
                    String replacement = ChatColors.getColored(args[6]);
                    editorReplacerConfig.setReplace(index, original, replacement, replacesMode);
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §a已修改索引 §f" + args[4] + " §a为 §f" + original + " §7§o> §f" + replacement);
                } else {
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c在索引 §f" + i + " §c处已有一个相同的原文本了.");
                }
            } else {
                user.sendFilteredText("§7 * §e/psr edit replace set <替换模式> <索引> [原文本] <新文本> §7- §b编辑指定索引的替换项");
            }
        } else {
            user.sendFilteredText("§7 * §e/psr edit replace set <替换模式> <索引> [原文本] <新文本> §7- §b编辑指定索引的替换项");
        }
    }

    private void addCommand(@Nonnull User user, @NotNull String[] args) {
        if (args.length > 3) {
            ReplacesMode replacesMode = getReplacesMode(args[3]);
            if (replacesMode == null) {
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c替换模式 §f" + args[3] + " §c不存在.");
                return;
            }
            if (args.length == 6) {
                String original = ChatColors.getColored(args[4]);
                ReplacerConfig editorReplacerConfig = user.getEditorReplacerConfig();
                int i = editorReplacerConfig.checkReplaceKey(original, replacesMode);
                if (i == -1) {
                    String replacement = ChatColors.getColored(args[5]);
                    editorReplacerConfig.addReplace(original, replacement, replacesMode);
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §a已在索引 §f" + editorReplacerConfig.getReplaces(replacesMode).size() + " §a添加替换项: §f" + original + " §7§o> §f" + replacement);
                } else {
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c在索引 §f" + i + " §c处已有一个相同的原文本了.");
                }
            } else if (args.length == 7) {
                if (!StringUtils.isNumeric(args[4])) {
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §f" + args[4] + " §c不是一个有效的正整数!");
                    return;
                }
                int index = Integer.parseInt(args[4]);
                if (index < 0) {
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c请指定大于或等于 §f0 §c的索引.");
                    return;
                }
                String original = ChatColors.getColored(args[5]);
                ReplacerConfig editorReplacerConfig = user.getEditorReplacerConfig();
                int i = editorReplacerConfig.checkReplaceKey(original, replacesMode);
                if (i == -1) {
                    String replacement = ChatColors.getColored(args[6]);
                    user.getEditorReplacerConfig().addReplace(index, original, replacement, replacesMode);
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §a已在索引 §f" + index + " §a添加替换项: §f" + original + " §7§o> §f" + replacement);
                } else {
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c在索引 §f" + i + " §c处已有一个相同的原文本了.");
                }
            }
        } else {
            user.sendFilteredText("§7 * §e/psr edit replace add <替换模式> [索引] <原文本> <新文本> §7- §b在索引添加一个替换项");
        }
    }

    private void removeCommand(@Nonnull User user, @NotNull String[] args) {
        if (args.length > 3) {
            ReplacesMode replacesMode = getReplacesMode(args[3]);
            if (replacesMode == null) {
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c替换模式 §f" + args[3] + " §c不存在.");
                return;
            }
            if (args.length == 5) {
                if (!StringUtils.isNumeric(args[4])) {
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §f" + args[4] + " §c不是一个有效的整数!");
                    return;
                }
                int index = Integer.parseInt(args[4]);
                if (index < 0) {
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c请指定大于或等于 §f0 §c的索引.");
                    return;
                }
                ReplacerConfig editorReplacerConfig = user.getEditorReplacerConfig();
                if (index > editorReplacerConfig.getReplaces(replacesMode).size()) {
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c请指定小于或等于总大小 §f" + editorReplacerConfig.getReplaces(replacesMode).size() + " §c的索引.");
                    return;
                }
                editorReplacerConfig.removeReplace(index, replacesMode);
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §a已删除索引 §f" + index + " §a的替换项.");
            }
        } else {
            user.sendFilteredText("§7 * §e/psr edit replace remove <替换模式> <索引> §7- §b删除在索引的替换项");
        }
    }

    @Override
    public List<String> onTab(@NotNull User user, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 3) {
            list = Arrays.asList("help", "list", "set", "add", "remove");
        } else if (args.length == 4
                && (args[2].equalsIgnoreCase("list") || args[2].equalsIgnoreCase("set")
                || args[2].equalsIgnoreCase("add") || args[2].equalsIgnoreCase("remove"))) {
            list.add("<替换模式>");
            for (ReplacesMode replacesMode : ReplacesMode.values()) {
                list.add(replacesMode.getNode());
            }
        } else if (args.length == 5) {
            if (args[2].equalsIgnoreCase("list")) {
                list.add("[页码]");
            } else if (args[2].equalsIgnoreCase("set")) {
                list.add("<索引>");
            } else if (args[2].equalsIgnoreCase("add")) {
                list.add("[索引]|<原文本>");
            } else if (args[2].equalsIgnoreCase("remove")) {
                list.add("<索引>");
            }
        } else if (args.length == 6) {
            if (args[2].equalsIgnoreCase("set")) {
                list.add("<原文本>");
            } else if (args[2].equalsIgnoreCase("add")) {
                if (StringUtils.isNumeric(args[3])) {
                    list.add("<原文本>");
                } else {
                    list.add("<新文本>");
                }
            }
        } else if (args.length == 7) {
            if (args[2].equalsIgnoreCase("set")) {
                list.add("<新文本>");
            } else if (args[2].equalsIgnoreCase("add") && StringUtils.isNumeric(args[3])) {
                list.add("<新文本>");
            }
        }
        return list;
    }

    @Override
    public void sendHelp(@Nonnull User user) {
        user.sendFilteredText("§7§m-----------§7§l §7[ §c§lP§6§lS§3§lR §7- §e替换项编辑器§7 ]§l §7§m-----------");
        user.sendFilteredText("§7 * §e/psr edit replace help §7- §b替换项编辑器指令列表");
        user.sendFilteredText("§7 * §e/psr edit replace list §7- §b查看所有替换项");
        user.sendFilteredText("§7 * §e/psr edit replace set §7- §b编辑指定索引的替换项");
        user.sendFilteredText("§7 * §e/psr edit replace add §7- §b在索引添加一个替换项");
        user.sendFilteredText("§7 * §e/psr edit replace remove §7- §b删除在索引的替换项");
        user.sendFilteredText("§7§m-----------------------------------------------");
    }

    private ReplacesMode getReplacesMode(@NotNull String string) {
        for (ReplacesMode type : ReplacesMode.values()) {
            if (type.getNode().equalsIgnoreCase(string)) {
                return type;
            }
        }
        return null;
    }

}
