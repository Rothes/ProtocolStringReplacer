package io.github.rothes.protocolstringreplacer.nms.packetreader;

import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;

public interface IPacketReader {

    ChatType readChatType(ClientboundPlayerChatPacket packet);

}
