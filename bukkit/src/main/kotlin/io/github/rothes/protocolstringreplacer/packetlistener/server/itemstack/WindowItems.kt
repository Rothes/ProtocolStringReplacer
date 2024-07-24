package io.github.rothes.protocolstringreplacer.packetlistener.server.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BukkitConverters;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.server.AbstractServerPacketListener;
import io.github.rothes.protocolstringreplacer.replacer.ReplacerManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class WindowItems extends AbstractServerItemPacketListener {

    public WindowItems() {
        super(PacketType.Play.Server.WINDOW_ITEMS);
    }

    protected void process(@NotNull PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        user.clearUserMetaCache();
        Object[] read = (Object[]) packetEvent.getPacket().getModifier().read(1);
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> nbt = replacerManager.getAcceptedReplacers(user, itemNbtFilter);
        List<ReplacerConfig> display = replacerManager.getAcceptedReplacers(user, itemDisplayFilter);
        List<ReplacerConfig> entries = replacerManager.getAcceptedReplacers(user, itemEntriesFilter);

        boolean saveMeta = !user.isInAnvil();
        for (Object item : read) {
            ItemStack itemStack = BukkitConverters.getItemStackConverter().getSpecific(item);
            if (itemStack.getType() == Material.AIR) {
                saveMeta = true;
                continue;
            }
            boolean blocked = AbstractServerPacketListener.replaceItemStack(packetEvent, user, listenType, itemStack, nbt, display, entries,
                    // Avoid too many packets kick
                    saveMeta);
            saveMeta = true;
            if (blocked) {
                return;
            }
        }
    }

}
