package me.rothes.protocolstringreplacer.api.replacer;

import me.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.MatchMode;
import me.rothes.protocolstringreplacer.replacer.ReplaceMode;
import org.apache.commons.collections.map.ListOrderedMap;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neosearch.stringsearcher.StringSearcher;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface ReplacerConfig {

    /**
     * Determines whether PSR should saveConfig on disable and reload or not.
     *
     * @return Edited.
     */
    boolean isEdited();

    /**
     * @return If this replacer config is enabled.
     */
    boolean isEnabled();

    /**
     * @return The priority of this replacer config.
     */
    int getPriority();

    @NotNull List<ListenType> getListenTypeList();
    @NotNull ListOrderedMap getReplaces(@Nonnull ReplaceMode replaceMode);
    @NotNull List<Object> getBlocks(@Nonnull ReplaceMode replaceMode);
    @Nullable String getAuthor();
    @Nullable String getVersion();
    @NotNull MatchMode getMatchMode();
    @NotNull StringSearcher<String> getReplacesStringSearcher(ReplaceMode replaceMode);
    @NotNull StringSearcher<String> getBlocksStringSearcher(ReplaceMode replaceMode);

    /**
     * @return The relative path of this replacer config. Used for some commands.
     */
    @NotNull String getRelativePath();

    /**
     * @return The file of the FileConfiguration. Can be null if not exist.
     */
    @Nullable File getFile();
    @Nullable CommentYamlConfiguration getConfiguration();
    void saveConfig();

    default int getMaxTextLength() {
        return -1;
    }
    default int getMaxJsonLength() {
        return -1;
    }
    default int getMaxDirectLength() {
        return -1;
    }
    default @NotNull String getPermissionLimit() {
        return "";
    }
    default @NotNull List<String> getWindowTitleLimit() {
        return Collections.emptyList();
    }
    default boolean windowTitleLimitIgnoreInventory() {
        return false;
    }
    default boolean handleScoreboardTitle() {
        return false;
    }
    default boolean handleScoreboardEntityName() {
        return false;
    }
    default boolean handleScoreboardTeamDisplayName() {
        return false;
    }
    default boolean handleScoreboardTeamPrefix() {
        return false;
    }
    default boolean handleScoreboardTeamSuffix() {
        return false;
    }
    default boolean handleItemStackNbt() {
        return false;
    }
    default boolean handleItemStackDisplay() {
        return false;
    }
    default boolean handleItemStackDisplayEntries() {
        return true;
    }
    default @NotNull Set<Material> acceptedItemTypes() {
        return Collections.emptySet();
    }
    default boolean acceptsLocale(String locale) {
        return true;
    }

}
