package io.github.rothes.protocolstringreplacer.packetlistener.server.itemstack

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer
import io.github.rothes.protocolstringreplacer.get
import io.github.rothes.protocolstringreplacer.set
import org.bukkit.Material

class WindowItemsPost11 : BaseServerItemPacketListener(PacketType.Play.Server.WINDOW_ITEMS) {

    override fun process(packetEvent: PacketEvent) {
        val user = getEventUser(packetEvent) ?: return
        user.clearUserItemRestoreCache()
        val replacerManager = ProtocolStringReplacer.getInstance().replacerManager
        val nbt = replacerManager.getAcceptedReplacers(user, itemNbtFilter)
        val lore = replacerManager.getAcceptedReplacers(user, itemLoreFilter)
        val entries = replacerManager.getAcceptedReplacers(user, itemEntriesFilter)

        val itemListModifier = packetEvent.packet.itemListModifier
        var saveMeta = !user.isInAnvil
        itemListModifier[0] = itemListModifier[0].map { itemStack ->
            if (itemStack.type == Material.AIR) {
                saveMeta = true
                return@map itemStack
            }
            replaceItemStack(packetEvent, user, listenType, itemStack, nbt, lore, entries, saveMeta).also {
                saveMeta = true
            } ?: return
        }
        val itemModifier = packetEvent.packet.itemModifier
        if (itemModifier.size() != 0) {
            // Since 1.17.1
            val itemStack = itemModifier[0]
            if (itemStack.type == Material.AIR) {
                return
            }
            itemModifier[0] = replaceItemStack(packetEvent, user, listenType, itemStack, nbt, lore, entries, true) ?: return
        }
    }
}
