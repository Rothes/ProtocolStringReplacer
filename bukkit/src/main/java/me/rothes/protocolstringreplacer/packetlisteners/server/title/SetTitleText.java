package me.rothes.protocolstringreplacer.packetlisteners.server.title;

import com.comphenix.protocol.PacketType;

public class SetTitleText extends AbstractTitleListener {

    public SetTitleText() {
        super(PacketType.Play.Server.SET_TITLE_TEXT);
    }

}
