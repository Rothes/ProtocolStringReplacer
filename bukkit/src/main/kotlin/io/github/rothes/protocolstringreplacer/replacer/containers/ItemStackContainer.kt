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
        val toString = nbt.toString()
        children.add(object : ChatJsonContainer(toString, root, false) {
            override fun getResult(): String {
                val result = super.getResult()
                if (result != toString) {
                    nbt.clearNBT()
                    nbt.mergeCompound(NBTContainer(result))
                }
                return result
            }
        })
    }

//    fun displayNamePeriod(): Boolean {
//        if (!NAME_JSON) {
//            // Skip if this is not json.
//            return false
//        }
//        children.clear()
//        jsonReplaceables.clear()
//        val toString = displayRoot?.getOrNull(NAME_KEY, String::class.java) ?: "{}"
//        children.add(object : ChatJsonContainer(toString, root, false) {
//            override fun getResult(): String {
//                val result = super.getResult()
//                if (result != toString) {
//                    displayRootCreated.setString(NAME_KEY, result)
//                }
//                return result
//            }
//        })
//        return true
//    }

    fun lorePeriod(): Boolean {
        children.clear()
        jsonReplaceables.clear()
        val toString = displayRoot?.getStringList(LORE_KEY)?.toString() ?: "[]"
        children.add(object : ChatJsonContainer(toString, root, false) {
            override fun getResult(): String {
                val result = super.getResult()
                if (result != toString) {
                    displayRootCreated.mergeCompound(NBTContainer("{\"$LORE_KEY\":$result}"))
                }
                return result
            }
        })
        return true
    }

    fun entriesPeriod() {
        children.clear()
        jsonReplaceables.clear()
        val display = displayRoot
        if (display != null) {
            if (display.hasTag(NAME_KEY)) {
                if (NAME_JSON) {
                    children.add(object : ChatJsonContainer(display.getString(NAME_KEY), root, true) {
                        override fun getResult(): String {
                            val result = super.getResult()
                            display.setString(NAME_KEY, result)
                            return result
                        }
                    })
                } else {
                    children.add(object : SimpleTextContainer(display.getString(NAME_KEY), root) {
                        override fun getResult(): String {
                            val result = super.getResult()
                            display.setString(NAME_KEY, result)
                            return result
                        }
                    })
                }
            }
            if (display.hasTag(LORE_KEY)) {
                if (LORE_JSON) {
                    addJsonList(display.getStringList(LORE_KEY))
                } else {
                    addTextList(display.getStringList(LORE_KEY))
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

    private fun addJsonList(list: ReadWriteNBTList<String>) {
        val size = list.size()
        for (line in 0 until size) {
            children.add(object : ChatJsonContainer(list[line], root, true) {
                override fun getResult(): String {
                    val result = super.getResult()
                    list[line] = result
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

    fun childrenResult() {
        super.getResult()
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

    private val tag = nbt.path(TAG_PATH)
    private val tagCreated = nbt.create(TAG_PATH)
    private val displayRoot = tag?.path(DISPLAY_PATH)
    private val displayRootCreated = tagCreated.create(DISPLAY_PATH)

    private fun ReadWriteNBT.path(path: Array<String>): ReadWriteNBT? {
        var nbt = this
        for (key in path) {
            nbt = nbt.getCompound(key) ?: return null
        }
        return nbt
    }
    private fun ReadWriteNBT.create(path: Array<String>): ReadWriteNBT {
        var nbt = this
        for (key in path) {
            nbt = nbt.getOrCreateCompound(key)
        }
        return nbt
    }

    companion object {

        private val NAME_JSON = plugin.serverMajorVersion >= 13
        private val LORE_JSON = plugin.serverMajorVersion >= 14
        private val NEW_NBT = plugin.serverMajorVersion == 20.toByte() && plugin.serverMinorVersion >= 5 || plugin.serverMajorVersion > 20
        private val TAG_PATH = if (NEW_NBT) arrayOf("components") else arrayOf("tag")
        private val DISPLAY_PATH = if (NEW_NBT) arrayOf() else arrayOf("display")
        private val NAME_KEY = if (NEW_NBT) "minecraft:custom_name" else "Name"
        private val LORE_KEY = if (NEW_NBT) "minecraft:lore" else "Lore"
        private val WRITABLE_BOOK: Material = try {
            Material.valueOf("BOOK_AND_QUILL")
        } catch (e: IllegalArgumentException) {
            Material.WRITABLE_BOOK
        }
    }
}
