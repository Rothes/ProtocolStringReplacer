package me.Rothes.ProtocolStringReplacer.PacketListeners.Server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.User.User;

import java.util.List;

public final class MapChunk extends AbstractServerPacketListener {

    public MapChunk() {
        super(PacketType.Play.Server.MAP_CHUNK);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            PacketContainer packet = packetEvent.getPacket();
            User user = getEventUser(packetEvent);
            List<NbtBase<?>> nbtBaseList = packet.getListNbtModifier().read(0);
            for (NbtBase<?> nbtBase : nbtBaseList) {
                NbtCompound nbtCompound = (NbtCompound) nbtBase;
                if ("minecraft:sign".equals(nbtCompound.getString("id"))) {
                    setSignText(nbtCompound, user, filter);
                }
            }
        }
    };

}
