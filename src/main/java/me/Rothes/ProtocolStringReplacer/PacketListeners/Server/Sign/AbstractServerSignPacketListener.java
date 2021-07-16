package me.Rothes.ProtocolStringReplacer.PacketListeners.Server.Sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.AbstractServerPacketListener;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.Replacer.ListenType;
import me.Rothes.ProtocolStringReplacer.Replacer.ReplacerConfig;
import me.Rothes.ProtocolStringReplacer.User.User;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public abstract class AbstractServerSignPacketListener extends AbstractServerPacketListener {

    protected AbstractServerSignPacketListener(PacketType packetType) {
        super(packetType, ListenType.SIGN);
    }

    protected void setSignText(@NotNull NbtCompound nbtCompound, @NotNull User user, @NotNull BiPredicate<ReplacerConfig, User> filter) {
        nbtCompound.put("Text1", ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                .getReplacedComponents(ComponentSerializer.parse(nbtCompound.getString("Text1")), user, filter)));
        nbtCompound.put("Text2", ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                .getReplacedComponents(ComponentSerializer.parse(nbtCompound.getString("Text2")), user, filter)));
        nbtCompound.put("Text3", ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                .getReplacedComponents(ComponentSerializer.parse(nbtCompound.getString("Text3")), user, filter)));
        nbtCompound.put("Text4", ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                .getReplacedComponents(ComponentSerializer.parse(nbtCompound.getString("Text4")), user, filter)));
    }

}
