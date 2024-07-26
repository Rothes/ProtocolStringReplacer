package io.github.rothes.protocolstringreplacer.packetlistener.server.title;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;

public final class Title extends BaseTitleListener {

    public Title() {
        super(PacketType.Play.Server.TITLE);
    }

    @Override
    protected void process(PacketEvent packetEvent) {
        if (packetEvent.getPacket().getTitleActions().read(0) == EnumWrappers.TitleAction.ACTIONBAR) {
            return;
        }
        super.process(packetEvent);
    }

}
