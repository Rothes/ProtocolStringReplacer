package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.title;

import com.comphenix.protocol.PacketType;

public class SetTitleText extends AbstractTitleListener {

    public SetTitleText() {
        super(PacketType.Play.Server.SET_TITLE_TEXT);
    }

}
