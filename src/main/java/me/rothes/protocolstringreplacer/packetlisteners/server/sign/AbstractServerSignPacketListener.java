package me.rothes.protocolstringreplacer.packetlisteners.server.sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import me.rothes.protocolstringreplacer.replacer.containers.ChatJsonContainer;
import me.rothes.protocolstringreplacer.user.User;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiPredicate;

public abstract class AbstractServerSignPacketListener extends AbstractServerPacketListener {

    protected AbstractServerSignPacketListener(PacketType packetType) {
        super(packetType, ListenType.SIGN);
    }

    protected void setSignText(@NotNull PacketEvent packetEvent, @NotNull NbtCompound nbtCompound, @NotNull User user, @NotNull BiPredicate<ReplacerConfig, User> filter) {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, filter);
        ChatJsonContainer container;

        String key;
        for (int i = 1; i < 4; i++) {
            key = "Text" + i;
            container = new ChatJsonContainer(nbtCompound.getString(key));
            container.createJsons(container);
            if (replacerManager.isJsonBlocked(container, replacers)) {
                packetEvent.setCancelled(true);
                return;
            }
            replacerManager.replaceContainerJsons(container, replacers);
            container.createDefaultChildren();
            container.createTexts(container);
            if (replacerManager.isTextBlocked(container, replacers)) {
                packetEvent.setCancelled(true);
                return;
            }
            replacerManager.replaceContainerTexts(container, replacers);
            replacerManager.setPapi(user, container.getTexts());
            nbtCompound.put(key, container.getResult());

        }
    }

}
