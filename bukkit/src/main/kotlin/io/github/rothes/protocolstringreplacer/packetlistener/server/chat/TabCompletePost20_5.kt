package io.github.rothes.protocolstringreplacer.packetlistener.server.chat

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.reflect.accessors.Accessors
import io.github.rothes.protocolstringreplacer.componentToJson
import io.github.rothes.protocolstringreplacer.get
import io.github.rothes.protocolstringreplacer.jsonToComponent
import io.github.rothes.protocolstringreplacer.modifier
import io.github.rothes.protocolstringreplacer.packetlistener.server.BaseServerPacketListener
import io.github.rothes.protocolstringreplacer.replacer.ListenType
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket
import java.util.Optional

class TabCompletePost20_5: BaseServerPacketListener(PacketType.Play.Server.TAB_COMPLETE, ListenType.TAB_COMPLETE) {

    private val text = Accessors.getFieldAccessor(ClientboundCommandSuggestionsPacket.Entry::class.java.declaredFields.first { it.type == String::class.java })!!
    private val tooltip = Accessors.getFieldAccessor(ClientboundCommandSuggestionsPacket.Entry::class.java.declaredFields.first { it.type == Optional::class.java })!!

    override fun process(packetEvent: PacketEvent) {
        val packet = packetEvent.packet
        val user = getEventUser(packetEvent) ?: return

        val entries = packet.modifier<List<ClientboundCommandSuggestionsPacket.Entry>>()[0]
        entries.forEach { entry ->
            getReplacedText(packetEvent, user, listenType, text[entry] as String, filter).let { replaced ->
                if (replaced == null) {
                    return
                }
                text[entry] = replaced
            }
            val optional = tooltip[entry] as Optional<*>
            if (optional.isPresent) {
                getReplacedJson(packetEvent, user, listenType, componentToJson(optional.get()), filter).let { replaced ->
                    if (replaced == null) {
                        return
                    }
                    tooltip[entry] = Optional.of(jsonToComponent(replaced))
                }
            }
        }
    }

}