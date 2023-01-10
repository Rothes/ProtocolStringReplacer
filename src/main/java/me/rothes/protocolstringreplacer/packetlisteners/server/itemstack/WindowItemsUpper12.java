package me.rothes.protocolstringreplacer.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
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
        user.clearUserMetaCache();
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, itemFilter);

        StructureModifier<List<ItemStack>> itemListModifier = packetEvent.getPacket().getItemListModifier();
        List<ItemStack> read = itemListModifier.read(0);
        boolean saveMeta = !user.isInAnvil();
        for (ItemStack itemStack : read) {
            if (itemStack.getType() == Material.AIR) {
                saveMeta = true;
                continue;
            }
            boolean blocked = replaceItemStack(packetEvent, user, listenType, itemStack, replacers,
                    // Avoid too many packets kick
                    saveMeta);
            saveMeta = true;
            if (blocked) {
                return;
            }
        }
        itemListModifier.write(0, read);
    }

}
