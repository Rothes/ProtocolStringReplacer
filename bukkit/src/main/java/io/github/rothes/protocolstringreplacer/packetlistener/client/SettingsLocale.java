package io.github.rothes.protocolstringreplacer.packetlistener.client;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class SettingsLocale extends BaseClientPacketListener {

    public SettingsLocale() {
        super(PacketType.Play.Client.SETTINGS);
    }

    @Override
    protected void process(@NotNull PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        String read = packetEvent.getPacket().getStrings().read(0);
        user.setClientLocale(read.toLowerCase(Locale.ROOT).replace('-', '_'));
    }

    @Override
    protected boolean canWrite(@NotNull PacketEvent packetEvent) {
        // We just read it.
        return true;
    }

}
