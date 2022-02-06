package me.rothes.protocolstringreplacer.packetlisteners.server.sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.user.PsrUser;

public class TileEntityDataUpper18 extends AbstractServerSignPacketListener {

    private final Object signType;

    public TileEntityDataUpper18() {
        super(PacketType.Play.Server.TILE_ENTITY_DATA);

        Object temp;
        try {
            temp = Class.forName("net.minecraft.world.level.block.entity.TileEntityTypes").getField("h").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            ProtocolStringReplacer.error("§4Error when hooking into TILE_ENTITY_DATA packet.");
            temp = null;
        }
        signType = temp;
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();
        if (packet.getModifier().read(1).equals(signType)) {
            NbtCompound nbtCompound = (NbtCompound) packet.getNbtModifier().read(0);
            setSignText(packetEvent, nbtCompound, user, filter);
        }
    }

}
