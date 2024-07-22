package io.github.rothes.protocolstringreplacer.nms

import io.github.rothes.protocolstringreplacer.nms.packetreader.IPacketReader

object NmsManager {

    lateinit var minecraftVersion: String

    val packetReader by lazy {
        val forName = Class.forName("io.github.rothes.protocolstringreplacer.nms." +
                "v${minecraftVersion.replace('.', '_')}.packetreader.PacketReader")
        forName.getConstructor().newInstance() as IPacketReader
    }
}