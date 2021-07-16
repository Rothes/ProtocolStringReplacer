package me.rothes.protocolstringreplacer.packetlisteners.client.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.user.User;
import org.bukkit.inventory.ItemStack;

public final class WindowClick extends AbstractClientItemPacketListener {

    public WindowClick() {
        super(PacketType.Play.Client.WINDOW_CLICK);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketReceiving(PacketEvent packetEvent) {
            User user = getEventUser(packetEvent);
            if (user.hasPermission("protocolstringreplacer.feature.usermetacache.noncreative")) {
                ItemStack itemStack = packetEvent.getPacket().getItemModifier().read(0);
                resotreItem(user, itemStack);
            }
        }
    };

}
