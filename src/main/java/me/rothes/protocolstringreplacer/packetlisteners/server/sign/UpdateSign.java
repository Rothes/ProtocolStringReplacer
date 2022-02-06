package me.rothes.protocolstringreplacer.packetlisteners.server.sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.api.user.PsrUser;

public class UpdateSign extends AbstractServerSignPacketListener {

    public UpdateSign() {
        super(PacketType.Play.Server.UPDATE_SIGN);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();
        Object[] read = (Object[]) packet.getModifier().read(2);
        for (int i = 0; i < read.length; i++) {
            String replaced = getReplacedJson(packetEvent, user, listenType,
                    BukkitConverters.getWrappedChatComponentConverter().getSpecific(read[i]).getJson(), filter);
            if (replaced != null) {
                read[i] = BukkitConverters.getWrappedChatComponentConverter().getGeneric(
                        WrappedChatComponent.fromJson(replaced));
            } else {
                return;
            }
        }
    }

}
