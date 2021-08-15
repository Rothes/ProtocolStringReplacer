package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.packetwrapper.WrapperPlayServerEntityMetadata;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.user.User;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public final class EntityMetadata extends AbstractServerPacketListener {

    public EntityMetadata() {
        super(PacketType.Play.Server.ENTITY_METADATA, ListenType.ENTITY);
    }

    protected void process(PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        Entity entity = packet.getEntityModifier(packetEvent).read(0);
        if (entity == null || (!ProtocolStringReplacer.getInstance().getConfigManager().listenDroppedItemEntity && entity.getType() == EntityType.DROPPED_ITEM)) {
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
                            wrappedChatComponent.setJson(ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                                    .getReplacedComponents(ComponentSerializer.parse(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedJson(
                                            wrappedChatComponent.getJson(), user, filter, false)), user, filter)));
                            watchableObject.setValue(Optional.of(wrappedChatComponent.getHandle()));
                        }
                    }
                } else if (BukkitConverters.getItemStackConverter().getSpecificType().isInstance(watchableObject.getValue())) {
                    Object value = watchableObject.getValue();
                    if (BukkitConverters.getItemStackConverter().getSpecificType().isInstance(value)) {
                        ItemStack itemStack = BukkitConverters.getItemStackConverter().getSpecific(value);
                        if (itemStack.hasItemMeta()) {
                            ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedItemStack(itemStack, user, filter);
                        }
                    }
                }
                packetEvent.setPacket(wrapperPlayServerEntityMetadata.getHandle());
            }
        }
    }

}
