package io.github.rothes.protocolstringreplacer.upgrade;

import io.github.rothes.protocolstringreplacer.api.configuration.DotYamlConfiguration;
import io.github.rothes.protocolstringreplacer.util.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DotConfigUpgradeHandler extends AbstractUpgradeHandler {

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
