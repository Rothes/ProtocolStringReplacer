package me.rothes.protocolstringreplacer.packetlisteners.server.scoreboard;

import com.comphenix.protocol.PacketType;
import me.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;

import java.util.function.BiPredicate;

public abstract class BaseUpdateTeamListener extends AbstractServerPacketListener {

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
