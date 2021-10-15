package me.rothes.protocolstringreplacer.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.user.User;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class WindowItemsUpper12 extends AbstractServerItemPacketListener {

    public WindowItemsUpper12() {
        super(PacketType.Play.Server.WINDOW_ITEMS);
    }

    protected void process(PacketEvent packetEvent) {
        User user = getEventUser(packetEvent);
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, itemFilter);
        for (ItemStack itemStack : packetEvent.getPacket().getItemListModifier().read(0)) {
            boolean blocked = replaceItemStack(packetEvent, user, listenType, itemStack, replacers);
            if (blocked) {
                return;
            }
        }
    }

}
