package io.github.rothes.protocolstringreplacer.packetlistener.server.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.server.AbstractServerPacketListener;
import io.github.rothes.protocolstringreplacer.replacer.ReplacerManager;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class SetSlot extends AbstractServerItemPacketListener {

    public SetSlot() {
        super(PacketType.Play.Server.SET_SLOT);
    }

    protected void process(@NotNull PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> nbt = replacerManager.getAcceptedReplacers(user, itemNbtFilter);
        List<ReplacerConfig> display = replacerManager.getAcceptedReplacers(user, itemDisplayFilter);
        List<ReplacerConfig> entries = replacerManager.getAcceptedReplacers(user, itemEntriesFilter);

        ItemStack itemStack = packetEvent.getPacket().getItemModifier().read(0);
        AbstractServerPacketListener.replaceItemStack(packetEvent, user, listenType, itemStack, nbt, display, entries, true);
    }

}
