package me.rothes.protocolstringreplacer.packetlisteners.server.sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

public final class MapChunkPost18 extends AbstractServerSignPacketListener {

    private Field dataField;
    private Field listField;
    private Object signType;
    private Field subTypeField;
    private Field subNbtField;

    public MapChunkPost18() {
        super(PacketType.Play.Server.MAP_CHUNK);
    }

    @Override
    protected void register() {
        Field field;
        Class<?> packetClass = PacketType.Play.Server.MAP_CHUNK.getPacketClass();
        try {
            field = packetClass.getDeclaredField("c");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException("Error when hooking into MAP_CHUNK packet");
        }
        dataField = field;

        Class<?> dataClass;
        try {
            dataClass = Class.forName(packetClass.getCanonicalName()
                    .replaceAll("WithLightPacket$", "PacketData"));
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("Error when hooking into MAP_CHUNK packet");
        }

        try {
            field = dataClass.getDeclaredField("d");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException("Error when hooking into MAP_CHUNK packet");
        }
        listField = field;

        /*

        // Not necessary and removed in 1.19

        try {
            field = dataClass.getDeclaredField("extraPackets");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException("Error when hooking into MAP_CHUNK packet");
        }
        extraField = field;
        */

        Object type;
        try {
            type = Class.forName("net.minecraft.world.level.block.entity.TileEntityTypes").getField("h").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new UnsupportedOperationException("Error when hooking into MAP_CHUNK packet");
        }
        signType = type;

        Class<?> subClass;
        try {
            subClass = Class.forName(dataClass.getCanonicalName() + "$a");
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("Error when hooking into MAP_CHUNK packet");
        }

        try {
            field = subClass.getDeclaredField("c");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException("Error when hooking into MAP_CHUNK packet");
        }
        subTypeField = field;

        try {
            field = subClass.getDeclaredField("d");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException("Error when hooking into MAP_CHUNK packet");
        }
        subNbtField = field;
        super.register();
    }

    protected void process(@NotNull PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();
        processPacket(packetEvent, user, packet.getHandle());
    }

    private void processPacket(PacketEvent packetEvent, PsrUser user, Object packet) {
        try {
            Object data = dataField.get(packet);
            for (Object obj : (List<?>) listField.get(data)) {
                if (subTypeField.get(obj).equals(signType)) {
                    Object nbt = subNbtField.get(obj);
                    if (nbt == null) {
                        continue;
                    }
                    NBTContainer nbtContainer = new NBTContainer(nbt);
                    replaceSign(packetEvent, nbtContainer, user, filter);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
