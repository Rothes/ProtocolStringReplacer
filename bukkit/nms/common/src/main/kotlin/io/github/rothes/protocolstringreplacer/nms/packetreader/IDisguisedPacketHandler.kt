package io.github.rothes.protocolstringreplacer.nms.packetreader

import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket
import java.lang.reflect.Field

interface IDisguisedPacketHandler {

    val displayNameField: Field
    val targetField: Field
    fun boundRecord(packet: ClientboundDisguisedChatPacket): Any
}
