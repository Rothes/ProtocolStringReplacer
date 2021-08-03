package me.rothes.protocolstringreplacer.commands.subcommands.editchildren;

import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import me.rothes.protocolstringreplacer.user.User;
import me.rothes.protocolstringreplacer.api.ArgUtils;
import me.rothes.protocolstringreplacer.api.configuration.DotYamlConfiguration;
import me.rothes.protocolstringreplacer.commands.SubCommand;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class File extends SubCommand {

    private static Pattern fileNamePrefix = Pattern.compile("(\")?Replacers/");

    public File() {
        super("file", "protocolstringreplacer.command.edit", "替换配置文件相关指令");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onExecute(@Nonnull User user, @NotNull String[] args) {
        if (args.length > 2) {
            if ("list".equalsIgnoreCase(args[2])) {
                if (args.length < 5) {
                    int page = 1;
                    LinkedList<ReplacerConfig> replacerConfigList = ProtocolStringReplacer.getInstance().getReplacerManager().getReplacerConfigList();
                    int totalPage = (int) Math.ceil((float) replacerConfigList.size() / 10);
                    if (args.length == 4) {
                        if (StringUtils.isNumeric(args[3])) {
                            page = Integer.parseInt(args[3]);
                        } else {
                            user.sendFilteredText("§c§lP§6§lS§3§lR §e> §f" + args[3] + " §c不是一个有效的正整数!");
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

                    user.sendFilteredText("§7§m-----------§7§l §7[ §c§lP§6§lS§3§lR §7- §e替换文件列表§7 ]§l §7§m-----------");

                    for (int i = (page - 1) * 10; i < replacerConfigList.size() && i < page * 10; i++) {
                        ReplacerConfig replacerConfig = replacerConfigList.get(i);
                        ComponentBuilder hoverBuilder = new ComponentBuilder("").append(replacerConfig.getRelativePath()).color(ChatColor.GOLD).bold(true).append("\n§3§l状态: ").bold(false).append(replacerConfig.isEnable()? "§a启用" : "§c禁用").
                                append("\n§3§l优先级: ").append(String.valueOf(replacerConfig.getPriority())).color(ChatColor.AQUA).append("\n§3§l版本: ").append(replacerConfig.getVersion() == null? "§7未定义" : replacerConfig.getVersion()).
                                append("\n§3§l作者: ").append(replacerConfig.getAuthor() == null? "§7未定义" : replacerConfig.getAuthor()).append("\n§3§l匹配方式: §b");
                        switch (replacerConfig.getMatchType()) {
                            case CONTAIN:
                                hoverBuilder.append("包含匹配");
                                break;
                            case EQUAL:
                                hoverBuilder.append("完全匹配");
                                break;
                            case REGEX:
                                hoverBuilder.append("正则表达式");
                                break;
                            default:
                                hoverBuilder.append("未知");
                        }
                        hoverBuilder.append("\n§3§l监听类型: ");
                        for (ListenType listenType : replacerConfig.getListenTypeList()) {
                            hoverBuilder.append("\n§7- ").append(listenType.getName()).color(ChatColor.AQUA);
                        }

                        user.sendFilteredMessage(new ComponentBuilder("[选定]").color(ChatColor.GOLD).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/psr edit file select " + i)).append(" " + i + ". ").reset().color(ChatColor.WHITE).
                                append(replacerConfig.getRelativePath()).color(ChatColor.AQUA).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create())).create());
                    }

                    ComponentBuilder pageComponent = new ComponentBuilder("");
                    if (page > 1) {
                        pageComponent.append(" ◀ ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/psr edit file list " + (page - 1))).color(ChatColor.YELLOW);
                    } else {
                        pageComponent.append("   ");
                    }
                    pageComponent.append("第").reset().color(ChatColor.DARK_AQUA).append(" " + page + " ").color(ChatColor.WHITE).append("页").color(ChatColor.DARK_AQUA).append(" | ").color(ChatColor.GRAY).append("共").
                            color(ChatColor.DARK_AQUA).append(" " + totalPage + " ").color(ChatColor.WHITE).append("页").color(ChatColor.DARK_AQUA);
                    if (page < totalPage) {
                        pageComponent.append(" ▶ ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/psr edit file list " + (page + 1))).color(ChatColor.YELLOW);
                    }
                    user.sendFilteredMessage(pageComponent.create());

                    user.sendFilteredText("§7§m-----------------------------------------------");
                } else {
                    user.sendFilteredText("§7 * §e/psr edit file list [页码] §7- §b查看所有加载的替换配置文件");
                }
                return;

            } else if ("select".equalsIgnoreCase(args[2])) {
                if (args.length == 4) {
                    ReplacerConfig replacerConfig = getSpecifiedReplacerConfig(args[3]);
                    if (replacerConfig != null) {
                        user.setEditorReplacerConfig(replacerConfig);
                        user.sendFilteredText("§c§lP§6§lS§3§lR §e> §a已选定替换配置: §f" + replacerConfig.getRelativePath());
                    } else {
                        user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c找不到此替换配置文件: §f" + args[3]);
                    }
                } else {
                    user.sendFilteredText("§7 * §e/psr file select <替换配置文件|索引> §7- §b选定编辑的替换配置");
                }
                return;

            } else if ("create".equalsIgnoreCase(args[2])) {
                if (args.length == 4) {
                    if (args[3].startsWith("Replacers/")) {
                        if (ReplacerManager.isYmlFile(args[3])) {
                            java.io.File file = new java.io.File(ProtocolStringReplacer.getInstance().getDataFolder() + "/" + args[3]);
                            try {
                                java.io.File fileParent = file.getParentFile();
                                if(!fileParent.exists()){
                                    fileParent.mkdirs();
                                }
                                file.createNewFile();
                                DotYamlConfiguration configuration = DotYamlConfiguration.loadConfiguration(file);
                                ReplacerConfig replacerConfig = new ReplacerConfig(file, configuration);
                                replacerConfig.saveConfig();
                                ProtocolStringReplacer.getInstance().getReplacerManager().addReplacerConfig(replacerConfig);
                                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §a已成功创建替换配置文件: §f" + replacerConfig.getRelativePath());
                            } catch (IOException exception) {
                                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c非法的文件路径.");
                            }
                        } else {
                            user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c此文件名后缀不为 \".yml\".");
                        }
                    } else {
                        user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c替换配置文件必须存储于 \"Replacers/\" 文件夹中.");
                    }
                } else {
                    user.sendFilteredText("§7 * §e/psr file create <文件名> §7- §b在磁盘中新建一个替换配置文件");
                }
                return;

            } else if ("delete".equalsIgnoreCase(args[2])) {
                if (args.length == 4) {
                    ReplacerConfig replacerConfig = getSpecifiedReplacerConfig(args[3]);
                    if (replacerConfig != null) {
                        if (user.isConfirmed(args)) {
                            if (user.isConfirmExpired()) {
                                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c确认操作已超时. 请重新执行.");
                                user.clearCommandToConfirm();
                            } else {
                                //noinspection ResultOfMethodCallIgnored
                                replacerConfig.getFile().delete();
                                ProtocolStringReplacer.getInstance().getReplacerManager().getReplacerConfigList().remove(replacerConfig);
                                user.clearCommandToConfirm();
                                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §a已删除替换配置文件: §f" + replacerConfig.getRelativePath());
                            }
                        } else {
                            user.setCommandToConfirm(args);
                            user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c请在 15 秒内重新执行一次操作或输入 §e/psr confirm §c以确认删除 §f" + replacerConfig.getRelativePath());
                        }
                    } else {
                        user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c找不到此替换配置文件: §f" + args[3]);
                    }
                } else {
                    user.sendFilteredText("§7 * §e/psr file delete <替换配置文件|索引> §7- §b在磁盘中删除一个替换配置文件");
                }
                return;
            }
        }
        sendHelp(user);
    }

    @Override
    public List<String> onTab(@NotNull User user, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 3) {
            list = Arrays.asList("help", "list", "select", "create", "delete");
        } else if (args.length == 4) {
            if (args[2].equalsIgnoreCase("list")) {
                list.add("[页码]");
            } else if (args[2].equalsIgnoreCase("delete") || args[2].equalsIgnoreCase("select")) {
                list.add("<替换配置文件|索引>");
                for (ReplacerConfig replacerConfig : ProtocolStringReplacer.getInstance().getReplacerManager().getReplacerConfigList()) {
                    list.add(ArgUtils.formatWithQuotes(replacerConfig.getRelativePath()));
                }
            } else if (args[2].equalsIgnoreCase("create")) {
                String arg = args[3];
                Matcher matcher = fileNamePrefix.matcher(arg);
                if (matcher.find()) {
                    int length = arg.length();
                    String subfix = ".yml\"";
                    int i = 5;
                    while (i > 0) {
                        if (arg.substring(length - i).equalsIgnoreCase(subfix.substring(0, i))) {
                            break;
                        }
                        i--;
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(arg, 0, arg.length() - i).append(subfix);
                    if (matcher.group(1) == null) {
                        stringBuilder.insert(0, '\"');
                    }
                    list.add(stringBuilder.toString());
                } else {
                    list.add("\"Replacers/");
                }
            }
        }
        return list;
    }

    @Override
    public void sendHelp(@Nonnull User user) {
        user.sendFilteredText("§7§m-----------§7§l §7[ §c§lP§6§lS§3§lR §7- §e文件编辑器§7 ]§l §7§m-----------");
        user.sendFilteredText("§7 * §e/psr edit file help §7- §b文件编辑器指令列表");
        user.sendFilteredText("§7 * §e/psr edit file list §7- §b查看所有替换配置文件");
        user.sendFilteredText("§7 * §e/psr edit file select §7- §b选定一个替换配置文件");
        user.sendFilteredText("§7 * §e/psr edit file create §7- §b新建一个替换配置文件");
        user.sendFilteredText("§7 * §e/psr edit file delete §7- §b删除一个替换配置文件");
        user.sendFilteredText("§7§m---------------------------------------------");
    }

    @Nullable
    private ReplacerConfig getSpecifiedReplacerConfig(@NotNull String string) {
        LinkedList<ReplacerConfig> replacerConfigList = ProtocolStringReplacer.getInstance().getReplacerManager().getReplacerConfigList();
        for (ReplacerConfig replacerConfig : replacerConfigList) {
            if (replacerConfig.getRelativePath().equals(string)) {
                return replacerConfig;
            }
        }
        if (StringUtils.isNumeric(string)) {
            int index = Integer.parseInt(string);
            if (replacerConfigList.size() > index) {
                return replacerConfigList.get(index);
            }
        }
        return null;
    }
}
