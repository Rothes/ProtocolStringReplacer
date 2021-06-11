package me.Rothes.ProtocolStringReplacer.PacketListeners.Client;

import com.comphenix.protocol.PacketType;
import me.Rothes.ProtocolStringReplacer.PacketListeners.AbstractPacketListener;
import me.Rothes.ProtocolStringReplacer.Replacer.ReplacerFile;
import me.Rothes.ProtocolStringReplacer.User.User;

import java.util.function.BiPredicate;

public class AbstractClientPacketListener extends AbstractPacketListener {

    protected AbstractClientPacketListener(PacketType packetType) {
        super(packetType);
    }

}
