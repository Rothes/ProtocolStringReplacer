package io.github.rothes.protocolstringreplacer.bukkit.api.configuration;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Only used to upgrade old configs currently.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class DotYamlConfiguration extends CommentYamlConfiguration {

    public DotYamlConfiguration() {
        super();
        options().pathSeparator('鰠');
    }

    @Nonnull
    public static DotYamlConfiguration loadConfiguration(@Nonnull File file) {
        Validate.notNull(file, "File cannot be null");
        DotYamlConfiguration config = new DotYamlConfiguration();

        try {
            config.load(file);
        } catch (FileNotFoundException ignored) {
        } catch (IOException | InvalidConfigurationException var4) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, var4);
        }

        return config;
    }

}
