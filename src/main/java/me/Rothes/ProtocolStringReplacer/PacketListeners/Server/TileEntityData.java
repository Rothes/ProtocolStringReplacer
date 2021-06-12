package me.Rothes.ProtocolStringReplacer.PacketListeners.Server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.User.User;

public class TileEntityData extends AbstractServerPacketListener{

    public TileEntityData() {
        super(PacketType.Play.Server.TILE_ENTITY_DATA);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            PacketContainer packet = packetEvent.getPacket();
            User user = getEventUser(packetEvent);
            // 9: Set the text on a sign
            if (packet.getIntegers().read(0) == 9) {
                NbtCompound nbtBase = (NbtCompound) packet.getNbtModifier().read(0);
                nbtBase.put("Text1", legacyTextToJson(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(
                        jsonToLegacyText(nbtBase.getString("Text1")), user, filter)));
                nbtBase.put("Text2", legacyTextToJson(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(
                        jsonToLegacyText(nbtBase.getString("Text2")), user, filter)));
                nbtBase.put("Text3", legacyTextToJson(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(
                        jsonToLegacyText(nbtBase.getString("Text3")), user, filter)));
                nbtBase.put("Text4", legacyTextToJson(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(
                        jsonToLegacyText(nbtBase.getString("Text4")), user, filter)));
            }
        }
    };

}
