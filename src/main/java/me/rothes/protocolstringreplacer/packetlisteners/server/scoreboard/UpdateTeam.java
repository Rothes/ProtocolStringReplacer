package me.rothes.protocolstringreplacer.packetlisteners.server.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class UpdateTeam extends AbstractScoreBoardListener {

    protected final BiPredicate<ReplacerConfig, PsrUser> teamDNameFilter = (replacerConfig, user) ->
            containType(replacerConfig) && checkFilter(user, replacerConfig) && replacerConfig.handleScoreboardTeamDisplayName();
    protected final BiPredicate<ReplacerConfig, PsrUser> teamPrefixFilter = (replacerConfig, user) ->
            containType(replacerConfig) && checkFilter(user, replacerConfig) && replacerConfig.handleScoreboardTeamPrefix();
    protected final BiPredicate<ReplacerConfig, PsrUser> teamSuffixFilter = (replacerConfig, user) ->
            containType(replacerConfig) && checkFilter(user, replacerConfig) && replacerConfig.handleScoreboardTeamSuffix();

    private final Field displayName;
    private final Field prefix;
    private final Field suffix;

    public UpdateTeam() {
        super(PacketType.Play.Server.SCOREBOARD_TEAM, ListenType.SCOREBOARD);

        Class<?> info = PacketType.Play.Server.SCOREBOARD_TEAM.getPacketClass().getDeclaredClasses()[0];
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
        if (!read.isPresent()) {
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
            WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromHandle(field.get(infoObj));
            String json = wrappedChatComponent.getJson();
            String replacedJson = getReplacedJson(event, user, listenType, json, filter);
            if (replacedJson == null) {
                return true;
            }
            wrappedChatComponent.setJson(replacedJson);
            field.set(infoObj, wrappedChatComponent.getHandle());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

}
