package io.github.rothes.protocolstringreplacer.packetlistener.server.scoreboard;

import com.comphenix.protocol.PacketType;
import io.github.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.server.BaseServerPacketListener;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;

import java.util.function.BiPredicate;

public abstract class BaseUpdateTeamListener extends BaseServerPacketListener {

    protected final BiPredicate<ReplacerConfig, PsrUser> teamDNameFilter = (replacerConfig, user) ->
            containType(replacerConfig) && checkFilter(user, replacerConfig) && replacerConfig.handleScoreboardTeamDisplayName();
    protected final BiPredicate<ReplacerConfig, PsrUser> teamPrefixFilter = (replacerConfig, user) ->
            containType(replacerConfig) && checkFilter(user, replacerConfig) && replacerConfig.handleScoreboardTeamPrefix();
    protected final BiPredicate<ReplacerConfig, PsrUser> teamSuffixFilter = (replacerConfig, user) ->
            containType(replacerConfig) && checkFilter(user, replacerConfig) && replacerConfig.handleScoreboardTeamSuffix();

    protected BaseUpdateTeamListener() {
        super(PacketType.Play.Server.SCOREBOARD_TEAM, ListenType.SCOREBOARD);
    }

}
