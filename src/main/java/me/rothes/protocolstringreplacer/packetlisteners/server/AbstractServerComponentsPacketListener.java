package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.utils.PaperUtils;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public abstract class AbstractServerComponentsPacketListener extends AbstractServerPacketListener {

    private static final String BLOCKED_JSON = "{\"text\":\"ProtocolStringReplacer blocked message. If you see this, it's caused by other plugin(s).\"}";

    protected AbstractServerComponentsPacketListener(PacketType packetType, ListenType listenType) {
        super(packetType, listenType);
    }

    protected String processSpigotComponent(StructureModifier<Object> modifier, PacketEvent packetEvent, PsrUser user) {
        if (!ProtocolStringReplacer.getInstance().isSpigot()) {
            return null;
        }
        StructureModifier<Object> componentModifier = modifier.withType(BaseComponent[].class);
        if (componentModifier.size() == 0) {
            return null;
        }
        Object read = componentModifier.read(0);
        if (read == null) {
            return null;
        }

        String result = getReplacedJson(packetEvent, user, listenType, ComponentSerializer.toString((BaseComponent[]) read), filter);
        componentModifier.write(0, null);
        return result == null ? BLOCKED_JSON : result;
    }

    protected String processPaperComponent(StructureModifier<Object> modifier, PacketEvent packetEvent, PsrUser user) {
        if (!ProtocolStringReplacer.getInstance().isPaper()) {
            return null;
        }
        StructureModifier<Object> componentModifier = modifier.withType(Component.class);
        Object read = componentModifier.read(0);
        if (read == null) {
            return null;
        }

        String result = getReplacedJson(packetEvent, user, listenType, PaperUtils.getPaperGsonComponentSerializer().serialize((Component) read), filter);
        componentModifier.write(0, null);
        return result == null ? BLOCKED_JSON : result;
    }

}
