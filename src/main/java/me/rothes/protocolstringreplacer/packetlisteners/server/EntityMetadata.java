package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.packetwrapper.WrapperPlayServerEntityMetadata;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.user.User;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.List;
import java.util.Optional;

public final class EntityMetadata extends AbstractServerPacketListener {

    public EntityMetadata() {
        super(PacketType.Play.Server.ENTITY_METADATA, ListenType.ENTITY);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            PacketContainer packet = packetEvent.getPacket();
            User user = getEventUser(packetEvent);
            WrapperPlayServerEntityMetadata wrapperPlayServerEntityMetadata = new WrapperPlayServerEntityMetadata(packet.deepClone());
            List<WrappedWatchableObject> metadataList = wrapperPlayServerEntityMetadata.getMetadata();

            if (metadataList != null) {
                for (WrappedWatchableObject watchableObject : metadataList) {
                    if (watchableObject.getIndex() == 2) {
                        Optional<?> value = (Optional<?>) watchableObject.getValue();
                        if (value.isPresent()) {
                            WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromHandle(value.get());
                            if (wrappedChatComponent != null) {
                                wrappedChatComponent.setJson(ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                                        .getReplacedComponents(ComponentSerializer.parse(wrappedChatComponent.getJson()), user, filter)));
                                watchableObject.setValue(Optional.of(wrappedChatComponent.getHandle()));
                                packetEvent.setPacket(wrapperPlayServerEntityMetadata.getHandle());
                            }
                        }
                    }
                }
            }

        }
    };

}
