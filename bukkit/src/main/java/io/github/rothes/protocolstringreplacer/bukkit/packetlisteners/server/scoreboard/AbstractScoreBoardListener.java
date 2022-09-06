package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.scoreboard;

import com.comphenix.protocol.PacketType;
import io.github.rothes.protocolstringreplacer.bukkit.api.configuration.CommentYamlConfiguration;
import io.github.rothes.protocolstringreplacer.bukkit.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.AbstractServerPacketListener;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.ListenType;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;

import java.util.function.BiPredicate;

public abstract class AbstractScoreBoardListener extends AbstractServerPacketListener {

    protected final BiPredicate<ReplacerConfig, PsrUser> titleFilter;
    protected final BiPredicate<ReplacerConfig, PsrUser> entityNameFilter;

    protected AbstractScoreBoardListener(PacketType packetType, ListenType listenType) {
        super(packetType, listenType);
        titleFilter = (replacerConfig, user) -> {
            if (containType(replacerConfig) && checkPermission(user, replacerConfig)) {
                CommentYamlConfiguration configuration = replacerConfig.getConfiguration();
                if (configuration == null) {
                    return true;
                }
                return configuration.getBoolean("Options.Filter.ScoreBoard.Replace-Title", false);
            }
            return false;
        };
        entityNameFilter = (replacerConfig, user) -> {
            if (containType(replacerConfig) && checkPermission(user, replacerConfig)) {
                CommentYamlConfiguration configuration = replacerConfig.getConfiguration();
                if (configuration == null) {
                    return true;
                }
                return configuration.getBoolean("Options.Filter.ScoreBoard.Replace-Entity-Name", false);
            }
            return false;
        };
    }

}
