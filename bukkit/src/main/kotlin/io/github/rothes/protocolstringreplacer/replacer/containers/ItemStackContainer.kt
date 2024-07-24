package io.github.rothes.protocolstringreplacer.replacer.containers

import de.tr7zw.changeme.nbtapi.NBT
import de.tr7zw.changeme.nbtapi.NBTContainer
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTList
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer
import io.github.rothes.protocolstringreplacer.plugin
import io.github.rothes.protocolstringreplacer.replacer.ReplacerManager.ItemMetaCache
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class ItemStackContainer @JvmOverloads constructor(itemStack: ItemStack, useCache: Boolean = true, root: Container<*>? = null) :
    AbstractContainer<ItemStack>(itemStack, root) {

    val isFromCache: Boolean
    lateinit var metaCache: ItemMetaCache
        private set
    private var nbt: ReadWriteNBT
    private val original: ItemMeta = content.itemMeta

    init {
        if (useCache) {
            val replacerManager = ProtocolStringReplacer.getInstance().replacerManager

            val getCache = replacerManager.getReplacedItemCache(original, itemType)
            if (getCache != null) {
                metaCache = getCache
                isFromCache = true
                nbt = metaCache.nbt
            } else {
                isFromCache = false
                nbt = NBT.itemStackToNBT(content)
                metaCache = replacerManager.addReplacedItemCache(original, nbt, itemType, false, IntArray(0))
            }
        } else {
            isFromCache = false
            nbt = NBT.itemStackToNBT(content)
        }
    }

    fun cloneItem() {
        nbt = NBT.itemStackToNBT(NBT.itemStackFromNBT(nbt))
    }

    override fun createDefaultChildren() {
        nbtPeriod()
    }

    private fun nbtPeriod() {
        children.add(object : ChatJsonContainer(nbt.toString(), root, false) {
            override fun getResult(): String {
                val result = super.getResult()
                nbt.clearNBT()
                nbt.mergeCompound(NBTContainer(result))
                return result
            }
        })
    }

    fun displayPeriod() {
        children.clear()
        jsonReplaceables.clear()
        val display = displayNbt
        children.add(object : ChatJsonContainer(display?.toString() ?: "{}", root, false) {
            override fun getResult(): String {
                val result = super.getResult()
                if (display != null) {
                    display.clearNBT()
                    display.mergeCompound(NBT.parseNBT(result))
                } else if (result != "{}") {
                    createDisplayNbt().mergeCompound(NBT.parseNBT(result))
                }
                return result
            }
        })
    }

    fun entriesPeriod() {
        children.clear()
        if (jsonReplaceables != null) jsonReplaceables.clear()
        val display = displayNbt
        if (display != null) {
            if (display.hasTag("Name")) {
                if (NAME_JSON) {
                    children.add(object : ChatJsonContainer(display.getString("Name"), root, true) {
                        override fun getResult(): String {
                            val result = super.getResult()
                            display.setString("Name", result)
                            if (NEW_NBT) {
                                nbt.getCompound("components")!!.setString("minecraft:custom_name", result)
                            }
                            return result
                        }
                    })
                } else {
                    children.add(object : SimpleTextContainer(display.getString("Name"), root) {
                        override fun getResult(): String {
                            val result = super.getResult()
                            display.setString("Name", result)
                            return result
                        }
                    })
                }
            }
            if (display.hasTag("Lore")) {
                if (LORE_JSON) {
                    addJsonList(display.getStringList("Lore"),
                        if (NEW_NBT) nbt.getCompound("components")!!.getStringList("minecraft:lore") else null)
                } else {
                    addTextList(display.getStringList("Lore"))
                }
            }
        }

        val type = content.type
        if (type == WRITABLE_BOOK || type == Material.WRITTEN_BOOK) {
            if (nbt.hasTag("author")) {
                children.add(object : SimpleTextContainer(nbt.getString("author"), root) {
                    override fun getResult(): String {
                        val result = super.getResult()
                        nbt.setString("author", result)
                        return result
                    }
                })
            }
            if (nbt.hasTag("title")) {
                children.add(object : SimpleTextContainer(nbt.getString("title"), root) {
                    override fun getResult(): String {
                        val result = super.getResult()
                        nbt.setString("title", result)
                        return result
                    }
                })
            }
            if (nbt.hasTag("pages")) {
                if (type == Material.WRITTEN_BOOK) {
                    addJsonList(nbt.getStringList("pages"))
                } else {
                    addTextList(nbt.getStringList("pages"))
                }
            }
        }
    }

    fun createDefaultChildrenDeep() {
        super.createDefaultChildren()
    }

    private fun addJsonList(list: ReadWriteNBTList<String>, copy: ReadWriteNBTList<String>? = null) {
        val size = list.size()
        for (line in 0 until size) {
            children.add(object : ChatJsonContainer(list[line], root, true) {
                override fun getResult(): String {
                    val result = super.getResult()
                    list[line] = result
                    copy?.set(line, result)
                    return result
                }
            })
        }
    }

    private fun addTextList(list: ReadWriteNBTList<String>) {
        val size = list.size()
        for (line in 0 until size) {
            children.add(object : SimpleTextContainer(list[line], root) {
                override fun getResult(): String {
                    val result = super.getResult()
                    list[line] = result
                    return result
                }
            })
        }
    }

    override fun getResult(): ItemStack {
        super.getResult()
        val replaced = NBT.itemStackFromNBT(nbt)!!
        content.setItemMeta(replaced.itemMeta)
        content = replaced
        return content
    }

    fun restoreItem() {
        content.setItemMeta(original)
        nbt.clearNBT()
        nbt.mergeCompound(NBT.itemStackToNBT(content))
    }

    val nbtString: String
        get() = nbt.toString()

    val itemType: Material
        get() = content.type

    private val displayNbt: ReadWriteNBT?
        get() = (if (NEW_NBT) nbt.getCompound("components")
            ?.getCompound("minecraft:custom_data") else nbt.getCompound("tag"))?.getCompound("display")
    private fun createDisplayNbt(): ReadWriteNBT {
        return (if (NEW_NBT) nbt.getOrCreateCompound("components")
            .getOrCreateCompound("minecraft:custom_data") else nbt.getOrCreateCompound("tag")).getOrCreateCompound("display")
    }

    companion object {

        private val NAME_JSON = plugin.serverMajorVersion >= 13
        private val LORE_JSON = plugin.serverMajorVersion >= 14
        private val NEW_NBT = plugin.serverMajorVersion == 20.toByte() && plugin.serverMinorVersion >= 5 || plugin.serverMajorVersion > 20
        private val WRITABLE_BOOK: Material = try {
            Material.valueOf("BOOK_AND_QUILL")
        } catch (e: IllegalArgumentException) {
            Material.WRITABLE_BOOK
        }
    }
}
