package io.github.rothes.protocolstringreplacer.packetlistener.server.actionbar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import io.github.rothes.protocolstringreplacer.packetlistener.server.title.AbstractTitleListener;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;

public class TitleActionBar extends AbstractTitleListener {

    public TitleActionBar() {
        super(PacketType.Play.Server.TITLE, ListenType.ACTIONBAR);
    }

    @Override
    protected void process(PacketEvent packetEvent) {
        if (packetEvent.getPacket().getTitleActions().read(0) != EnumWrappers.TitleAction.ACTIONBAR) {
            return;
        }
        super.process(packetEvent);
    }

}
