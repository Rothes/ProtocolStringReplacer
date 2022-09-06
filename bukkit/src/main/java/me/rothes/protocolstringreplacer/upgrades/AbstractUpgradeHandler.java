package me.rothes.protocolstringreplacer.upgrades;

import me.rothes.protocolstringreplacer.api.configuration.DotYamlConfiguration;
import me.rothes.protocolstringreplacer.utils.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractUpgradeHandler {

    public abstract void upgrade();

    protected abstract void upgradeReplacerConfig(@Nonnull File file, @Nonnull DotYamlConfiguration config);

    protected void upgradeAllReplacerConfigs(@Nonnull File folder) {
        HashMap<File, DotYamlConfiguration> loaded = new HashMap<>();
        List<File> files = FileUtils.getFolderFiles(folder, true, ".yml");
        for (File file : files) {
            loaded.put(file, DotYamlConfiguration.loadConfiguration(file));
        }
        for (Map.Entry<File, DotYamlConfiguration> entry : loaded.entrySet()) {
            upgradeReplacerConfig(entry.getKey(), entry.getValue());
        }
    }

}
