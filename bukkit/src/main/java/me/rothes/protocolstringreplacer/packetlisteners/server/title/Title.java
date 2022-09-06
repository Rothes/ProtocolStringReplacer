package me.rothes.protocolstringreplacer.packetlisteners.server.title;

import com.comphenix.protocol.PacketType;

public final class Title extends AbstractTitleListener {

    public Title() {
        super(PacketType.Play.Server.TITLE);
    }

}
