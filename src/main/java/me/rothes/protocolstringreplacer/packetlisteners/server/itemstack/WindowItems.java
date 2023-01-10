package me.rothes.protocolstringreplacer.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BukkitConverters;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WindowItems extends AbstractServerItemPacketListener {

    public WindowItems() {
        super(PacketType.Play.Server.WINDOW_ITEMS);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        user.clearUserMetaCache();
        Object[] read = (Object[]) packetEvent.getPacket().getModifier().read(1);
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, itemFilter);
        boolean saveMeta = !user.isInAnvil();
        for (Object item : read) {
            ItemStack itemStack = BukkitConverters.getItemStackConverter().getSpecific(item);
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
    }

}
