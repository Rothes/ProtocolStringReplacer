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
        Entity entity;
        try {
            entity = packet.getEntityModifier(packetEvent).read(0);
        } catch (IllegalArgumentException exception) {
            Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §c解析实体时发生异常. 这是 ProtocolLib 导致的错误, 请不要向 PSR 的作者反馈.");
            Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §c此异常一般不会导致问题. 但您也可以自行构建下方的 ProtocolLib, 来避免此异常.");
            Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §cGit 链接: https://github.com/Rothes/ProtocolLib.git");
            return;
        }
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
