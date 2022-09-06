package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;

import java.util.List;

public final class MapChunk extends AbstractServerSignPacketListener {

    public MapChunk() {
        super(PacketType.Play.Server.MAP_CHUNK);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();
        List<NbtBase<?>> nbtBaseList = packet.getListNbtModifier().read(0);
        for (NbtBase<?> nbtBase : nbtBaseList) {
            NbtCompound nbtCompound = (NbtCompound) nbtBase;
            if (!nbtCompound.containsKey("id")) {
                continue;
            }
            if ("minecraft:sign".equals(nbtCompound.getString("id"))) {
                setSignText(packetEvent, nbtCompound, user, filter);
            }
        }
    }

}
