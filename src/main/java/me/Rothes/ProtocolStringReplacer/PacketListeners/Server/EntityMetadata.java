package me.Rothes.ProtocolStringReplacer.PacketListeners.Server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import me.Rothes.ProtocolStringReplacer.User.User;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import org.bukkit.entity.Entity;

public class EntityMetadata extends AbstractServerPacketListener {

    public EntityMetadata() {
        super(PacketType.Play.Server.ENTITY_METADATA);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            PacketContainer packet = packetEvent.getPacket();
            User user = getEventUser(packetEvent);
            StructureModifier<Entity> structureModifier = packet.getEntityModifier(packetEvent);
            Entity entity = packet.getEntityModifier(packetEvent).read(0);
            if (entity != null) {
                String name = entity.getCustomName();
                if (name != null) {
                    entity.setCustomName(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(name, user, filter));
                }
            }
        }
    };

}
