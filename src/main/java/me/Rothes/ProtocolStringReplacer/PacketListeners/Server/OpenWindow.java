package me.Rothes.ProtocolStringReplacer.PacketListeners.Server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import me.Rothes.ProtocolStringReplacer.User.User;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import org.bukkit.Bukkit;

public final class OpenWindow extends AbstractServerPacketListener {

    public OpenWindow() {
        super(PacketType.Play.Server.OPEN_WINDOW);
    }

    public final PacketAdapter packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ListenerPriority.HIGHEST, packetType) {
        public void onPacketSending(PacketEvent packetEvent) {
            PacketContainer packet = packetEvent.getPacket();
            StructureModifier<WrappedChatComponent> wrappedChatComponentStructureModifier = packet.getChatComponents();
            WrappedChatComponent wrappedChatComponent = wrappedChatComponentStructureModifier.read(0);
            String json = wrappedChatComponent.getJson();
            User user = getEventUser(packetEvent);
            String currentTitle = jsonToLegacyText(json);
            user.setCurrentWindowTitle(currentTitle);
            if (new JsonParser().parse(json).getAsJsonObject().get("translate") == null) {
                wrappedChatComponent.setJson(legacyTextToJson(ProtocolStringReplacer.getInstance().getReplacerManager().getReplacedString(currentTitle, user, filter)));
                wrappedChatComponentStructureModifier.write(0, wrappedChatComponent);
            }
        }
    };

}
