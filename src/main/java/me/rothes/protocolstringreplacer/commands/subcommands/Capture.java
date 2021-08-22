package me.rothes.protocolstringreplacer.commands.subcommands;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.user.User;
import me.rothes.protocolstringreplacer.commands.SubCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Capture extends SubCommand {

    public Capture() {
        super("capture", "protocolstringreplacer.command.capture", "捕获部分数据包的内容");
    }

    @Override
    public void onExecute(@Nonnull User user, @Nonnull String[] args) {
        if (!user.isOnline()) {
            user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c仅玩家可使用该指令.");
            return;
        }
        if (args.length > 1) {
            if ("add".equalsIgnoreCase(args[1])) {
                addCommand(user, args);
                return;
            } else if ("remove".equalsIgnoreCase(args[1])) {
                removeCommand(user, args);
                return;
            } else if ("list".equalsIgnoreCase(args[1])) {
                listCommand(user, args);
                return;
            }
        }
        sendHelp(user);
    }

    private void addCommand(@Nonnull User user, @Nonnull String[] args) {
        if (args.length == 3) {
            ListenType listenType = ListenType.getType(args[2]);
            if (listenType == null) {
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c监听类型 §f" + args[2] + " §c不存在.");
                return;
            }
            if (!listenType.isCapturable()) {
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c此监听类型不可捕获.");
                return;
            }
            if (user.isCapturing(listenType)) {
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c您已经启用了 §f" + listenType.getName() + " §c的捕获了.");
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c使用指令 §e/psr capture remove " + listenType.getName() + " §c即可取消捕获.");
                return;
            }
            user.addCaptureType(listenType);
            user.sendFilteredText("§c§lP§6§lS§3§lR §e> §a已启用 §f" + listenType.getName() + " §a的捕获.");
        } else {
            user.sendFilteredText("§7 * §e/psr capture add <监听类型> §7- §b启用监听类型的Json捕获");
        }
    }

    private void removeCommand(@Nonnull User user, @Nonnull String[] args) {
        if (args.length == 3) {
            ListenType listenType = ListenType.getType(args[2]);
            if (listenType == null) {
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c监听类型 §f" + args[2] + " §c不存在.");
                return;
            }
            if (!user.isCapturing(listenType)) {
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c您已经关闭了 §f" + listenType.getName() + " §c的捕获了.");
                user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c使用指令 §e/psr capture add " + listenType.getName() + " §c即可开启捕获.");
                return;
            }
            user.removeCaptureType(listenType);
            user.sendFilteredText("§c§lP§6§lS§3§lR §e> §a已移除 §f" + listenType.getName() + " §a的捕获.");
        } else {
            user.sendFilteredText("§7 * §e/psr capture remove <监听类型> §7- §b移除监听类型的Json捕获");
        }
    }

    private void listCommand(@Nonnull User user, @Nonnull String[] args) {
        if (args.length == 3 || args.length == 4) {
            Bukkit.getScheduler().runTaskAsynchronously(ProtocolStringReplacer.getInstance(), () -> {
                ListenType listenType = ListenType.getType(args[2]);
                if (listenType == null) {
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c监听类型 §f" + args[2] + " §c不存在.");
                    return;
                }
                if (!user.isCapturing(listenType)) {
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c您未在捕获 §f" + listenType.getName() + " §c.");
                    user.sendFilteredText("§c§lP§6§lS§3§lR §e> §c使用指令 §e/psr capture add " + listenType.getName() + " §c即可开启捕获.");
                    return;
                }
                int page = 1;
                if (args.length == 4) {
                    if (StringUtils.isNumeric(args[3])) {
                        page = Integer.parseInt(args[3]);
                    } else {
                        user.sendFilteredText("§c§lP§6§lS§3§lR §e> §f" + args[3] + " §c不是一个有效的正整数!");
                        return;
                    }
                }
                user.sendFilteredText("§7§m---------§7§l §7[ §c§lP§6§lS§3§lR §7- §e捕获列表§7 ]§l §7§m---------");

                LinkedList<BaseComponent[]> captureMessages = user.getCaptureMessages(listenType);
                int totalPage = (int) Math.ceil((float) captureMessages.size() / 10);
                for (int i = (page - 1) * 10; i < captureMessages.size() && i < page * 10; i++) {
                    user.sendFilteredMessage(captureMessages.get(i));
                }

                ComponentBuilder pageComponent = new ComponentBuilder();
                if (page > 1) {
                    pageComponent.append(" ◀ ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/psr capture list " + args[2] + " " + (page - 1))).color(ChatColor.YELLOW);
                } else {
                    pageComponent.append("   ");
                }
                pageComponent.append("第").reset().color(ChatColor.DARK_AQUA).append(" " + page + " ").color(ChatColor.WHITE).append("页").color(ChatColor.DARK_AQUA).append(" | ").color(ChatColor.GRAY).append("共").
                        color(ChatColor.DARK_AQUA).append(" " + totalPage + " ").color(ChatColor.WHITE).append("页").color(ChatColor.DARK_AQUA);
                if (page < totalPage) {
                    pageComponent.append(" ▶ ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/psr capture list " + args[2] + " " + (page + 1))).color(ChatColor.YELLOW);
                }
                user.sendFilteredMessage(pageComponent.create());
                user.sendFilteredText("§7§m---------------------------------------");
            });
        } else {
            user.sendFilteredText("§7 * §e/psr capture list <监听类型> [页码] §7- §b列出捕获的Json信息");
        }
    }



    @Override
    public List<String> onTab(@NotNull User user, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 2) {
            list = Arrays.asList("add", "remove", "list");
        } else if (args.length == 3
                && args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("list")) {
            list.add("<监听类型>");
            for (ListenType listenType : ListenType.values()) {
                if (listenType.isCapturable()) {
                    list.add(listenType.getName());
                }
            }
        } else if (args.length == 4
                && args[1].equalsIgnoreCase("list")) {
            list.add("[页码]");
        }
        return list;
    }

    @Override
    public void sendHelp(@Nonnull User user) {
        user.sendFilteredText("§7§m-----------§7§l §7[ §c§lP§6§lS§3§lR §7- §e捕获工具§7 ]§l §7§m-----------");
        user.sendFilteredText("§7 * §e/psr capture add §7- §b启用监听类型的Json捕获");
        user.sendFilteredText("§7 * §e/psr capture remove §7- §b移除监听类型的Json捕获");
        user.sendFilteredText("§7 * §e/psr capture list §7- §b列出捕获的Json信息");
        user.sendFilteredText("§7§m-------------------------------------------");
    }

}
