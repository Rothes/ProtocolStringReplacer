package io.github.rothes.protocolstringreplacer.packetlistener.server.sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.tr7zw.nbtapi.NBTContainer;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;

import java.util.Collection;
import java.util.List;

public final class MapChunk extends BaseServerSignPacketListener {

    public MapChunk() {
        super(PacketType.Play.Server.MAP_CHUNK);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();
        List<?> read = (List<?>) packet.getModifier().withType(Collection.class).read(0);
        for (Object nbt : read) {
            NBTContainer nbtContainer = new NBTContainer(nbt);
            if (!nbtContainer.hasTag("id")) {
                continue;
            }
            if ("minecraft:sign".equals(nbtContainer.getString("id"))) {
                replaceSign(packetEvent, nbtContainer, user, filter);
            }
        }
    }

}
