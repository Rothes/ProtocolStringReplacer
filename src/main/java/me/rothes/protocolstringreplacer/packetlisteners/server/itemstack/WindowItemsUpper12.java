package me.rothes.protocolstringreplacer.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class WindowItemsUpper12 extends AbstractServerItemPacketListener {

    public WindowItemsUpper12() {
        super(PacketType.Play.Server.WINDOW_ITEMS);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        user.cleanUserMetaCache();
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, itemFilter);
        boolean firstReplaced = false;
        for (ItemStack itemStack : packetEvent.getPacket().getItemListModifier().read(0)) {
            if (itemStack.getType() == Material.AIR) {
                continue;
            }
            boolean blocked = replaceItemStack(packetEvent, user, listenType, itemStack, replacers,
                    // Avoid too many packets kick
                    firstReplaced && user.isInAnvil());
            firstReplaced = true;
            if (blocked) {
                return;
            }
        }
    }

}
