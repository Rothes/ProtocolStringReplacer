package me.rothes.protocolstringreplacer.packetlisteners.server.sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import org.jetbrains.annotations.NotNull;

public class TileEntityDataPost18 extends AbstractServerSignPacketListener {

    public TileEntityDataPost18() {
        super(PacketType.Play.Server.TILE_ENTITY_DATA);
    }

    protected void process(@NotNull PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();
        if (TileTypeHelper.isSignType(packet.getModifier().read(1))) {
            Object read = packet.getStructures().withType(MinecraftReflection.getNBTBaseClass()).read(0);
            replaceSign(packetEvent, new NBTContainer(read), user, filter);
        }
    }

}
