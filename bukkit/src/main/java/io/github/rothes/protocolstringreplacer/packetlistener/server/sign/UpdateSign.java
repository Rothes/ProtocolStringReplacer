package io.github.rothes.protocolstringreplacer.packetlistener.server.sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.server.BaseServerPacketListener;

public class UpdateSign extends BaseServerSignPacketListener {

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
            String replaced = BaseServerPacketListener.getReplacedJson(packetEvent, user, listenType,
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
