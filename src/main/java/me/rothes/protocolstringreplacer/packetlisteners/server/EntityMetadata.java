package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public final class EntityMetadata extends AbstractServerPacketListener {

    private final Class<?> CHAT_BASE_COMPONENT = MinecraftReflection.getIChatBaseComponentClass();
    private final EquivalentConverter<ItemStack> ITEMSTACK_CONVERTER = BukkitConverters.getItemStackConverter();

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
        PacketContainer processed = processPacket(packetEvent, user, packet, true);
        if (packet != processed) {
            packetEvent.setPacket(processed);
        }
    }

    private PacketContainer processPacket(PacketEvent packetEvent, PsrUser user, PacketContainer packet, boolean clone) {
        if (shouldDV) {
            List<WrappedDataValue> dataValueList = packet.getDataValueCollectionModifier().read(0);
            for (WrappedDataValue wrappedDataValue : dataValueList) {
                Object getValue = wrappedDataValue.getValue();
                Object o = processObject(packetEvent, user, getValue);
                if (o == this) {
                    return packet;
                } else if (o != null && o != getValue) {
                    if (clone) {
                        PacketContainer cloned = clonePacket(packet);
                        processPacket(packetEvent, user, cloned, false);
                        return cloned;
                    }
                    wrappedDataValue.setValue(o);
                }
            }

        } else {
            List<WrappedWatchableObject> metadataList = packet.getWatchableCollectionModifier().read(0);

            if (metadataList != null) {
                for (WrappedWatchableObject watchableObject : metadataList) {
                    Object getValue = watchableObject.getValue();
                    Object o = processObject(packetEvent, user, getValue);
                    if (o == this) {
                        return packet;
                    } else if (o != null && o != getValue) {
                        if (clone) {
                            PacketContainer cloned = clonePacket(packet);
                            processPacket(packetEvent, user, cloned, false);
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
                WrappedChatComponent wrappedChatComponent;
                if (CHAT_BASE_COMPONENT.isInstance(get)) {
                    // Legacy
                    wrappedChatComponent = WrappedChatComponent.fromHandle(get);
                } else if (get instanceof WrappedChatComponent) {
                    // New
                    wrappedChatComponent = (WrappedChatComponent) get;
                } else {
                    return null;
                }

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

        } else if (CHAT_BASE_COMPONENT.isInstance(object)) {
            // Name of the entity
            WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromHandle(object);
            String json = wrappedChatComponent.getJson();
            String replacedJson = getReplacedJson(packetEvent, user, listenType, json, filter);
            if (replacedJson != null) {
                if (json.equals(replacedJson)) {
                    return null;
                }
                wrappedChatComponent.setJson(replacedJson);
                return wrappedChatComponent.getHandle();
            } else {
                return this;
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
            return replacedText == null ? this : replacedText.equals(object) ? EQUAL : replacedText;

        } else if (object instanceof ItemStack) {
            // Item in Item Frame
            ItemStack itemStack = ITEMSTACK_CONVERTER.getSpecific(object);
            List<ReplacerConfig> replacerConfigs = ProtocolStringReplacer.getInstance().getReplacerManager().getAcceptedReplacers(user, filter);
            replaceItemStack(packetEvent, user, listenType, itemStack, replacerConfigs, replacerConfigs, replacerConfigs, true);

//        } else if (ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().isInstance(object)) {
//            NBTContainer container = new NBTContainer(object);
//            return null;
        }
        return null;
    }

}
