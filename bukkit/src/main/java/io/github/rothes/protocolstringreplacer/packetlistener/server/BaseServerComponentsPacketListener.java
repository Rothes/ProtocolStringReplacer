package io.github.rothes.protocolstringreplacer.packetlistener.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.util.SpigotUtils;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;
import io.github.rothes.protocolstringreplacer.util.PaperUtils;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class BaseServerComponentsPacketListener extends BaseServerPacketListener {

    protected static final String BLOCKED_JSON = "{\"text\":\"ProtocolStringReplacer blocked message. If you see this, it's caused by other plugin(s).\"}";

    protected BaseServerComponentsPacketListener(PacketType packetType, ListenType listenType) {
        super(packetType, listenType);
    }

    protected static BaseComponent[] getSpigotComponent(StructureModifier<Object> modifier) {
        if (!ProtocolStringReplacer.getInstance().isSpigot()) {
            return null;
        }
        StructureModifier<BaseComponent[]> componentModifier = modifier.withType(BaseComponent[].class);
        if (componentModifier.size() == 0) {
            return null;
        }
        return componentModifier.read(0);
    }

    protected String processSpigotComponent(StructureModifier<Object> modifier, PacketEvent packetEvent, PsrUser user) {
        if (!ProtocolStringReplacer.getInstance().isSpigot()) {
            return null;
        }
        StructureModifier<BaseComponent[]> componentModifier = modifier.withType(BaseComponent[].class);
        if (componentModifier.size() == 0) {
            return null;
        }
        BaseComponent[] read = componentModifier.read(0);
        if (read == null) {
            return null;
        }

        String result = getReplacedJson(packetEvent, user, listenType, SpigotUtils.serializeComponents(read), filter);
        componentModifier.write(0, null);
        return result == null ? BLOCKED_JSON : result;
    }

    protected static Component getPaperComponent(StructureModifier<Object> modifier) {
        if (!ProtocolStringReplacer.getInstance().hasPaperComponent()) {
            return null;
        }
        StructureModifier<Component> componentModifier = modifier.withType(Component.class);
        return componentModifier.read(0);
    }

    protected String processPaperComponent(StructureModifier<Object> modifier, PacketEvent packetEvent, PsrUser user) {
        if (!ProtocolStringReplacer.getInstance().hasPaperComponent()) {
            return null;
        }
        StructureModifier<Component> componentModifier = modifier.withType(Component.class);
        Component read = componentModifier.read(0);
        if (read == null) {
            return null;
        }

        String result = getReplacedJson(packetEvent, user, listenType, PaperUtils.serializeComponent(read), filter);
        componentModifier.write(0, null);
        return result == null ? BLOCKED_JSON : result;
    }

}
