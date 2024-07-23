package io.github.rothes.protocolstringreplacer.packetlistener.server.chat

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.reflect.accessors.Accessors
import com.comphenix.protocol.wrappers.WrappedChatComponent
import io.github.rothes.protocolstringreplacer.nms.NmsManager
import io.github.rothes.protocolstringreplacer.packetlistener.server.AbstractServerComponentsPacketListener
import io.github.rothes.protocolstringreplacer.replacer.ListenType
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket
import java.util.*

class DisguisedChatPost21 : AbstractServerComponentsPacketListener(PacketType.Play.Server.DISGUISED_CHAT, ListenType.CHAT) {

    private val display = Accessors.getFieldAccessor(NmsManager.disguisedPacketHandler.displayNameField)
    private val target = Accessors.getFieldAccessor(NmsManager.disguisedPacketHandler.targetField)

    override fun process(packetEvent: PacketEvent) {
        val user = getEventUser(packetEvent) ?: return

        val handle = packetEvent.packet.handle as ClientboundDisguisedChatPacket
        val boundRecord = NmsManager.disguisedPacketHandler.boundRecord(handle)
        getReplacedJson(packetEvent, user, listenType,
            WrappedChatComponent.fromHandle(display[boundRecord]).json, filter).let {
            if (it != null) {
                display[boundRecord] = WrappedChatComponent.fromJson(it).handle
            } else {
                return
            }
        }
        val optional = target[boundRecord] as Optional<*>
        if (optional.isPresent) {
            getReplacedJson(packetEvent, user, listenType,
                WrappedChatComponent.fromHandle(optional.get()).json, filter).let {
                if (it != null) {
                    target[boundRecord] = Optional.of(WrappedChatComponent.fromJson(it).handle)
                } else {
                    return
                }
            }
        }


        val wrappedChatComponentStructureModifier = packetEvent.packet.chatComponents
        val wrappedChatComponent = wrappedChatComponentStructureModifier.read(0)
        val replaced = getReplacedJson(packetEvent, user, listenType, wrappedChatComponent.json, filter)

        if (replaced != null) {
            wrappedChatComponentStructureModifier.write(0, WrappedChatComponent.fromJson(replaced))
        }
    }
}