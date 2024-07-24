package io.github.rothes.protocolstringreplacer.packetlistener.server.itemstack

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer
import io.github.rothes.protocolstringreplacer.get
import io.github.rothes.protocolstringreplacer.set
import org.bukkit.Material

class WindowItemsPost11 : AbstractServerItemPacketListener(PacketType.Play.Server.WINDOW_ITEMS) {

    override fun process(packetEvent: PacketEvent) {
        val user = getEventUser(packetEvent) ?: return
        user.clearUserMetaCache()
        val replacerManager = ProtocolStringReplacer.getInstance().replacerManager
        val nbt = replacerManager.getAcceptedReplacers(user, itemNbtFilter)
        val display = replacerManager.getAcceptedReplacers(user, itemDisplayFilter)
        val entries = replacerManager.getAcceptedReplacers(user, itemEntriesFilter)

        val itemListModifier = packetEvent.packet.itemListModifier
        var saveMeta = !user.isInAnvil
        itemListModifier[0] = itemListModifier[0].map { itemStack ->
            if (itemStack.type == Material.AIR) {
                saveMeta = true
                return@map itemStack
            }
            replaceItemStack(packetEvent, user, listenType, itemStack, nbt, display, entries, saveMeta).also {
                saveMeta = true
            } ?: return
        }
    }
}
