package io.github.rothes.protocolstringreplacer.packetlistener.server.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.api.exceptions.IncompatibleServerException;
import io.github.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.server.BaseServerPacketListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public final class UpdateTeamPost17 extends BaseUpdateTeamListener {

    private final Field displayName;
    private final Field prefix;
    private final Field suffix;

    public UpdateTeamPost17() {
        Class<?> info = Arrays.stream(PacketType.Play.Server.SCOREBOARD_TEAM.getPacketClass().getDeclaredClasses()).filter(
                it -> !it.isInterface() && !it.isEnum()
        ).findFirst().get();
        List<Field> collect = Arrays.stream(info.getDeclaredFields())
                .filter(it -> it.getType() == MinecraftReflection.getIChatBaseComponentClass())
                .collect(Collectors.toList());
        displayName = collect.get(0);
        displayName.setAccessible(true);
        prefix = collect.get(1);
        prefix.setAccessible(true);
        suffix = collect.get(2);
        suffix.setAccessible(true);
    }

    protected void process(@NotNull PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        PacketContainer packet = packetEvent.getPacket();

        Optional<?> read = (Optional<?>) packet.getModifier().withType(Optional.class).read(0);
        if (read == null || !read.isPresent()) {
            return;
        }

        Object o = read.get();
        if (processField(o, prefix, packetEvent, user, teamPrefixFilter)) {
            return;
        }
        if (processField(o, displayName, packetEvent, user, teamDNameFilter)) {
            return;
        }
        processField(o, suffix, packetEvent, user, teamSuffixFilter);
    }

    private boolean processField(Object infoObj, Field field, PacketEvent event, PsrUser user,
                                 BiPredicate<ReplacerConfig, PsrUser> filter) {
        try {
            Object handle = field.get(infoObj);
            if (handle == null) {
                // Other plugins (e.g. NametagEdit) may cause NPE
                return false;
            }
            WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromHandle(handle);
            String json = wrappedChatComponent.getJson();
            String replacedJson = BaseServerPacketListener.getReplacedJson(event, user, listenType, json, filter);
            if (replacedJson == null) {
                return true;
            }
            wrappedChatComponent.setJson(replacedJson);
            field.set(infoObj, wrappedChatComponent.getHandle());
        } catch (IllegalAccessException e) {
            throw new IncompatibleServerException(e);
        }
        return false;
    }

}
