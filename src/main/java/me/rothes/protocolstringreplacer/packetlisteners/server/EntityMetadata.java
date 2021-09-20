package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.rothes.protocolstringreplacer.PSRLocalization;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.packetwrapper.WrapperPlayServerEntityMetadata;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.user.User;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public final class EntityMetadata extends AbstractServerPacketListener {

    public EntityMetadata() {
        super(PacketType.Play.Server.ENTITY_METADATA, ListenType.ENTITY);
    }

    protected void process(PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        Entity entity;
        try {
            entity = packet.getEntityModifier(packetEvent).read(0);
        } catch (IllegalArgumentException exception) {
            ProtocolStringReplacer.error(PSRLocalization.getLocaledMessage(
                    "Console.Messages.Packet-Listener.Entity-Metadata.Cannot-Read-Entity.Line-1"));
            ProtocolStringReplacer.error(PSRLocalization.getLocaledMessage(
                    "Console.Messages.Packet-Listener.Entity-Metadata.Cannot-Read-Entity.Line-2"));
            ProtocolStringReplacer.error(PSRLocalization.getLocaledMessage(
                    "Console.Messages.Packet-Listener.Entity-Metadata.Cannot-Read-Entity.Line-3"));
            return;
        }
        if (entity == null) {
            return;
        }
        User user = getEventUser(packetEvent);
        WrapperPlayServerEntityMetadata wrapperPlayServerEntityMetadata;
        try {
            wrapperPlayServerEntityMetadata = new WrapperPlayServerEntityMetadata(packet.deepClone());
        } catch (RuntimeException e) {
            return;
        }
        List<WrappedWatchableObject> metadataList = wrapperPlayServerEntityMetadata.getMetadata();

        if (metadataList != null) {
            for (WrappedWatchableObject watchableObject : metadataList) {
                if (watchableObject.getValue() instanceof Optional<?>) {
                    Optional<?> value = (Optional<?>) watchableObject.getValue();
                    if (value.isPresent() && MinecraftReflection.getIChatBaseComponentClass().isInstance(value.get())) {
                        WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromHandle(value.get());
                        if (wrappedChatComponent != null) {
                            String replacedJson = getReplacedJson(packetEvent, user, wrappedChatComponent.getJson(), filter);
                            if (replacedJson != null) {
                                wrappedChatComponent.setJson(replacedJson);
                                watchableObject.setValue(Optional.of(wrappedChatComponent.getHandle()));
                            } else {
                                return;
                            }
                        }
                    }
                } else if (BukkitConverters.getItemStackConverter().getSpecificType().isInstance(watchableObject.getValue())) {
                    Object value = watchableObject.getValue();
                    if (BukkitConverters.getItemStackConverter().getSpecificType().isInstance(value)) {
                        ItemStack itemStack = BukkitConverters.getItemStackConverter().getSpecific(value);
                        replacedItemStack(packetEvent, user, itemStack, filter);
                    }
                }
                packetEvent.setPacket(wrapperPlayServerEntityMetadata.getHandle());
            }
        }
    }

}
