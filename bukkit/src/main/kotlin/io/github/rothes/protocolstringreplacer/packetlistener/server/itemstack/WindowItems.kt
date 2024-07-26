package io.github.rothes.protocolstringreplacer.packetlistener.server.itemstack

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.BukkitConverters
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer
import io.github.rothes.protocolstringreplacer.get
import io.github.rothes.protocolstringreplacer.set
import org.bukkit.Material

class WindowItems : BaseServerItemPacketListener(PacketType.Play.Server.WINDOW_ITEMS) {

    override fun process(packetEvent: PacketEvent) {
        val user = getEventUser(packetEvent) ?: return
        user.clearUserMetaCache()
        val replacerManager = ProtocolStringReplacer.getInstance().replacerManager
        val nbt = replacerManager.getAcceptedReplacers(user, itemNbtFilter)
        val lore = replacerManager.getAcceptedReplacers(user, itemLoreFilter)
        val entries = replacerManager.getAcceptedReplacers(user, itemEntriesFilter)

        var saveMeta = !user.isInAnvil
        packetEvent.packet.modifier[1] = (packetEvent.packet.modifier[1] as Array<*>).map {
            BukkitConverters.getItemStackConverter().getSpecific(it)
        }.map { itemStack ->
            if (itemStack.type == Material.AIR) {
                saveMeta = true
                return@map itemStack
            }
            replaceItemStack(packetEvent, user, listenType, itemStack, nbt, lore, entries, saveMeta).also {
                saveMeta = true
            } ?: return
        }.map {
            BukkitConverters.getItemStackConverter().getGeneric(it)
        }.toTypedArray()
    }
}
