package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.gson.JsonSyntaxException;
import io.papermc.paper.text.PaperComponents;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.user.User;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;

import java.util.Optional;

public final class Chat extends AbstractServerPacketListener {

    private GsonComponentSerializer paperGsonComponentSerializer;

    public Chat() {
        super(PacketType.Play.Server.CHAT, ListenType.CHAT);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            PacketContainer packet = packetEvent.getPacket();
            Optional<Boolean> isFiltered = packet.getMeta("psr_filtered_packet");
            if (!(isFiltered.isPresent() && isFiltered.get())) {
                User user = getEventUser(packetEvent);
                StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
                WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
                if (wrappedChatComponent != null) {
                    // TODO
                    BaseComponent[] baseComponents = ComponentSerializer.parse(wrappedChatComponent.getJson());
                    baseComponents = ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedComponents(baseComponents, user, filter);
                    wrappedChatComponentStructureModifier.write(0, WrappedChatComponent.fromJson(ComponentSerializer.toString(baseComponents)));
                } else {
                    StructureModifier<Object> structureModifier = packet.getModifier();
                    for (int fieldIndex = 1; fieldIndex < 3; fieldIndex++) {
                        Object read = structureModifier.read(fieldIndex);
                        if (read instanceof BaseComponent[]) {
                            structureModifier.write(fieldIndex, ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedComponents((BaseComponent[]) read, user, filter));
                        } else if (isPaperComponent(read)) {
                            try {
                            structureModifier.write(fieldIndex, getPaperGsonComponentSerializer().deserialize(
                                    ComponentSerializer.toString(
                                            ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedComponents(
                                                    ComponentSerializer.parse(getPaperGsonComponentSerializer().serialize((net.kyori.adventure.text.Component) read)), user, filter
                                            )
                                    )
                            ));
                            } catch (JsonSyntaxException exception) {
                                Bukkit.getConsoleSender().sendMessage("§7[§cProtocol§6StringReplacer§7] §c捕获到异常. 请在GitHub反馈, 并粘贴以下错误信息.");
                                Bukkit.getConsoleSender().sendMessage("§c ================= 异常跟踪 ================= ");
                                exception.printStackTrace();
                                Bukkit.getConsoleSender().sendMessage("§c - 修改前: ");
                                Bukkit.getConsoleSender().sendMessage(getPaperGsonComponentSerializer().serialize((net.kyori.adventure.text.Component) read));
                                Bukkit.getConsoleSender().sendMessage("§c - 修改后: ");
                                try {
                                    Bukkit.getConsoleSender().sendMessage(ComponentSerializer.toString(
                                            ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedComponents(
                                                    ComponentSerializer.parse(getPaperGsonComponentSerializer().serialize((net.kyori.adventure.text.Component) read)), user, filter
                                            )
                                    ));
                                } catch (JsonSyntaxException e) {
                                    Bukkit.getConsoleSender().sendMessage("§c exception.");
                                }
                                Bukkit.getConsoleSender().sendMessage("§c ================= 异常跟踪 ================= ");
                            }
                        }
                    }
                }
            }
        }
    };

    private boolean isPaperComponent(Object object) {
        return ProtocolStringReplacer.getInstance().isPaper() && object instanceof net.kyori.adventure.text.Component;
    }

    private GsonComponentSerializer getPaperGsonComponentSerializer() {
        if (paperGsonComponentSerializer == null) {
            paperGsonComponentSerializer = PaperComponents.gsonSerializer();
        }
        return paperGsonComponentSerializer;
    }

}
