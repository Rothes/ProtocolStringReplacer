package io.github.rothes.protocolstringreplacer.packetlistener.server.title;

import com.comphenix.protocol.PacketType;

public class SetTitleText extends BaseTitleListener {

    public SetTitleText() {
        super(PacketType.Play.Server.SET_TITLE_TEXT);
    }

}
