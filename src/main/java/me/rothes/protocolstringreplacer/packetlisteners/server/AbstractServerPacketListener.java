package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.packetlisteners.AbstractPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.BiPredicate;

public abstract class AbstractServerPacketListener extends AbstractPacketListener {

    protected final BiPredicate<ReplacerConfig, User> filter;
    protected final ListenType listenType;
    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    protected AbstractServerPacketListener(PacketType packetType, ListenType listenType) {
        super(packetType);
        this.listenType = listenType;
        filter = (replacerFile, user) -> containType(replacerFile);
        packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ProtocolStringReplacer.getInstance().getConfigManager().listenerPriority, packetType) {
            public void onPacketSending(PacketEvent packetEvent) {
                boolean readOnly = packetEvent.isReadOnly();
                if (!canWrite(packetEvent)) {
                    return;
                }
                process(packetEvent);
                if (readOnly) {
                    packetEvent.setReadOnly(readOnly);
                }
            }
        };
    }

    protected final boolean containType(ReplacerConfig replacerConfig) {
        return replacerConfig.getListenTypeList().contains(listenType);
    }

    protected void saveCaptureMessage(@Nonnull User user, @Nonnull String json) {
        Validate.notNull(user, "User cannot be null");
        Validate.notNull(json, "Json String cannot be null");

        Bukkit.getScheduler().runTaskAsynchronously(ProtocolStringReplacer.getInstance(), () -> {
            if (user.isCapturing(listenType)) {
                user.addCaptureMessage(listenType, json2CaptureMessage(json));
            }
        });
    }

    protected BaseComponent[] json2CaptureMessage(@Nonnull String json) {
        Validate.notNull(json, "Json String cannot be null");

        String time = dateFormat.format(new Date(System.currentTimeMillis()));
        ComponentBuilder hoverTextBuilder = new ComponentBuilder().append("§3§l§m----------------------§3§l 捕获内容 §m----------------------\n")
                .append("§b§l预览: \n")
                .append(ComponentSerializer.parse(json)).append("\n")
                .append("§b§lJson: \n");
        StringBuilder builder = new StringBuilder(json);
        int jsonLength = json.length();
        short lineCount = 0;
        for (int i = 0; i < jsonLength; i++) {
            lineCount++;
            if (lineCount > 60 && ",".indexOf(builder.charAt(i)) != -1) {
                builder.insert(i, '\n');
                lineCount = 0;
                jsonLength++;
                i++;
            }
        }
        hoverTextBuilder.append(builder.toString()).append("\n§a点击复制Json到剪切板");
        ComponentBuilder captureMessageBuilder = new ComponentBuilder().append("§3 §l" + listenType + "§3: §b" +  time)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverTextBuilder.create()))
                .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, json));
        return captureMessageBuilder.create();
    }

}
