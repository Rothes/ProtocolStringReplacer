package io.github.rothes.protocolstringreplacer.packetlistener.server.title;

import com.comphenix.protocol.PacketType;

public class SetSubtitleText extends BaseTitleListener {

    public SetSubtitleText() {
        super(PacketType.Play.Server.SET_SUBTITLE_TEXT);
    }

}
