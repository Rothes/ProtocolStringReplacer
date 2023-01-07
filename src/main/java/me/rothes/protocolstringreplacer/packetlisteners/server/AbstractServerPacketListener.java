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
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public abstract class AbstractServerPacketListener extends AbstractPacketListener {

    protected final BiPredicate<ReplacerConfig, PsrUser> filter;
    protected final ListenType listenType;
    private static final String DIRECT_NOT_REPLACED = "PSR Direct Not Replaced - 蔐魬鴯鋆颽漚铼";

    protected AbstractServerPacketListener(PacketType packetType, ListenType listenType) {
        super(packetType);
        this.listenType = listenType;
        filter = (replacerConfig, user) -> containType(replacerConfig) && checkPermission(user, replacerConfig);
        packetAdapter = new PacketAdapter(ProtocolStringReplacer.getInstance(), ProtocolStringReplacer.getInstance().getPacketListenerManager().getListenerPriority(), packetType) {
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
                                                       @Nonnull String json, List<ReplacerConfig> replacers, boolean saveTitle) {
        boolean blocked = false;
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();

        ChatJsonContainer container = new ChatJsonContainer(json, true);

        container.createJsons(container);
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
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, filter);
        return getReplacedJson(packetEvent, user, listenType, json, replacers);
    }

    @Nullable
    protected static String getReplacedJson(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user, @Nonnull ListenType listenType,
                                            @Nonnull String json, List<ReplacerConfig> replacers) {
        String replacedDirect = getReplacedDirect(packetEvent, user, listenType, json, replacers, false);
        if (replacedDirect == null) {
            return null;
        }
        //noinspection StringEquality
        if (replacedDirect != DIRECT_NOT_REPLACED) {
            return ComponentSerializer.toString(TextComponent.fromLegacyText(replacedDirect));
        }

        ChatJsonContainer container = deployContainer(packetEvent, user, listenType, json, replacers, false);

        if (container != null) {
            return container.getResult();
        } else {
            return null;
        }
    }

    @Nullable
    protected static String getReplacedDirect(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user, @Nonnull ListenType listenType,
                                            @Nonnull String json, List<ReplacerConfig> replacers, boolean saveTitle) {
        if (saveTitle) {
            user.setCurrentWindowTitle(json);
        }

        StringBuilder sb = new StringBuilder();
        for (BaseComponent baseComponent : ComponentSerializer.parse(json)) {
            sb.append(baseComponent.toLegacyText());
        }

        String directString = sb.toString();

        if (user.isCapturing(listenType)) {
            CaptureInfo info = new CaptureInfoImpl();
            info.setTime(System.currentTimeMillis());
            info.setUser(user);
            info.setListenType(listenType);

            ArrayList<String> directs = new ArrayList<>();
            directs.add(directString);
            info.setDirects(directs);

            ChatJsonContainer container = new ChatJsonContainer(json, true);
            container.createJsons(container);
            info.setJsons(container.getJsons());
            ProtocolStringReplacer.getInstance().getReplacerManager().replaceContainerJsons(container, replacers);
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
            } catch (Throwable t) {
                throw new RuntimeException("Unable to create Texts. Please check your Json format.\n"
                        + "Original Json: " + json + "\n"
                        + "Replaced Json: " + container.getJsons().get(0).getText() + "\n"
                        + "If you need support, please provide the stacktrace below.", t);
            }

            info.setTexts(container.getTexts());

            user.addCaptureInfo(listenType, info);
        }

        if (ProtocolStringReplacer.getInstance().getReplacerManager().isDirectBlocked(directString, replacers)) {
            packetEvent.setCancelled(true);
            return null;
        }
        String replaceDirect = ProtocolStringReplacer.getInstance().getReplacerManager().replaceDirect(directString, replacers);
        return replaceDirect.equals(directString) ? DIRECT_NOT_REPLACED : replaceDirect;
    }

    @Nullable
    protected static WrappedChatComponent getReplacedJsonWrappedComponent(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user, @Nonnull ListenType listenType,
                                                                          @Nonnull String json, BiPredicate<ReplacerConfig, PsrUser> filter, boolean saveTitle) {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, filter);
        ChatJsonContainer container = deployContainer(packetEvent, user, listenType, json, replacers, saveTitle);

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
            info.setDirects(Collections.emptyList());
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

        boolean fromCache = container.isFromCache();
        if (!fromCache) {
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
            if (fromCache) {
                container.createDefaultChildren();
                container.createDefaultChildrenDeep();
                container.createTexts(container);
            }

            replacerManager.setPapi(user, container.getTexts(), papiIndexes);
        }
        container.getResult();

        if (saveCache && !original.isSimilar(itemStack)) {
            user.saveUserMetaCache(original, itemStack);
        }
        if (user.isCapturing(listenType)) {
            captureItemStackInfo(user, original, listenType, replacers);
        }
        return false;
    }

    private static void captureItemStackInfo(@Nonnull PsrUser user, @Nonnull ItemStack itemStack,
                                             @Nonnull ListenType listenType, List<ReplacerConfig> replacers) {
        ItemStackContainer container = new ItemStackContainer(itemStack, false);
        CaptureInfo info = new CaptureInfoImpl();
        info.setTime(System.currentTimeMillis());
        info.setUser(user);
        info.setListenType(listenType);

        container.createDefaultChildren();
        container.createJsons(container);
        List<String> originalJsons = container.getJsons().stream().map(Replaceable::getText).collect(Collectors.toList());

        List<String> directs = new ArrayList<>(originalJsons.size());
        for (String json : originalJsons) {
            StringBuilder sb = new StringBuilder();
            for (BaseComponent baseComponent : ComponentSerializer.parse(json)) {
                sb.append(baseComponent.toLegacyText());
            }

            directs.add(sb.toString());
        }
        info.setDirects(directs);

        info.setJsons(container.getJsons());
        ProtocolStringReplacer.getInstance().getReplacerManager().replaceContainerJsons(container, replacers);
        try {
            container.createDefaultChildrenDeep();
        } catch (Throwable t) {
            throw new RuntimeException("Unable to create default children. Please check your Json format.\n"
                    + "Original Jsons: " + originalJsons + "\n"
                    + "Replaced Jsons: " + container.getJsons() + "\n"
                    + "If you need support, please provide the stacktrace below.", t);
        }
        try {
            container.createTexts(container);
        } catch (Throwable t) {
            throw new RuntimeException("Unable to create Texts. Please check your Json format.\n"
                    + "Original Jsons: " + originalJsons + "\n"
                    + "Replaced Jsons: " + container.getJsons() + "\n"
                    + "If you need support, please provide the stacktrace below.", t);
        }
        info.setTexts(container.getTexts());
        user.addCaptureInfo(listenType, info);
    }

    private static boolean cacheItemStack(@Nonnull ItemStackContainer container, List<ReplacerConfig> replacers) {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();

        container.createDefaultChildren();
        container.createJsons(container);

        for (Replaceable json : container.getJsons()) {
            StringBuilder sb = new StringBuilder();
            for (BaseComponent baseComponent : ComponentSerializer.parse(json.getText())) {
                sb.append(baseComponent.toLegacyText());
            }

            String directString = sb.toString();

            if (ProtocolStringReplacer.getInstance().getReplacerManager().isDirectBlocked(directString, replacers)) {
                container.getMetaCache().setBlocked(true);
                return true;
            }
            String replaceDirect = ProtocolStringReplacer.getInstance().getReplacerManager().replaceDirect(directString, replacers);
            if (!replaceDirect.equals(directString)) {
                json.setText(ComponentSerializer.toString(TextComponent.fromLegacyText(replaceDirect)));
                container.getMetaCache().setDirect(true);
            }

        }
        if (!container.getMetaCache().isDirect()) {
            if (replacerManager.isJsonBlocked(container, replacers)) {
                container.getMetaCache().setBlocked(true);
                return true;
            }
            List<String> originalJsons = container.getJsons().stream().map(Replaceable::getText).collect(Collectors.toList());
            replacerManager.replaceContainerJsons(container, replacers);
            try {
                container.createDefaultChildrenDeep();
            } catch (Throwable t) {
                throw new RuntimeException("Unable to create default children. Please check your Json format.\n"
                        + "Original Jsons: " + originalJsons + "\n"
                        + "Replaced Jsons: " + container.getJsons() + "\n"
                        + "If you need support, please provide the stacktrace below.", t);
            }
            try {
                container.createTexts(container);
            } catch (Throwable t) {
                throw new RuntimeException("Unable to create Texts. Please check your Json format.\n"
                        + "Original Jsons: " + originalJsons + "\n"
                        + "Replaced Jsons: " + container.getJsons() + "\n"
                        + "If you need support, please provide the stacktrace below.", t);
            }
            if (replacerManager.isTextBlocked(container, replacers)) {
                container.getMetaCache().setBlocked(true);
                return true;
            }
            replacerManager.replaceContainerTexts(container, replacers);
        } else {
            container.createDefaultChildrenDeep();
            container.createTexts(container);
        }
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
