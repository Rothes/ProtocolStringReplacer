package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.title;

import com.comphenix.protocol.PacketType;

public class SetSubtitleText extends AbstractTitleListener {

    public SetSubtitleText() {
        super(PacketType.Play.Server.SET_SUBTITLE_TEXT);
    }

}
