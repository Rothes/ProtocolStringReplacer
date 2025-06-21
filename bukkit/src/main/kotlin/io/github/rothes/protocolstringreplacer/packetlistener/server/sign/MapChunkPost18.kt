package io.github.rothes.protocolstringreplacer.packetlistener.server.sign

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import de.tr7zw.nbtapi.NBTContainer
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData
import net.minecraft.world.level.block.entity.BlockEntityType
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class MapChunkPost18 : BaseServerSignPacketListener(PacketType.Play.Server.MAP_CHUNK) {

    private val chunkData: Field
    private val blockEntitiesData: Field
    private val type: Field
    private val tag: Field

    init {
        val clazz = ClientboundLevelChunkPacketData::class.java
        chunkData = PacketType.Play.Server.MAP_CHUNK.packetClass.declaredFields.firstOrThrow("chunkData") { it.type == clazz }
        with(clazz) {
            blockEntitiesData = declaredFields.firstOrThrow("blockEntitiesData") { it.type.isAssignableFrom(List::class.java) }
            val blockEntityInfo = declaredClasses.firstOrThrow("blockEntityInfo") { !it.isInterface && it.modifiers == Modifier.STATIC }
            type = blockEntityInfo.declaredFields.firstOrThrow("type") { it.type == BlockEntityType::class.java }
            tag = blockEntityInfo.declaredFields.firstOrThrow("tag") { it.type == CompoundTag::class.java }
        }
        chunkData.isAccessible = true
        blockEntitiesData.isAccessible = true
        type.isAccessible = true
        tag.isAccessible = true
    }

    override fun process(packetEvent: PacketEvent) {
        val user = getEventUser(packetEvent) ?: return
        val packet = packetEvent.packet
        for (blockEntityInfo in blockEntitiesData[chunkData[packet.handle]] as List<*>) {
            if (TileTypeHelper.isSignType(type[blockEntityInfo])) {
                replaceSign(packetEvent, NBTContainer(tag[blockEntityInfo] ?: continue), user, filter)
            }
        }
    }

    private inline fun <T> Array<T>.firstOrThrow(msg: String, predicate: (T) -> Boolean): T {
        for (element in this) if (predicate(element)) return element
        throw NoSuchElementException(msg)
    }
}