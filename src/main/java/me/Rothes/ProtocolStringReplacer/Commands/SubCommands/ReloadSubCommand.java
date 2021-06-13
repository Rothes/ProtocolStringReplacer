package me.Rothes.ProtocolStringReplacer.Commands.SubCommands;

import me.Rothes.ProtocolStringReplacer.Commands.SubCommand;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

public class ReloadSubCommand extends SubCommand {

    public ReloadSubCommand() {
        super("reload", "protocolstringreplacer.command.reload", "重载插件配置文件");
    }

    @Override
    public void onExecute(@Nonnull CommandSender sender, @Nonnull String[] args) {
        sender.sendMessage("§c§lP§6§lS§3§lR §e> §b正在异步重载插件...");
        if (args.length == 1) {
            Bukkit.getScheduler().runTaskAsynchronously(ProtocolStringReplacer.getInstance(), () -> ProtocolStringReplacer.getInstance().reload(sender));
        } else {
            sender.sendMessage("§c§lP§6§lS§3§lR §e> §c参数过多. §e/psr reload");
        }
    }

}
