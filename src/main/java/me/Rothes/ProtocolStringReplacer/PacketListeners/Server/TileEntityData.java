package me.Rothes.ProtocolStringReplacer.PacketListeners.Server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.User.User;

public final class TileEntityData extends AbstractServerPacketListener{

    public TileEntityData() {
        super(PacketType.Play.Server.TILE_ENTITY_DATA);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            PacketContainer packet = packetEvent.getPacket();
            User user = getEventUser(packetEvent);
            // 9: Set the text on a sign
            if (packet.getIntegers().read(0) == 9) {
                NbtCompound nbtCompound = (NbtCompound) packet.getNbtModifier().read(0);
                setSignText(nbtCompound, user, filter);
            }
        }
    };

}
