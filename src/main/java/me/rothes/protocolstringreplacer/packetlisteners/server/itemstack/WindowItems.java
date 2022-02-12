package me.rothes.protocolstringreplacer.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BukkitConverters;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
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
        user.cleanUserMetaCache();
        Object[] read = (Object[]) packetEvent.getPacket().getModifier().read(1);
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, itemFilter);
        for (Object item : read) {
            ItemStack itemStack = BukkitConverters.getItemStackConverter().getSpecific(item);
            boolean blocked = replaceItemStack(packetEvent, user, listenType, itemStack, replacers);
            if (blocked) {
                return;
            }
        }
    }

}
