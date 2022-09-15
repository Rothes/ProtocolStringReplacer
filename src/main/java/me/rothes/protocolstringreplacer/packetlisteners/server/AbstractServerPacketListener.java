package me.rothes.protocolstringreplacer.packetlisteners.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.capture.CaptureInfo;
import me.rothes.protocolstringreplacer.api.capture.CaptureInfoImpl;
import me.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.packetlisteners.AbstractPacketListener;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import me.rothes.protocolstringreplacer.replacer.containers.ChatJsonContainer;
import me.rothes.protocolstringreplacer.replacer.containers.ItemStackContainer;
import me.rothes.protocolstringreplacer.replacer.containers.Replaceable;
import me.rothes.protocolstringreplacer.replacer.containers.SimpleTextContainer;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public abstract class AbstractServerPacketListener extends AbstractPacketListener {

    protected final BiPredicate<ReplacerConfig, PsrUser> filter;
    protected final ListenType listenType;

    protected AbstractServerPacketListener(PacketType packetType, ListenType listenType) {
        super(packetType);
        this.listenType = listenType;
        filter = (replacerConfig, user) -> containType(replacerConfig) && checkPermission(user, replacerConfig);
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

    protected final boolean checkPermission(PsrUser user, ReplacerConfig replacerConfig) {
        String permission = replacerConfig.getPermissionLimit();
        return permission.isEmpty() || user.hasPermission(permission);
    }

    protected static ChatJsonContainer deployContainer(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user, @Nonnull ListenType listenType,
                                                       @Nonnull String json, BiPredicate<ReplacerConfig, PsrUser> filter, boolean saveTitle) {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, filter);

        return deployContainer(packetEvent, user, listenType, json, replacers, saveTitle);
    }

    protected static ChatJsonContainer deployContainer(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user, @Nonnull ListenType listenType,
                                                       @Nonnull String json, List<ReplacerConfig> replacers, boolean saveTitle) {
        boolean blocked = false;
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();

        ChatJsonContainer container = new ChatJsonContainer(json, true);
        CaptureInfoImpl info = null;
        if (user.isCapturing(listenType)) {
            info = new CaptureInfoImpl();
            info.setTime(System.currentTimeMillis());
            info.setUser(user);
            info.setListenType(listenType);
        }

        container.createJsons(container);
        if (saveTitle) {
            List<Replaceable> jsons = container.getJsons();
            user.setCurrentWindowTitle(jsons.get(jsons.size() - 1).getText());
        }
        if (user.isCapturing(listenType)) {
            info.setJsons(container.getJsons());
        }
        if (replacerManager.isJsonBlocked(container, replacers)) {
            packetEvent.setCancelled(true);
            blocked = true;
        } else {
            replacerManager.replaceContainerJsons(container, replacers);
        }
        try {
            container.createDefaultChildren();
        } catch (Throwable t) {
            throw new RuntimeException("Unable to create default children. Please check your Json format.\n"
                    + "Original Json: " + json + "\n"
                    + "Replaced Json: " + container.getJsons().get(0).getText() + "\n"
                    + "If you need support, please provide the stacktrace below.", t);
        }
        try {
            container.createTexts(container);
            if (user.isCapturing(listenType)) {
                info.setTexts(container.getTexts());
                user.addCaptureInfo(listenType, info);
            }
        } catch (Throwable t) {
            throw new RuntimeException("Unable to create Texts. Please check your Json format.\n"
                    + "Original Json: " + json + "\n"
                    + "Replaced Json: " + container.getJsons().get(0).getText() + "\n"
                    + "If you need support, please provide the stacktrace below.", t);
        }
        if (blocked || replacerManager.isTextBlocked(container, replacers)) {
            packetEvent.setCancelled(true);
            blocked = true;
        } else {
            replacerManager.replaceContainerTexts(container, replacers);
            replacerManager.setPapi(user, container.getTexts());
        }

        return blocked ? null : container;
    }

    @Nullable
    protected static String getReplacedJson(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user, @Nonnull ListenType listenType,
                                            @Nonnull String json, BiPredicate<ReplacerConfig, PsrUser> filter) {
        ChatJsonContainer container = deployContainer(packetEvent, user, listenType, json, filter, false);

        if (container != null) {
            return container.getResult();
        } else {
            return null;
        }
    }

    @Nullable
    protected static String getReplacedJson(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user, @Nonnull ListenType listenType,
                                            @Nonnull String json, List<ReplacerConfig> replacers) {
        ChatJsonContainer container = deployContainer(packetEvent, user, listenType, json, replacers, false);

        if (container != null) {
            return container.getResult();
        } else {
            return null;
        }
    }

    @Nullable
    protected static WrappedChatComponent getReplacedJsonWrappedComponent(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user, @Nonnull ListenType listenType,
                                                                          @Nonnull String json, BiPredicate<ReplacerConfig, PsrUser> filter, boolean saveTitle) {
        ChatJsonContainer container = deployContainer(packetEvent, user, listenType, json, filter, saveTitle);

        if (container != null) {
            return WrappedChatComponent.fromJson(container.getResult());
        } else {
            return null;
        }
    }

    @Nullable
    protected static WrappedChatComponent getReplacedJsonWrappedComponent(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user, @Nonnull ListenType listenType,
                                                                          @Nonnull String json, BiPredicate<ReplacerConfig, PsrUser> filter) {
        return getReplacedJsonWrappedComponent(packetEvent, user, listenType, json, filter, false);
    }

    @Nullable
    protected static String getReplacedText(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user, @Nonnull ListenType listenType,
                                            @Nonnull String text, BiPredicate<ReplacerConfig, PsrUser> filter) {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, filter);
        SimpleTextContainer container = new SimpleTextContainer(text);
        CaptureInfoImpl info = null;
        if (user.isCapturing(listenType)) {
            info = new CaptureInfoImpl();
            info.setTime(System.currentTimeMillis());
            info.setUser(user);
            info.setListenType(listenType);
            info.setJsons(new ArrayList<>());
        }

        container.createTexts(container);
        if (user.isCapturing(listenType)) {
            info.setTexts(container.getTexts());
            user.addCaptureInfo(listenType, info);
        }
        if (replacerManager.isTextBlocked(container, replacers)) {
            packetEvent.setCancelled(true);
            return null;
        }
        replacerManager.replaceContainerTexts(container, replacers);
        replacerManager.setPapi(user, container.getTexts());
        return container.getResult();
    }

    protected static boolean replaceItemStack(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user, @Nonnull ListenType listenType,
                                              @Nonnull ItemStack itemStack, BiPredicate<ReplacerConfig, PsrUser> filter) {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, filter);
        return replaceItemStack(packetEvent, user, listenType, itemStack, replacers);
    }

    protected static boolean replaceItemStack(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user, @Nonnull ListenType listenType,
                                              @Nonnull ItemStack itemStack, List<ReplacerConfig> replacers) {
        return replaceItemStack(packetEvent, user, listenType, itemStack, replacers, true);
    }

    protected static boolean replaceItemStack(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user, @Nonnull ListenType listenType,
                                              @Nonnull ItemStack itemStack, List<ReplacerConfig> replacers, boolean saveCache) {
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        ItemStack original = itemStack.clone();

        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        ItemStackContainer container = new ItemStackContainer(itemStack);

        if (!container.isFromCache()) {
            if (cacheItemStack(container, replacers)) {
                return true;
            }
        }
        if (container.getMetaCache().isBlocked()) {
            packetEvent.setCancelled(true);
            return true;
        }

        int[] papiIndexes = container.getMetaCache().getPlaceholderIndexes();
        if (papiIndexes.length != 0) {
            container.cloneItem();
            container.createDefaultChildren();
            container.createTexts(container);

            replacerManager.setPapi(user, container.getTexts(), papiIndexes);
        }
        container.getResult();

        if (saveCache && !original.isSimilar(itemStack)) {
            user.saveUserMetaCache(original, itemStack);
        }
        if (user.isCapturing(listenType)) {
            captureItemStackInfo(user, original, listenType);
        }
        return false;
    }

    private static void captureItemStackInfo(@Nonnull PsrUser user, @Nonnull ItemStack itemStack, @Nonnull ListenType listenType) {
        ItemStackContainer container = new ItemStackContainer(itemStack, false);
        CaptureInfo info = new CaptureInfoImpl();
        info.setTime(System.currentTimeMillis());
        info.setUser(user);
        info.setListenType(listenType);

        container.createDefaultChildren();
        container.createJsons(container);

        info.setJsons(container.getJsons());
        container.createTexts(container);
        info.setTexts(container.getTexts());
        user.addCaptureInfo(listenType, info);
    }

    private static boolean cacheItemStack(@Nonnull ItemStackContainer container, List<ReplacerConfig> replacers) {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();

        container.createDefaultChildren();
        container.createJsons(container);
        if (replacerManager.isJsonBlocked(container, replacers)) {
            container.getMetaCache().setBlocked(true);
            return true;
        }
        replacerManager.replaceContainerJsons(container, replacers);
        container.createTexts(container);
        if (replacerManager.isTextBlocked(container, replacers)) {
            container.getMetaCache().setBlocked(true);
            return true;
        }
        replacerManager.replaceContainerTexts(container, replacers);
        Integer[] ints = replacerManager.getPapiIndexes(container.getTexts()).toArray(new Integer[0]);
        int[] indexes = new int[ints.length];
        for (int i = 0; i < ints.length; i++) {
            indexes[i] = ints[i];
        }
        container.getMetaCache().setPlaceholderIndexes(indexes);
        container.getResult();
        return false;
    }

}
