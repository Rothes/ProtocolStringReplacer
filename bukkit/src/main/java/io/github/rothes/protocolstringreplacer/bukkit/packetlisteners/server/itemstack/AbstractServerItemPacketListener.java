package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import io.github.rothes.protocolstringreplacer.bukkit.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.AbstractServerPacketListener;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.ListenType;

import java.util.List;
import java.util.function.BiPredicate;

public abstract class AbstractServerItemPacketListener extends AbstractServerPacketListener {

    protected final BiPredicate<ReplacerConfig, PsrUser> itemFilter;

    protected AbstractServerItemPacketListener(PacketType packetType) {
        super(packetType, ListenType.ITEMSTACK);
        itemFilter = (replacerConfig, user) -> {
            if (containType(replacerConfig) && checkPermission(user, replacerConfig)) {
                String currentWindowTitle = user.getCurrentWindowTitle();
                List<String> windowTitles = replacerConfig.getWindowTitleLimit();
                if (windowTitles.isEmpty()) {
                    return true;
                }
                if (currentWindowTitle == null) {
                    return replacerConfig.windowTitleLimitIgnoreInventory();
                } else {
                    return windowTitles.contains(currentWindowTitle);
                }
            }
            return false;
        };
    }

}
