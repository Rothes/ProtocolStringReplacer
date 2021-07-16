package me.Rothes.ProtocolStringReplacer.PacketListeners.Server.Sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.User.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.List;

public class UpdateSign extends AbstractServerSignPacketListener {

    public UpdateSign() {
        super(PacketType.Play.Server.UPDATE_SIGN);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            PacketContainer packet = packetEvent.getPacket();
            User user = getEventUser(packetEvent);
            Object[] read = (Object[]) packet.getModifier().read(2);
            for (int i = 0; i < read.length; i++) {
                BaseComponent[] baseComponents = ComponentSerializer.parse(BukkitConverters.getWrappedChatComponentConverter().getSpecific(read[i]).getJson());
                for (int i1 = 0; i1 < baseComponents.length; i1++) {
                    BaseComponent baseComponent = baseComponents[i1];
                    List<BaseComponent> extra = baseComponent.getExtra();
                    if (extra != null) {
                        TextComponent text = (TextComponent) extra.get(0);
                        text.setText(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(text.getText(), user, filter));
                    }
                    baseComponents[i1] = baseComponent;
                }
                read[i] = BukkitConverters.getWrappedChatComponentConverter().getGeneric(
                        WrappedChatComponent.fromJson(ComponentSerializer.toString(baseComponents))
                );
            }
        }
    };

}