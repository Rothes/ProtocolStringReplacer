package me.rothes.protocolstringreplacer.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import me.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;

import java.util.function.BiPredicate;

public abstract class AbstractServerItemPacketListener extends AbstractServerPacketListener {

    protected final BiPredicate<ReplacerConfig, PsrUser> itemNbtFilter =
            (replacerConfig, user) -> containType(replacerConfig)
                    && replacerConfig.handleItemStackNbt() && checkFilter(user, replacerConfig) && checkWindowTitle(user, replacerConfig);
    protected final BiPredicate<ReplacerConfig, PsrUser> itemDisplayFilter =
            (replacerConfig, user) -> containType(replacerConfig)
                    && replacerConfig.handleItemStackDisplay() && checkFilter(user, replacerConfig) && checkWindowTitle(user, replacerConfig);
    protected final BiPredicate<ReplacerConfig, PsrUser> itemEntriesFilter = (replacerConfig, user) -> containType(replacerConfig)
            && replacerConfig.handleItemStackDisplayEntries() && checkFilter(user, replacerConfig) && checkWindowTitle(user, replacerConfig);

    protected AbstractServerItemPacketListener(PacketType packetType) {
        super(packetType, ListenType.ITEMSTACK);
    }

}
