package me.rothes.protocolstringreplacer.packetlisteners.client;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Locale;

public class SettingsLocaleUpper20 extends AbstractClientPacketListener {

    private Field language;

    public SettingsLocaleUpper20() {
        super(PacketType.Play.Client.SETTINGS);
    }

    @Override
    protected void process(@NotNull PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        Object record = packetEvent.getPacket().getModifier().read(0);
        String read;
        try {
            read = (String) field().get(record);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        user.setClientLocale(read.toLowerCase(Locale.ROOT).replace('-', '_'));
    }

    private Field field() {
        if (language == null) {
            language = PacketType.Play.Client.SETTINGS.getPacketClass().getDeclaredFields()[0].getType().getDeclaredFields()[0];
            language.setAccessible(true);
        }
        return language;
    }

    @Override
    protected boolean canWrite(@NotNull PacketEvent packetEvent) {
        // We just read it.
        return true;
    }

}
