package io.github.rothes.protocolstringreplacer.nms.generic.packetreader

import io.github.rothes.protocolstringreplacer.nms.packetreader.ChatType
import io.github.rothes.protocolstringreplacer.nms.packetreader.IPacketReader
import net.minecraft.core.MappedRegistry
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket
import net.minecraft.server.MinecraftServer

class PacketReader: IPacketReader {

    private val registry = MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.CHAT_TYPE)
    private val chatTypes = with(registry as MappedRegistry) {
        registryKeySet()
            .sortedBy {
                getId(this.get(it))
            }
            .map { it.location().path }
            .map { ChatType.entries.find { type -> type.keys.contains(it) }!! }
            .toTypedArray()
    }

    override fun readChatType(packet: ClientboundPlayerChatPacket): ChatType {
        return chatTypes[registry.getId(packet.chatType.chatType.value())]
    }

}