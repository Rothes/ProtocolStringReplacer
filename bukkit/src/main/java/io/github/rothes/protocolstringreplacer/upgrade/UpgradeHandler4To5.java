package io.github.rothes.protocolstringreplacer.upgrade;

import io.github.rothes.protocolstringreplacer.PsrLocalization;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public final class UpgradeHandler4To5 extends AbstractUpgradeHandler {

    @Override
    public void upgrade() {
        CommentYamlConfiguration config = ProtocolStringReplacer.getInstance().getConfig();
        Pattern commentPattern = CommentYamlConfiguration.getCommentKeyPattern();
        ArrayList<String> comments = new ArrayList<>();
        String locale = null;
        for (String key : config.getKeys(true)) {
            if (commentPattern.matcher(key).find()) {
                comments.add(key);
            } else if (key.equals("Options.Localization")){
                locale = config.getString(key);
                for (String comment : comments) {
                    config.set(comment, null);
                }
                config.set(key, null);
            } else {
                comments.clear();
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
    protected void upgradeReplacerConfig(@NotNull File file, @NotNull YamlConfiguration config) {
        // Empty method.
    }

}
