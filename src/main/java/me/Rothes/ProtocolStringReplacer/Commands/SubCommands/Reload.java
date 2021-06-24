package me.Rothes.ProtocolStringReplacer.Commands.SubCommands;

import me.Rothes.ProtocolStringReplacer.Commands.SubCommand;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.User.User;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class Reload extends SubCommand {

    public Reload() {
        super("reload", "protocolstringreplacer.command.reload", "重载插件配置文件");
    }

    @Override
    public void onExecute(@Nonnull User user, @Nonnull String[] args) {
        if (args.length == 1) {
            user.sendFilteredText("§c§lP§6§lS§3§lR §e> §b正在异步重载插件...");
            Bukkit.getScheduler().runTaskAsynchronously(ProtocolStringReplacer.getInstance(), () -> ProtocolStringReplacer.getInstance().reload(user));
        } else {
            sendHelp(user);
        }
    }

    @Override
    public List<String> onTab(@NotNull User user, @NotNull String[] args) {
        return null;
    }

    @Override
    public void sendHelp(@Nonnull User user) {
        user.sendFilteredText("§7§m------§7§l §7[ §c§lP§6§lS§3§lR §7- §e重载§7 ]§l §7§m------");
        user.sendFilteredText("§7 * §e/psr reload help §7- §b重载指令列表");
        user.sendFilteredText("§7 * §e/psr reload§7- §b异步重载插件所有配置文件");
        user.sendFilteredText("§7§m-----------------------------------------");
    }

}
