package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.packetlisteners.AbstractPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import me.rothes.protocolstringreplacer.replacer.containers.ChatJsonContainer;
import me.rothes.protocolstringreplacer.replacer.containers.ItemMetaContainer;
import me.rothes.protocolstringreplacer.replacer.containers.SimpleTextContainer;
import me.rothes.protocolstringreplacer.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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

    protected ChatJsonContainer deployContainer(@Nonnull PacketEvent packetEvent, @Nonnull User user,
                                                @Nonnull String json, BiPredicate<ReplacerConfig, User> filter) {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, filter);

        ChatJsonContainer container = new ChatJsonContainer(json, true);
        container.createJsons(container);
        if (replacerManager.isJsonBlocked(container, replacers)) {
            packetEvent.setCancelled(true);
            return null;
        }
        replacerManager.replaceContainerJsons(container, replacers);
        container.createDefaultChildren();
        container.createTexts(container);
        if (replacerManager.isTextBlocked(container, replacers)) {
            packetEvent.setCancelled(true);
            return null;
        }
        replacerManager.replaceContainerTexts(container, replacers);
        replacerManager.setPapi(user, container.getTexts());

        return container;
    }

    @Nullable
    protected String getReplacedJson(@Nonnull PacketEvent packetEvent, @Nonnull User user,
                                     @Nonnull String json, BiPredicate<ReplacerConfig, User> filter) {
        ChatJsonContainer container = deployContainer(packetEvent, user, json, filter);

        if (container != null) {
            return container.getResult();
        } else {
            return null;
        }
    }

    @Nullable
    protected WrappedChatComponent getReplacedJsonWrappedComponent(@Nonnull PacketEvent packetEvent, @Nonnull User user,
                                                                   @Nonnull String json, BiPredicate<ReplacerConfig, User> filter) {
        ChatJsonContainer container = deployContainer(packetEvent, user, json, filter);

        if (container != null) {
            return WrappedChatComponent.fromJson(container.getResult());
        } else {
            return null;
        }
    }

    @Nullable
    protected String getReplacedText(@Nonnull PacketEvent packetEvent, @Nonnull User user,
                                     @Nonnull String text, BiPredicate<ReplacerConfig, User> filter) {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, filter);
        SimpleTextContainer container = new SimpleTextContainer(text);
        container.createTexts(container);
        if (replacerManager.isTextBlocked(container, replacers)) {
            packetEvent.setCancelled(true);
            return null;
        }
        replacerManager.replaceContainerTexts(container, replacers);
        replacerManager.setPapi(user, container.getTexts());
        return container.getResult();
    }

    protected boolean replaceItemStack(@Nonnull PacketEvent packetEvent, @Nonnull User user,
                                       @Nonnull ItemStack itemStack, BiPredicate<ReplacerConfig, User> filter) {
        if (itemStack.hasItemMeta()) {
            ItemStack original = itemStack.clone();

            ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
            List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, filter);
            ItemMetaContainer container = new ItemMetaContainer(itemStack.getItemMeta());
            container.createDefaultChildren();
            if (!container.isFromCache()) {
                container.createJsons(container);
                if (replacerManager.isJsonBlocked(container, replacers)) {
                    packetEvent.setCancelled(true);
                    return true;
                }
                replacerManager.replaceContainerJsons(container, replacers);
            }
            container.createTexts(container);
            List<Integer> papiIndexes;
            if (!container.isFromCache()) {
                if (replacerManager.isTextBlocked(container, replacers)) {
                    packetEvent.setCancelled(true);
                    return true;
                }
                replacerManager.replaceContainerTexts(container, replacers);
                papiIndexes = replacerManager.getPapiIndexes(container.getTexts());
                container.getMetaCache().setPlaceholderIndexes(papiIndexes);
            } else {
                papiIndexes = container.getMetaCache().getPlaceholderIndexes();
            }
            replacerManager.setPapi(user, container.getTexts(), papiIndexes);
            itemStack.setItemMeta(container.getResult());
            if (!original.isSimilar(itemStack)) {
                user.saveUserMetaCache(original, itemStack);
            }
        }
        return false;
    }

}
