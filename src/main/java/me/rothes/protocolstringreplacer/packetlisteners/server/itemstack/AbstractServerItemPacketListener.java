package me.rothes.protocolstringreplacer.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import me.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerPacketListener;
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
