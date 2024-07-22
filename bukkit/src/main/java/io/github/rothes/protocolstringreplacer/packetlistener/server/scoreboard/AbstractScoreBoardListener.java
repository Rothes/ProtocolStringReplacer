package io.github.rothes.protocolstringreplacer.packetlistener.server.scoreboard;

import com.comphenix.protocol.PacketType;
import io.github.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.server.AbstractServerPacketListener;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;

import java.util.function.BiPredicate;

public abstract class AbstractScoreBoardListener extends AbstractServerPacketListener {

    protected final BiPredicate<ReplacerConfig, PsrUser> titleFilter;
    protected final BiPredicate<ReplacerConfig, PsrUser> entityNameFilter;

    protected AbstractScoreBoardListener(PacketType packetType, ListenType listenType) {
        super(packetType, listenType);
        titleFilter = (replacerConfig, user) -> {
            if (containType(replacerConfig) && checkFilter(user, replacerConfig)) {
                return replacerConfig.handleScoreboardTitle();
            }
            return false;
        };
        entityNameFilter = (replacerConfig, user) -> {
            if (containType(replacerConfig) && checkFilter(user, replacerConfig)) {
                return replacerConfig.handleScoreboardEntityName();
            }
            return false;
        };
    }

}
