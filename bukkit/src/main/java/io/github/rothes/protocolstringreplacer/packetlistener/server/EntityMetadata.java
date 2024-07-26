package io.github.rothes.protocolstringreplacer.packetlistener.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public final class EntityMetadata extends BaseServerPacketListener {

    public byte exceptionTimes = 0;

    private final boolean shouldDV = ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 20
            || (ProtocolStringReplacer.getInstance().getServerMajorVersion() == 19
                && ProtocolStringReplacer.getInstance().getServerMinorVersion() >= 3);

    public EntityMetadata() {
        super(PacketType.Play.Server.ENTITY_METADATA, ListenType.ENTITY);
    }

    protected void process(@NotNull PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();
        PacketContainer processed = processPacket(packetEvent, user, packet, null, -1);
        if (packet != processed) {
            packetEvent.setPacket(processed);
        }
    }

    private PacketContainer processPacket(PacketEvent packetEvent, PsrUser user, PacketContainer packet, Object first, int index) {
        boolean clone = first == null;
        if (shouldDV) {
            List<WrappedDataValue> dataValueList = packet.getDataValueCollectionModifier().read(0);
            if (!clone) {
                dataValueList.get(index).setValue(first);
            }
            for (int i = index + 1, dataValueListSize = dataValueList.size(); i < dataValueListSize; i++) {
                WrappedDataValue wrappedDataValue = dataValueList.get(i);
                Object getValue = wrappedDataValue.getValue();
                Object o = processObject(packetEvent, user, getValue);
                if (o == this) {
                    return packet;
                } else if (o != null && o != getValue) {
                    if (clone) {
                        PacketContainer cloned = clonePacket(packet);
                        processPacket(packetEvent, user, cloned, o, i);
                        return cloned;
                    }
                    wrappedDataValue.setValue(o);
                }
            }

        } else {
            List<WrappedWatchableObject> metadataList = packet.getWatchableCollectionModifier().read(0);
            if (!clone) {
                metadataList.get(index).setValue(first);
            }
            if (metadataList != null) {
                for (int i = index + 1, metadataListSize = metadataList.size(); i < metadataListSize; i++) {
                    WrappedWatchableObject watchableObject = metadataList.get(i);
                    Object getValue = watchableObject.getValue();
                    Object o = processObject(packetEvent, user, getValue);
                    if (o == this) {
                        return packet;
                    } else if (o != null && o != getValue) {
                        if (clone) {
                            PacketContainer cloned = clonePacket(packet);
                            processPacket(packetEvent, user, cloned, o, i);
                            return cloned;
                        }
                        watchableObject.setValue(o);
                    }
                }
            }
        }
        return packet;
    }

    private PacketContainer clonePacket(PacketContainer packet) {
        try {
            return packet.deepClone();
        } catch (RuntimeException e) {
            if (exceptionTimes < ProtocolStringReplacer.getInstance().getConfigManager().protocolLibSideStackPrintCount) {
                ProtocolStringReplacer.warn("Exception which is a ProtocolLib side problem: " + e);
                ProtocolStringReplacer.warn("Please update your ProtocolLib to the latest (development) version.");
                exceptionTimes++;
            }
            return null;
        }
    }

    private Object processObject(PacketEvent packetEvent, PsrUser user, Object object) {
        if (object instanceof Optional<?>) {
            // Name of the entity
            Optional<?> value = (Optional<?>) object;
            if (value.isPresent()) {
                Object get = value.get();
                if (!(get instanceof WrappedChatComponent)) {
                    return null;
                }
                WrappedChatComponent wrappedChatComponent = (WrappedChatComponent) get;

                String json = wrappedChatComponent.getJson();
                String replacedJson = getReplacedJson(packetEvent, user, listenType, json, filter);
                if (replacedJson != null) {
                    if (json.equals(replacedJson)) {
                        return null;
                    }
                    wrappedChatComponent.setJson(replacedJson);
                    return Optional.of(wrappedChatComponent.getHandle());
                } else {
                    return this;
                }
            }

        } else if (object instanceof WrappedChatComponent) {
            // Name of the entity
            WrappedChatComponent wrappedChatComponent = (WrappedChatComponent) object;
            String json = wrappedChatComponent.getJson();
            String replacedJson = getReplacedJson(packetEvent, user, listenType, json, filter);
            if (replacedJson != null) {
                if (json.equals(replacedJson)) {
                    return null;
                }
                wrappedChatComponent.setJson(replacedJson);
                return wrappedChatComponent;
            } else {
                return this;
            }

        } else if (object instanceof String) {
            // Name of the entity CONFIRMED ON SPIGOT 1.12.2
            String replacedText = getReplacedText(packetEvent, user, listenType, (String) object, filter);
            return replacedText == null ? this : replacedText.equals(object) ? null : replacedText;

        } else if (object instanceof ItemStack) {
            // Item in Item Frame
            ItemStack itemStack = (ItemStack) object;
            List<ReplacerConfig> replacerConfigs = ProtocolStringReplacer.getInstance().getReplacerManager().getAcceptedReplacers(user, filter);
            return replaceItemStack(packetEvent, user, listenType, itemStack, replacerConfigs, replacerConfigs, replacerConfigs, true);

//        } else if (ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().isInstance(object)) {
//            NBTContainer container = new NBTContainer(object);
//            return null;
        }
        return null;
    }

}
