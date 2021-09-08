package com.sk89q.protocolstringreplacer;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import org.apache.commons.lang.Validate;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class is used to disguise a server class.
 * So we can disable the default prefix of the Logger,
 * and then use what we customed.
 * Special thanks to Bkm016@Github !
 * See this thread for the detail:
 * https://www.mcbbs.net/thread-1258172-1-1.html
 */
public class PSRDisguisePlugin implements Plugin {

    private final ProtocolStringReplacer plugin;

    public PSRDisguisePlugin(@Nonnull final ProtocolStringReplacer plugin) {
        Validate.notNull(plugin, "ProtocolStringReplacer cannot be null");
        this.plugin = plugin;
    }

    @NotNull
    @Override
    public File getDataFolder() {
        return null;
    }

    @NotNull
    @Override
    public PluginDescriptionFile getDescription() {
        return plugin.getDescription();
    }

    @NotNull
    @Override
    public FileConfiguration getConfig() {
        return null;
    }

    @Nullable
    @Override
    public InputStream getResource(@NotNull String filename) {
        return null;
    }

    @Override
    public void saveConfig() {

    }

    @Override
    public void saveDefaultConfig() {

    }

    @Override
    public void saveResource(@NotNull String resourcePath, boolean replace) {

    }

    @Override
    public void reloadConfig() {

    }

    @NotNull
    @Override
    public PluginLoader getPluginLoader() {
        return null;
    }

    @NotNull
    @Override
    public Server getServer() {
        return plugin.getServer();
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public boolean isNaggable() {
        return false;
    }

    @Override
    public void setNaggable(boolean canNag) {

    }

    @Nullable
    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        return null;
    }

    @NotNull
    @Override
    public Logger getLogger() {
        return null;
    }

    @NotNull
    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }

}
