package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.bossbar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.AbstractServerPacketListener;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.ListenType;

public final class BossBar extends AbstractServerPacketListener {

    public BossBar() {
        super(PacketType.Play.Server.BOSS, ListenType.BOSS_BAR);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packetEvent.getPacket().getChatComponents();
        if (wrappedChatComponentStructureModifier.size() != 0) {
            WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
            String json = wrappedChatComponent.getJson();

            WrappedChatComponent replaced = getReplacedJsonWrappedComponent(packetEvent, user, listenType, json, filter);
            if (replaced != null) {
                wrappedChatComponentStructureModifier.write(0, replaced);
            }
        }
    }

}
