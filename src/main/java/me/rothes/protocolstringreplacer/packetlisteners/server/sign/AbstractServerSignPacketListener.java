package me.rothes.protocolstringreplacer.packetlisteners.server.sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.user.User;
import me.rothes.protocolstringreplacer.packetlisteners.server.AbstractServerPacketListener;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public abstract class AbstractServerSignPacketListener extends AbstractServerPacketListener {

    protected AbstractServerSignPacketListener(PacketType packetType) {
        super(packetType, ListenType.SIGN);
    }

    protected void setSignText(@NotNull NbtCompound nbtCompound, @NotNull User user, @NotNull BiPredicate<ReplacerConfig, User> filter) {
        nbtCompound.put("Text1", ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                .getReplacedComponents(ComponentSerializer.parse(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedJson(
                        nbtCompound.getString("Text1"), user, filter, false
                )), user, filter)));
        nbtCompound.put("Text2", ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                .getReplacedComponents(ComponentSerializer.parse(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedJson(
                        nbtCompound.getString("Text2"), user, filter, false
                )), user, filter)));
        nbtCompound.put("Text3", ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                .getReplacedComponents(ComponentSerializer.parse(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedJson(
                        nbtCompound.getString("Text3"), user, filter, false
                )), user, filter)));
        nbtCompound.put("Text4", ComponentSerializer.toString(ProtocolStringReplacer.getInstance().getReplacerManager()
                .getReplacedComponents(ComponentSerializer.parse(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedJson(
                        nbtCompound.getString("Text4"), user, filter, false
                )), user, filter)));
    }

}
