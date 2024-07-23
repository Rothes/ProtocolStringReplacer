package io.github.rothes.protocolstringreplacer.packetlistener.server.sign

import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer
import io.github.rothes.protocolstringreplacer.nms.NmsManager

object TileTypeHelper {

    private val signType: Any
    private val hangingSignType: Any

    init {
        if (ProtocolStringReplacer.getInstance().serverMajorVersion < 20) {
            signType = Class.forName("net.minecraft.world.level.block.entity.TileEntityTypes").getField("h")[null]
            hangingSignType = Any()
        } else {
            signType = NmsManager.blockEntityTypeGetter.signType
            hangingSignType = NmsManager.blockEntityTypeGetter.hangingSignType
        }
    }

    @JvmStatic
    fun isSignType(type: Any?): Boolean {
        return type != null && (type === signType || type === hangingSignType)
    }
}
