package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public final class EntityMetadata extends AbstractServerPacketListener {

    public byte exceptionTimes = 0;

    private final boolean shouldDV = ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 19
            && ProtocolStringReplacer.getInstance().getServerMinorVersion() >= 3;

    public EntityMetadata() {
        super(PacketType.Play.Server.ENTITY_METADATA, ListenType.ENTITY);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer ognPacket = packetEvent.getPacket();
        PacketContainer packet;
        try {
//            if (ognPacket.getEntityModifier(packetEvent).read(0) == null) {
//                return;
//            }
            packet = ognPacket.deepClone();
        } catch (RuntimeException e) {
            if (exceptionTimes < ProtocolStringReplacer.getInstance().getConfigManager().protocolLibSideStackPrintCount) {
                ProtocolStringReplacer.warn("Exception which may be a ProtocolLib side problem:", e);
                ProtocolStringReplacer.warn("Please try to update your ProtocolLib to the latest development version.");
                exceptionTimes++;
            }
            return;
        }
        if (shouldDV) {
            List<WrappedDataValue> dataValueList = packet.getDataValueCollectionModifier().read(0);
            for (WrappedDataValue wrappedDataValue : dataValueList) {
                Object getValue = wrappedDataValue.getValue();
                Object o = processObject(packetEvent, user, getValue);
                if (o != null) {
                    wrappedDataValue.setValue(o);
                }
            }

        } else {
            List<WrappedWatchableObject> metadataList = packet.getWatchableCollectionModifier().read(0);

            if (metadataList != null) {
                for (WrappedWatchableObject watchableObject : metadataList) {
                    Object getValue = watchableObject.getValue();
                    Object o = processObject(packetEvent, user, getValue);
                    if (o != null) {
                        watchableObject.setValue(o);
                    }
                }
            }
        }
        packetEvent.setPacket(packet);
    }

    private Object processObject(PacketEvent packetEvent, PsrUser user, Object object) {
        if (object instanceof Optional<?>) {
            // Name of the entity
            Optional<?> value = (Optional<?>) object;
            if (value.isPresent()) {
                Object get = value.get();
                WrappedChatComponent wrappedChatComponent;
                if (MinecraftReflection.getIChatBaseComponentClass().isInstance(get)) {
                    // Legacy
                    wrappedChatComponent = WrappedChatComponent.fromHandle(get);
                } else if (get instanceof WrappedChatComponent) {
                    // New
                    wrappedChatComponent = (WrappedChatComponent) get;
                } else {
                    return null;
                }

                String replacedJson = getReplacedJson(packetEvent, user, listenType, wrappedChatComponent.getJson(), filter);
                if (replacedJson != null) {
                    wrappedChatComponent.setJson(replacedJson);
                    return Optional.of(wrappedChatComponent.getHandle());
                } else {
                    return null;
                }
            }

        } else if (MinecraftReflection.getIChatBaseComponentClass().isInstance(object)) {
            // Name of the entity
            WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromHandle(object);
            String replacedJson = getReplacedJson(packetEvent, user, listenType, wrappedChatComponent.getJson(), filter);
            if (replacedJson != null) {
                wrappedChatComponent.setJson(replacedJson);
                return Optional.of(wrappedChatComponent.getHandle());
            } else {
                return null;
            }

        } else if (object instanceof String) {
            // Name of the entity CONFIRMED ON SPIGOT 1.12.2
            return getReplacedText(packetEvent, user, listenType, (String) object, filter);

        } else if (BukkitConverters.getItemStackConverter().getSpecificType().isInstance(object)) {
            // Item in Item Frame
            ItemStack itemStack = BukkitConverters.getItemStackConverter().getSpecific(object);
            replaceItemStack(packetEvent, user, listenType, itemStack, filter);
        }
        return null;
    }

}
