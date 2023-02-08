package me.rothes.protocolstringreplacer.upgrades;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class UpgradeHandler5To6 extends AbstractUpgradeHandler {

    @Override
    public void upgrade() {
        CommentYamlConfiguration config = ProtocolStringReplacer.getInstance().getConfig();
        config.set("Configs-Version", 6);
        try {
            config.save(ProtocolStringReplacer.getInstance().getConfigFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        upgradeAllReplacerConfigs(new File(ProtocolStringReplacer.getInstance().getDataFolder(), "/Replacers"));
    }

    @Override
    protected void upgradeReplacerConfig(@NotNull File file, @NotNull YamlConfiguration config) {
        renameNode(config, "Options.Enable", "Options.Enabled");
        renameNode(config, "Options.Filter.ScoreBoard.Replace-Title", "Options.Filter.ScoreBoard.Handle-Title");
        renameNode(config, "Options.Filter.ScoreBoard.Replace-Entity-Name", "Options.Filter.ScoreBoard.Handle-Entity-Name");
        renameNode(config, "Options.Filter.ScoreBoard.Replace-Team-Display-Name", "Options.Filter.ScoreBoard.Handle-Team-Display-Name");
        renameNode(config, "Options.Filter.ScoreBoard.Replace-Team-Prefix", "Options.Filter.ScoreBoard.Handle-Team-Prefix");
        renameNode(config, "Options.Filter.ScoreBoard.Replace-Team-Suffix", "Options.Filter.ScoreBoard.Handle-Team-Suffix");
        config.set("Options.Filter.ItemStack.Handle-Nbt-Compound", false);
        config.set("Options.Filter.ItemStack.Handle-Nbt-Display-Compound", false);
        config.set("Options.Filter.ItemStack.Handle-Nbt-Display-Entries", true);
        try {
            config.save(file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void renameNode(@NotNull YamlConfiguration config, @NotNull String o, @NotNull String n) {
        Object old = config.get(o);
        if (old == null) {
            return;
        }
        config.set(n, old);
        config.set(o, null);
    }

}
