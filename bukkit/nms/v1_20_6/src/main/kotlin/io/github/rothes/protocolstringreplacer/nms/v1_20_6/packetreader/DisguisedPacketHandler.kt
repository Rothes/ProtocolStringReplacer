package io.github.rothes.protocolstringreplacer.nms.v1_20_6.packetreader

import net.minecraft.network.chat.ChatType
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket
import io.github.rothes.protocolstringreplacer.nms.packetreader.IDisguisedPacketHandler
import java.lang.reflect.Field
import java.util.Optional

class DisguisedPacketHandler: IDisguisedPacketHandler {

    override val displayNameField: Field = ChatType.Bound::class.java.declaredFields.first { it.type == Component::class.java }
    override val targetField: Field = ChatType.Bound::class.java.declaredFields.first { it.type == Optional::class.java }

    override fun boundRecord(packet: ClientboundDisguisedChatPacket): Any {
        return packet.chatType
    }
}