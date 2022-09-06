package io.github.rothes.protocolstringreplacer.bukkit.upgrades;

import io.github.rothes.protocolstringreplacer.bukkit.api.configuration.CommentYamlConfiguration;
import io.github.rothes.protocolstringreplacer.bukkit.api.configuration.DotYamlConfiguration;
import io.github.rothes.protocolstringreplacer.bukkit.PsrLocalization;
import io.github.rothes.protocolstringreplacer.bukkit.ProtocolStringReplacer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class UpgradeHandler4To5 extends AbstractUpgradeHandler{

    @Override
    public void upgrade() {
        CommentYamlConfiguration config = ProtocolStringReplacer.getInstance().getConfig();
        Pattern commentPattern = CommentYamlConfiguration.getCommentKeyPattern();
        ArrayList<String> coments = new ArrayList<>();
        String locale = null;
        for (String key : config.getKeys(true)) {
            if (commentPattern.matcher(key).find()) {
                coments.add(key);
            } else if (key.equals("Options.Localization")){
                locale = config.getString(key);
                for (String comment : coments) {
                    config.set(comment, null);
                }
                config.set(key, null);
            } else {
                coments.clear();
            }
        }
        config.set("Configs-Version", 5);
        try {
            config.save(ProtocolStringReplacer.getInstance().getConfigFile());
            ProtocolStringReplacer.getInstance().checkConfigKeys();
            config.set("Options.Locale", locale);
            config.save(ProtocolStringReplacer.getInstance().getConfigFile());
            PsrLocalization.initialize(ProtocolStringReplacer.getInstance());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void upgradeReplacerConfig(@NotNull File file, @NotNull DotYamlConfiguration config) {
        // Empty method.
    }

}
