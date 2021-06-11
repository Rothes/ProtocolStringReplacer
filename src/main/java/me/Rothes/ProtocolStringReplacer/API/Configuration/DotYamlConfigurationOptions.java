package me.Rothes.ProtocolStringReplacer.API.Configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.jetbrains.annotations.NotNull;

public class DotYamlConfigurationOptions extends YamlConfigurationOptions {

    protected DotYamlConfigurationOptions(@NotNull YamlConfiguration configuration) {
        super(configuration);
        this.pathSeparator('鰠');
    }

}
