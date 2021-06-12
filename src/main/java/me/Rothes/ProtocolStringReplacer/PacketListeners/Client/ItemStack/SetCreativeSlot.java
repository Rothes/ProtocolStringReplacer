package me.Rothes.ProtocolStringReplacer.PacketListeners.Client.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.User.User;
import org.bukkit.inventory.ItemStack;

public class SetCreativeSlot extends AbstractClientItemPacketListener {

    public SetCreativeSlot() {
        super(PacketType.Play.Client.SET_CREATIVE_SLOT);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketReceiving(PacketEvent packetEvent) {
            User user = getEventUser(packetEvent);
            if (user.hasPermission("protocolstringreplacer.feature.usermetacache")) {
                ItemStack itemStack = packetEvent.getPacket().getItemModifier().read(0);
                resotreItem(user, itemStack);
            }
        }
    };

}
