package io.github.rothes.protocolstringreplacer.nms.generic.packetreader

import io.github.rothes.protocolstringreplacer.nms.packetreader.ChatType
import io.github.rothes.protocolstringreplacer.nms.packetreader.IPacketReader
import net.minecraft.core.MappedRegistry
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket
import net.minecraft.server.MinecraftServer

class PacketReader: IPacketReader {

    private val chatTypes = with(MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.CHAT_TYPE) as MappedRegistry) {
        registryKeySet()
            .associateWith { it.location().path }
            .entries
            .sortedBy {
                getId(this.get(it.key))
            }
            .map { ChatType.entries.find { type -> type.keys.contains(it.value) }!! }
            .toTypedArray()
    }

    override fun readChatType(packet: ClientboundPlayerChatPacket): ChatType {
        return chatTypes[packet.chatType.chatType]
    }

}