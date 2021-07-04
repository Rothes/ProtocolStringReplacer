package me.Rothes.ProtocolStringReplacer.Upgrades;

import me.Rothes.ProtocolStringReplacer.API.Configuration.DotYamlConfiguration;

import javax.annotation.Nonnull;
import java.io.File;

public abstract class AbstractUpgradeHandler {

    public abstract void upgrade();

    protected abstract void upgradeReplacerConfig(@Nonnull File file, @Nonnull DotYamlConfiguration config);

}
