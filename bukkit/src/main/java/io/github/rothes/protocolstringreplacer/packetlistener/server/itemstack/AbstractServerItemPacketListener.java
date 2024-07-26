package io.github.rothes.protocolstringreplacer.packetlistener.server.itemstack;

import com.comphenix.protocol.PacketType;
import io.github.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.server.AbstractServerPacketListener;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;

import java.util.function.BiPredicate;

public abstract class AbstractServerItemPacketListener extends AbstractServerPacketListener {

    protected final BiPredicate<ReplacerConfig, PsrUser> itemNbtFilter =
            (replacerConfig, user) -> containType(replacerConfig)
                    && replacerConfig.handleItemStackNbt() && checkFilter(user, replacerConfig) && checkWindowTitle(user, replacerConfig);
    protected final BiPredicate<ReplacerConfig, PsrUser> itemLoreFilter =
            (replacerConfig, user) -> containType(replacerConfig)
                    && replacerConfig.handleItemStackLore() && checkFilter(user, replacerConfig) && checkWindowTitle(user, replacerConfig);
    protected final BiPredicate<ReplacerConfig, PsrUser> itemEntriesFilter = (replacerConfig, user) -> containType(replacerConfig)
            && replacerConfig.handleItemStackDisplayEntries() && checkFilter(user, replacerConfig) && checkWindowTitle(user, replacerConfig);

    protected AbstractServerItemPacketListener(PacketType packetType) {
        super(packetType, ListenType.ITEMSTACK);
    }

}
