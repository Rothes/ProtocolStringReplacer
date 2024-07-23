package io.github.rothes.protocolstringreplacer.nms

import io.github.rothes.protocolstringreplacer.nms.packetreader.IBlockEntityTypeGetter
import io.github.rothes.protocolstringreplacer.nms.packetreader.IDisguisedPacketHandler
import io.github.rothes.protocolstringreplacer.nms.packetreader.IMenuTypeGetter
import io.github.rothes.protocolstringreplacer.nms.packetreader.IPacketReader

object NmsManager {

    private const val PACKAGE_PREFIX = "io.github.rothes.protocolstringreplacer.nms"

    lateinit var minecraftVersion: String

    val packetReader by lazy { IPacketReader::class.java.instance }
    val disguisedPacketHandler by lazy { IDisguisedPacketHandler::class.java.instance }
    val menuTypeGetter by lazy { IMenuTypeGetter::class.java.instance }
    val blockEntityTypeGetter by lazy { IBlockEntityTypeGetter::class.java.instance }

    @Suppress("UNCHECKED_CAST")
    private val <T> Class<T>.versioned: Class<out T>
        get() = Class.forName(buildString {
            append(PACKAGE_PREFIX)
            append(".v")
            append(minecraftVersion.replace('.', '_'))
            append(this@versioned.`package`.name.substring(PACKAGE_PREFIX.length))
            append('.')
            append(this@versioned.simpleName.substring(1))
        }) as Class<out T>

    @Suppress("UNCHECKED_CAST")
    private val <T> Class<T>.instance: T
        get() = this.versioned.getConstructor().newInstance() as T
}