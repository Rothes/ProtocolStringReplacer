package com.sk89q.protocolstringreplacer;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.apache.commons.lang.Validate;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.BiomeProvider;
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
public class PsrDisguisePlugin implements Plugin {

    private final ProtocolStringReplacer plugin;

    public PsrDisguisePlugin(@Nonnull final ProtocolStringReplacer plugin) {
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

    @Override
    public @NotNull PluginMeta getPluginMeta() {
        return null;
    }

    @Override
    public FileConfiguration getConfig() {
        return null;
    }

    @Override
    public InputStream getResource(@NotNull String filename) {
        return null;
    }

    @Override
    public void saveConfig() {
        // Empty method.
    }

    @Override
    public void saveDefaultConfig() {
        // Empty method.
    }

    @Override
    public void saveResource(@NotNull String resourcePath, boolean replace) {
        // Empty method.
    }

    @Override
    public void reloadConfig() {
        // Empty method.
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
        // Empty method.
    }

    @Override
    public void onLoad() {
        // Empty method.
    }

    @Override
    public void onEnable() {
        // Empty method.
    }

    @Override
    public boolean isNaggable() {
        return false;
    }

    @Override
    public void setNaggable(boolean canNag) {
        // Empty method.
    }

    @Nullable
    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        return null;
    }

    @Nullable
    @Override
    public BiomeProvider getDefaultBiomeProvider(@NotNull String worldName, @Nullable String id) {
        return null;
    }

    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public @NotNull ComponentLogger getComponentLogger() {
        return Plugin.super.getComponentLogger();
    }

    @Override
    public org.slf4j.@NotNull Logger getSLF4JLogger() {
        return Plugin.super.getSLF4JLogger();
    }

    @Override
    public org.apache.logging.log4j.@NotNull Logger getLog4JLogger() {
        return Plugin.super.getLog4JLogger();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public @NotNull LifecycleEventManager<Plugin> getLifecycleManager() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }

}
