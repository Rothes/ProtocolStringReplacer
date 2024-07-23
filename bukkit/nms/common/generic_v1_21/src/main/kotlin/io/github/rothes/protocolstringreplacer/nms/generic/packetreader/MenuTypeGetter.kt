package io.github.rothes.protocolstringreplacer.nms.generic.packetreader

import io.github.rothes.protocolstringreplacer.nms.packetreader.IMenuTypeGetter
import net.minecraft.world.inventory.MenuType

class MenuTypeGetter: IMenuTypeGetter {

    override fun getAnvilMenuType(): Any {
        return MenuType.ANVIL
    }

}