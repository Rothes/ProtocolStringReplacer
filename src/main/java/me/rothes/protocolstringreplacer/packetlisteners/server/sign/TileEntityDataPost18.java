package me.rothes.protocolstringreplacer.packetlisteners.server.sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.user.PsrUser;

public class TileEntityDataPost18 extends AbstractServerSignPacketListener {

    private final Object signType;

    public TileEntityDataPost18() {
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
            Object read = packet.getStructures().withType(MinecraftReflection.getNBTBaseClass()).read(0);
            setSignText(packetEvent, new NBTContainer(read), user, filter);
        }
    }

}
