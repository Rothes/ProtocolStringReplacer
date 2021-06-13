package me.Rothes.ProtocolStringReplacer.PacketListeners.Server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import me.Rothes.ProtocolStringReplacer.PacketListeners.AbstractPacketListener;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.Replacer.ReplacerConfig;
import me.Rothes.ProtocolStringReplacer.User.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public class AbstractServerPacketListener extends AbstractPacketListener {

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
        }
        return stringBuilder.toString();
    }

    protected final String legacyTextToJson(@NotNull String text) {
        return "{\"text\":\"" + text.replace("\"", "\"\"") + "\"}";
    }

    protected void setSignText(@NotNull NbtCompound nbtCompound, @NotNull User user, @NotNull BiPredicate<ReplacerConfig, User> filter) {
        nbtCompound.put("Text1", legacyTextToJson(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(
                jsonToLegacyText(nbtCompound.getString("Text1")), user, filter)));
        nbtCompound.put("Text2", legacyTextToJson(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(
                jsonToLegacyText(nbtCompound.getString("Text2")), user, filter)));
        nbtCompound.put("Text3", legacyTextToJson(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(
                jsonToLegacyText(nbtCompound.getString("Text3")), user, filter)));
        nbtCompound.put("Text4", legacyTextToJson(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(
                jsonToLegacyText(nbtCompound.getString("Text4")), user, filter)));
    }
}
