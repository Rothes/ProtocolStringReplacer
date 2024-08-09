package io.github.rothes.protocolstringreplacer.nms.v1_19_1.packetreader

import io.github.rothes.protocolstringreplacer.nms.packetreader.ChatType
import io.github.rothes.protocolstringreplacer.nms.packetreader.IPacketReader
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket
import net.minecraft.server.MinecraftServer

class PacketReader: IPacketReader {

    private val chatTypes = with(
        MinecraftServer.getServer().registryAccess().registryOrThrow(Registry.CHAT_TYPE_REGISTRY) as MappedRegistry
    ) {
        registryKeySet()
            .sortedBy {
                getId(this.get(it))
            }
            .map { it.location().path }
            .map { ChatType.entries.find { type -> type.keys.contains(it) }!! }
            .toTypedArray()
    }

    override fun readChatType(packet: ClientboundPlayerChatPacket): ChatType {
        return chatTypes[packet.typeId]
    }

}