package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import io.github.rothes.protocolstringreplacer.bukkit.api.configuration.CommentYamlConfiguration;
import io.github.rothes.protocolstringreplacer.bukkit.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.ListenType;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.AbstractServerPacketListener;
import java.util.List;
import java.util.function.BiPredicate;

public abstract class AbstractServerItemPacketListener extends AbstractServerPacketListener {

    protected final BiPredicate<ReplacerConfig, PsrUser> itemFilter;

    protected AbstractServerItemPacketListener(PacketType packetType) {
        super(packetType, ListenType.ITEMSTACK);
        itemFilter = (replacerConfig, user) -> {
            if (containType(replacerConfig) && checkPermission(user, replacerConfig)) {
                String currentWindowTitle = user.getCurrentWindowTitle();
                CommentYamlConfiguration configuration = replacerConfig.getConfiguration();
                if (configuration == null) {
                    return true;
                }
                List<String> windowTitles = configuration.getStringList("Options.Filter.ItemStack.Window-Title");
                if (windowTitles.isEmpty()) {
                    return true;
                }
                if (currentWindowTitle == null) {
                    return configuration.getBoolean("Options.Filter.ItemStack.Ignore-Inventory-Title", false);
                } else {
                    return windowTitles.contains(currentWindowTitle);
                }
            }
            return false;
        };
    }

}
