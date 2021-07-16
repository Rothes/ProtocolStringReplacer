package me.rothes.protocolstringreplacer.upgrades;

import me.rothes.protocolstringreplacer.api.configuration.DotYamlConfiguration;

import javax.annotation.Nonnull;
import java.io.File;

public abstract class AbstractUpgradeHandler {

    public abstract void upgrade();

    protected abstract void upgradeReplacerConfig(@Nonnull File file, @Nonnull DotYamlConfiguration config);

}
