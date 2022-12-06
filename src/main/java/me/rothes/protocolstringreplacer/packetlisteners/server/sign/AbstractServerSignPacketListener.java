package me.rothes.protocolstringreplacer.packetlisteners.server.sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiPredicate;

public abstract class AbstractServerSignPacketListener extends AbstractServerPacketListener {

    protected AbstractServerSignPacketListener(PacketType packetType) {
        super(packetType, ListenType.SIGN);
    }

    protected void setSignText(@NotNull PacketEvent packetEvent, @NotNull NBTContainer nbtContainer, @NotNull PsrUser user, @NotNull BiPredicate<ReplacerConfig, PsrUser> filter) {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, filter);

        String key;
        for (int i = 1; i <= 4; i++) {
            key = "Text" + i;
            String original = nbtContainer.getString(key);
            if (original == null || original.equals("null")) {
                continue;
            }
            String replacedJson = getReplacedJson(packetEvent, user, listenType, original, replacers);
            if (replacedJson == null) {
                return;
            }
            if (replacedJson.equals(original)) {
                continue;
            }
            nbtContainer.setString(key, replacedJson);
        }
    }

}
