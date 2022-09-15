package me.rothes.protocolstringreplacer.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import me.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;

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
