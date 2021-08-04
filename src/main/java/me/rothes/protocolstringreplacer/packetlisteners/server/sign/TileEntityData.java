package me.rothes.protocolstringreplacer.packetlisteners.server.sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import me.rothes.protocolstringreplacer.user.User;

public final class TileEntityData extends AbstractServerSignPacketListener {

    public TileEntityData() {
        super(PacketType.Play.Server.TILE_ENTITY_DATA);
    }

    protected void process(PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        User user = getEventUser(packetEvent);
        // 9: Set the text on a sign
        if (packet.getIntegers().read(0) == 9) {
            NbtCompound nbtCompound = (NbtCompound) packet.getNbtModifier().read(0);
            setSignText(nbtCompound, user, filter);
        }
    }

}
