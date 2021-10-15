package me.rothes.protocolstringreplacer.packetlisteners.server.sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import me.rothes.protocolstringreplacer.api.user.User;

import java.util.List;

public final class MapChunk extends AbstractServerSignPacketListener {

    public MapChunk() {
        super(PacketType.Play.Server.MAP_CHUNK);
    }

    protected void process(PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        User user = getEventUser(packetEvent);
        List<NbtBase<?>> nbtBaseList = packet.getListNbtModifier().read(0);
        for (NbtBase<?> nbtBase : nbtBaseList) {
            NbtCompound nbtCompound = (NbtCompound) nbtBase;
            if ("minecraft:sign".equals(nbtCompound.getString("id"))) {
                setSignText(packetEvent, nbtCompound, user, filter);
            }
        }
    }

}
