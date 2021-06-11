package me.Rothes.ProtocolStringReplacer.PacketListeners.Server;

import com.comphenix.protocol.PacketType;
import me.Rothes.ProtocolStringReplacer.PacketListeners.AbstractPacketListener;
import me.Rothes.ProtocolStringReplacer.Replacer.ReplacerFile;
import me.Rothes.ProtocolStringReplacer.User.User;

import java.util.function.BiPredicate;

public class AbstractServerPacketListener extends AbstractPacketListener {

    protected final BiPredicate<ReplacerFile, User> filter;

    protected AbstractServerPacketListener(PacketType packetType) {
        super(packetType);
        filter = (replacerFile, user) -> containPacket(replacerFile);
    }

    protected final boolean containPacket(ReplacerFile replacerFile) {
        return replacerFile.getPacketTypeList().contains(packetType);
    }

}
