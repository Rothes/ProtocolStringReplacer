package io.github.rothes.protocolstringreplacer.upgrade;

import io.github.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import io.github.rothes.protocolstringreplacer.util.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractUpgradeHandler {

    public abstract void upgrade();

    protected abstract void upgradeReplacerConfig(@Nonnull File file, @Nonnull YamlConfiguration config);

    protected void upgradeAllReplacerConfigs(@Nonnull File folder) {
        HashMap<File, CommentYamlConfiguration> loaded = new HashMap<>();
        List<File> files = FileUtils.getFolderFiles(folder, true, ".yml");
        for (File file : files) {
            loaded.put(file, CommentYamlConfiguration.loadConfiguration(file));
        }
        for (Map.Entry<File, CommentYamlConfiguration> entry : loaded.entrySet()) {
            upgradeReplacerConfig(entry.getKey(), entry.getValue());
        }
    }

}
