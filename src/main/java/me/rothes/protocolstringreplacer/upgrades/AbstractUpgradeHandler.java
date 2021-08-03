package me.rothes.protocolstringreplacer.upgrades;

import me.rothes.protocolstringreplacer.api.configuration.DotYamlConfiguration;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractUpgradeHandler {

    public abstract void upgrade();

    protected abstract void upgradeReplacerConfig(@Nonnull File file, @Nonnull DotYamlConfiguration config);

    protected void upgradeAllReplacerConfigs(@Nonnull File folder) {
        HashMap<File, DotYamlConfiguration> loadReplacesFiles = ReplacerManager.loadReplacesFiles(folder);
        for (Map.Entry<File, DotYamlConfiguration> entry : loadReplacesFiles.entrySet()) {
            upgradeReplacerConfig(entry.getKey(), entry.getValue());
        }
    }

}
