package me.rothes.protocolstringreplacer.packetlisteners.server.sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.user.PsrUser;

import java.lang.reflect.Field;
import java.util.List;

public class MapChunkUpper18 extends AbstractServerSignPacketListener {

    private final Class<?> packetClass = PacketType.Play.Server.MAP_CHUNK.getPacketClass();
    private final Field dataField;
    private final Field listField;
    private final Field extraField;
    private final Object signType;
    private final Field subTypeField;
    private final Field subNbtField;
    private final boolean hooked;

    public MapChunkUpper18() {
        super(PacketType.Play.Server.MAP_CHUNK);
        boolean hooked = true;
        Field field;
        try {
            field = packetClass.getDeclaredField("c");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            ProtocolStringReplacer.error("§4Error when hooking into MAP_CHUNK packet: 0");
            field = null;
            hooked = false;
        }
        dataField = field;

        Class<?> dataClass;
        try {
            dataClass = Class.forName(packetClass.getCanonicalName()
                    .replaceAll("WithLightPacket$", "PacketData"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            ProtocolStringReplacer.error("§4Error when hooking into MAP_CHUNK packet: 1");
            hooked = false;
            listField = null;
            extraField = null;
            signType = null;
            subTypeField = null;
            subNbtField = null;
            this.hooked = hooked;
            return;
        }

        try {
            field = dataClass.getDeclaredField("d");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            ProtocolStringReplacer.error("§4Error when hooking into MAP_CHUNK packet: 2");
            field = null;
            hooked = false;
        }
        listField = field;

        try {
            field = dataClass.getDeclaredField("extraPackets");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            ProtocolStringReplacer.error("§4Error when hooking into MAP_CHUNK packet: 3");
            field = null;
            hooked = false;
        }
        extraField = field;

        Object type;
        try {
            type = Class.forName("net.minecraft.world.level.block.entity.TileEntityTypes").getField("h").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            ProtocolStringReplacer.error("§4Error when hooking into MAP_CHUNK packet: 4");
            type = null;
            hooked = false;
        }
        signType = type;

        Class<?> subClass;
        try {
            subClass = Class.forName(dataClass.getCanonicalName() + "$a");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            ProtocolStringReplacer.error("§4Error when hooking into MAP_CHUNK packet: 5");
            hooked = false;
            subTypeField = null;
            subNbtField = null;
            this.hooked = hooked;
            return;
        }

        try {
            field = subClass.getDeclaredField("c");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            ProtocolStringReplacer.error("§4Error when hooking into MAP_CHUNK packet: 6");
            field = null;
            hooked = false;
        }
        subTypeField = field;

        try {
            field = subClass.getDeclaredField("d");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            ProtocolStringReplacer.error("§4Error when hooking into MAP_CHUNK packet: 7");
            field = null;
            hooked = false;
        }
        subNbtField = field;
        this.hooked = hooked;
    }

    protected void process(PacketEvent packetEvent) {
        if (!hooked) {
            return;
        }
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();
        processPacket(packetEvent, user, packet.getHandle());
    }

    private void processPacket(PacketEvent packetEvent, PsrUser user, Object packet) {
        if (!packet.getClass().equals(packetClass)) {
            return;
        }
        try {
            Object data = dataField.get(packet);
            EquivalentConverter<NbtBase<?>> converter = BukkitConverters.getNbtConverter();
            NbtCompound nbtCompound;
            for (Object obj : (List<?>) listField.get(data)) {
                if (subTypeField.get(obj).equals(signType)) {
                    Object nbt = subNbtField.get(obj);
                    if (nbt == null) {
                        continue;
                    }
                    nbtCompound = (NbtCompound) converter.getSpecific(nbt);
                    setSignText(packetEvent, nbtCompound, user, filter);
                }
            }
            for (Object extra : (List<?>) extraField.get(data)) {
                processPacket(packetEvent, user, extra);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
