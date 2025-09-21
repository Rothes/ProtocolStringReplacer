@file:Suppress("DEPRECATION")

package io.github.rothes.protocolstringreplacer.replacer.containers

import de.tr7zw.nbtapi.NBT
import io.github.rothes.protocolstringreplacer.plugin
import net.md_5.bungee.api.chat.ItemTag
import net.md_5.bungee.api.chat.hover.content.Item

open class ItemContentContainer(item: Item, root: Container<*>?) : AbstractContainer<Item>(item, root) {

    private val container: ItemStackContainer?

    init {
        if (content.tag != null && content.id != "minecraft:air") {
            val nbt = NBT.createNBTObject()
            nbt.setString("id", content.id)
            if (NEW_NBT) {
                nbt.setInteger("count", content.count)
                nbt.getOrCreateCompound("components").mergeCompound(NBT.parseNBT(content.tag.nbt))
            } else {
                nbt.setInteger("Count", content.count)
                nbt.getOrCreateCompound("tag").mergeCompound(NBT.parseNBT(content.tag.nbt))
            }
            val itemStack = NBT.itemStackFromNBT(nbt)!!
            container = ItemStackContainer(itemStack, false, root)
        } else {
            container = null
        }
    }

    override fun createDefaultChildren() {
        if (container != null) {
            children.add(container)
        }
        super.createDefaultChildren()
    }

    override fun createTexts(root: Container<*>) {
        if (container != null) {
            container.entriesPeriod()
            container.createDefaultChildrenDeep()
            container.createTexts(root)
        }
    }

    override fun getResult(): Item {
        if (container != null) {
            val itemStack = container.result
            val nbt = NBT.itemStackToNBT(itemStack)
            content.tag = ItemTag.ofNbt(if (NEW_NBT) nbt.getCompound("components").toString() else nbt.getCompound("tag").toString())
        }
        return content
    }

    companion object {
        private val NEW_NBT = plugin.serverMajorVersion == 20.toByte() && plugin.serverMinorVersion >= 5 || plugin.serverMajorVersion > 20
    }

}