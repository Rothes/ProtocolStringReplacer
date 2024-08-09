package io.github.rothes.protocolstringreplacer.nms

import io.github.rothes.protocolstringreplacer.nms.packetreader.IBlockEntityTypeGetter
import io.github.rothes.protocolstringreplacer.nms.packetreader.IDisguisedPacketHandler
import io.github.rothes.protocolstringreplacer.nms.packetreader.IMenuTypeGetter
import io.github.rothes.protocolstringreplacer.nms.packetreader.IPacketReader

object NmsManager {

    private const val PACKAGE_PREFIX = "io.github.rothes.protocolstringreplacer.nms"

    private lateinit var minecraftVersion: String

    fun setVersion(major: Int, minor: Int) {
        var m = minor
        while (m < 10) {
            setVersionProp(major, m)
            try {
                create<IPacketReader>()
                return
            } catch (ignored: ClassNotFoundException) {
                m++
            }
        }
        setVersionProp(major, minor)
    }

    private fun setVersionProp(major: Int, minor: Int) {
        minecraftVersion = if (minor == 0) "v1_$major" else "v1_${major}_$minor"
    }

    val packetReader by lazy { create<IPacketReader>() }
    val disguisedPacketHandler by lazy { create<IDisguisedPacketHandler>() }
    val menuTypeGetter by lazy { create<IMenuTypeGetter>() }
    val blockEntityTypeGetter by lazy { create<IBlockEntityTypeGetter>() }

    private inline fun <reified T> create(): T {
        return T::class.java.instance
    }

    @Suppress("UNCHECKED_CAST")
    private val <T> Class<T>.versioned: Class<out T>
        get() = Class.forName(buildString {
            append(PACKAGE_PREFIX)
            append(".")
            append(minecraftVersion)
            append(this@versioned.`package`.name.substring(PACKAGE_PREFIX.length))
            append('.')
            append(this@versioned.simpleName.substring(1))
        }) as Class<out T>

    private val <T> Class<T>.instance: T
        get() = this.versioned.getConstructor().newInstance() as T

}