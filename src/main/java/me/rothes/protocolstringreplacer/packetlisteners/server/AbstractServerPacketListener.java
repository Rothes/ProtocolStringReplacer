package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import me.rothes.protocolstringreplacer.packetlisteners.AbstractPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.user.User;

import java.util.function.BiPredicate;

public abstract class AbstractServerPacketListener extends AbstractPacketListener {

    protected final BiPredicate<ReplacerConfig, User> filter;
    protected final ListenType listenType;

    protected AbstractServerPacketListener(PacketType packetType, ListenType listenType) {
        super(packetType);
        this.listenType = listenType;
        filter = (replacerFile, user) -> containType(replacerFile);
    }

    protected final boolean containType(ReplacerConfig replacerConfig) {
        return replacerConfig.getListenTypeList().contains(listenType);
    }

}
