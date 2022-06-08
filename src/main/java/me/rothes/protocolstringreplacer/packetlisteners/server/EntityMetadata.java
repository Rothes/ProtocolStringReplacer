package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.libs.com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public final class EntityMetadata extends AbstractServerPacketListener {

    public byte exceptionTimes = 0;

    public EntityMetadata() {
        super(PacketType.Play.Server.ENTITY_METADATA, ListenType.ENTITY);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();
        WrapperPlayServerEntityMetadata wrapperPlayServerEntityMetadata;
        try {
            if (packet.getEntityModifier(packetEvent).read(0) == null) {
                return;
            }
            wrapperPlayServerEntityMetadata = new WrapperPlayServerEntityMetadata(packet.deepClone());
        } catch (RuntimeException e) {
            if (exceptionTimes < ProtocolStringReplacer.getInstance().getConfigManager().protocollibSideStackPrintCount) {
                ProtocolStringReplacer.warn("Exception which may be a ProtocolLib side problem:", e);
                exceptionTimes++;
            }
            return;
        }
        List<WrappedWatchableObject> metadataList = wrapperPlayServerEntityMetadata.getMetadata();

        if (metadataList != null) {
            for (WrappedWatchableObject watchableObject : metadataList) {
                Object getValue = watchableObject.getValue();
                if (getValue instanceof Optional<?>) {
                    // Name of the entity
                    Optional<?> value = (Optional<?>) getValue;
                    if (value.isPresent() && MinecraftReflection.getIChatBaseComponentClass().isInstance(value.get())) {
                        WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromHandle(value.get());
                        if (wrappedChatComponent != null) {
                            String replacedJson = getReplacedJson(packetEvent, user, listenType, wrappedChatComponent.getJson(), filter);
                            if (replacedJson != null) {
                                wrappedChatComponent.setJson(replacedJson);
                                watchableObject.setValue(Optional.of(wrappedChatComponent.getHandle()));
                            } else {
                                return;
                            }
                        }
                    }

                } else if (BukkitConverters.getItemStackConverter().getSpecificType().isInstance(getValue)) {
                    // Item in Item Frame
                    ItemStack itemStack = BukkitConverters.getItemStackConverter().getSpecific(getValue);
                    replaceItemStack(packetEvent, user, listenType, itemStack, filter);
                }
                packetEvent.setPacket(wrapperPlayServerEntityMetadata.getHandle());
            }
        }
    }

}
