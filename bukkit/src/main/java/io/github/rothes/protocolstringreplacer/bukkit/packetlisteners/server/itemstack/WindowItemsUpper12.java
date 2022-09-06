package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import io.github.rothes.protocolstringreplacer.bukkit.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.bukkit.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.ReplacerManager;
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
