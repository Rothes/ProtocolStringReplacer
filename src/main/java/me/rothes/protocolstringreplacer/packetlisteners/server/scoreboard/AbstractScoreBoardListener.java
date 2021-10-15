package me.rothes.protocolstringreplacer.packetlisteners.server.scoreboard;

import com.comphenix.protocol.PacketType;
import me.rothes.protocolstringreplacer.api.configuration.CommentYamlConfiguration;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.api.user.User;

import java.util.function.BiPredicate;

public abstract class AbstractScoreBoardListener extends AbstractServerPacketListener {

    protected final BiPredicate<ReplacerConfig, User> titleFilter;
    protected final BiPredicate<ReplacerConfig, User> entityNameFilter;

    protected AbstractScoreBoardListener(PacketType packetType, ListenType listenType) {
        super(packetType, listenType);
        titleFilter = (replacerFile, user) -> {
            CommentYamlConfiguration configuration = replacerFile.getConfiguration();
            boolean replace = configuration.getBoolean("Options.Filter.ScoreBoard.Replace-Title", false);
            return replace && containType(replacerFile);
        };
        entityNameFilter = (replacerFile, user) -> {
            CommentYamlConfiguration configuration = replacerFile.getConfiguration();
            boolean replace = configuration.getBoolean("Options.Filter.ScoreBoard.Replace-Entity-Name", false);
            return replace && containType(replacerFile);
        };
    }

}
