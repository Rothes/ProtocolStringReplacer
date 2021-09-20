package me.rothes.protocolstringreplacer.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.user.User;
import me.rothes.protocolstringreplacer.api.configuration.DotYamlConfiguration;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerPacketListener;
import java.util.List;
import java.util.function.BiPredicate;

public abstract class AbstractServerItemPacketListener extends AbstractServerPacketListener {

    protected final BiPredicate<ReplacerConfig, User> itemFilter;

    protected AbstractServerItemPacketListener(PacketType packetType) {
        super(packetType, ListenType.ITEMSTACK);
        itemFilter = (replacerFile, user) -> {
            DotYamlConfiguration configuration = replacerFile.getConfiguration();
            List<String> windowTitles = configuration.getStringList("Options鰠Filter鰠Itemstack鰠Window-Title");
            if (containType(replacerFile)) {
                return windowTitles.isEmpty() || windowTitles.contains(user.getCurrentWindowTitle());
            }
            return false;
        };
    }

}
