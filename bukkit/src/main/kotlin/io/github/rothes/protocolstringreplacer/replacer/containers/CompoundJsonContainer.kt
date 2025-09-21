package io.github.rothes.protocolstringreplacer.replacer.containers

import de.tr7zw.nbtapi.NBT
import de.tr7zw.nbtapi.NBTType
import de.tr7zw.nbtapi.iface.ReadWriteNBT

class CompoundJsonContainer(
    private val compound: ReadWriteNBT,
    private val key: String,
    root: Container<*>,
): AbstractContainer<Unit>(Unit, root) {

    private val type = compound.getType(key)
    private val jsonCompound: ReadWriteNBT?
    private val original: String
    private val container: Container<String>

    init {
        when (type) {
            NBTType.NBTTagCompound -> {
                jsonCompound = compound.getCompound(key)
                original = jsonCompound.toString()
                container = ChatJsonContainer(original, root)
                children.add(container)
            }
            NBTType.NBTTagList -> {
                jsonCompound = null
                // TODO: Could be compound + string list??
                original = compound.getStringList(key).joinToString(separator = "")
                container = SimpleTextContainer(original, root)
                children.add(container)
            }
            NBTType.NBTTagString -> {
                jsonCompound = null
                original = compound.getString(key)
                container = SimpleTextContainer(original, root)
                children.add(container)
            }
            else -> error("Unsupported compound type $type")
        }
    }

    override fun getResult() {
        val result = container.getResult()
        if (result != original) {
            when (type) {
                NBTType.NBTTagCompound -> {
                    jsonCompound!!
                    jsonCompound.clearNBT()
                    jsonCompound.mergeCompound(NBT.parseNBT(result))
                }
                NBTType.NBTTagList, NBTType.NBTTagString -> {
                    compound.setString(key, result)
                }
                else -> throw AssertionError()
            }
        }
    }

}