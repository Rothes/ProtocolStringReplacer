package io.github.rothes.protocolstringreplacer.nms.generic.packetreader

import io.github.rothes.protocolstringreplacer.nms.packetreader.IBlockEntityTypeGetter
import net.minecraft.world.level.block.entity.BlockEntityType

class BlockEntityTypeGetter: IBlockEntityTypeGetter {

    override fun getSignType(): Any = BlockEntityType.SIGN

    override fun getHangingSignType(): Any = BlockEntityType.HANGING_SIGN
}