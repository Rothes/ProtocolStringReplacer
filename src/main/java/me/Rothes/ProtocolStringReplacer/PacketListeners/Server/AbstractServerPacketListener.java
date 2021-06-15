package me.Rothes.ProtocolStringReplacer.PacketListeners.Server;

import com.comphenix.protocol.PacketType;
import me.Rothes.ProtocolStringReplacer.PacketListeners.AbstractPacketListener;
import me.Rothes.ProtocolStringReplacer.Replacer.ReplacerConfig;
import me.Rothes.ProtocolStringReplacer.User.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public abstract class AbstractServerPacketListener extends AbstractPacketListener {

    protected final BiPredicate<ReplacerConfig, User> filter;

    protected AbstractServerPacketListener(PacketType packetType) {
        super(packetType);
        filter = (replacerFile, user) -> containPacket(replacerFile);
    }

    protected final boolean containPacket(ReplacerConfig replacerConfig) {
        return replacerConfig.getPacketTypeList().contains(packetType);
    }

    protected final String jsonToLegacyText(@NotNull String json) {
        BaseComponent[] baseComponents = ComponentSerializer.parse(json);
        StringBuilder stringBuilder = new StringBuilder(baseComponents.length);
        for (BaseComponent baseComponent : baseComponents) {
            stringBuilder.append(baseComponent.toLegacyText().substring(2));
            Bukkit.getConsoleSender().sendMessage(baseComponent.toLegacyText().replace("§", "&"));
        }
        return stringBuilder.toString();
    }

    protected final String legacyTextToJson(@NotNull String text) {
        return "{\"text\":\"" + text.replace("\"", "\"\"") + "\"}";
    }

}
