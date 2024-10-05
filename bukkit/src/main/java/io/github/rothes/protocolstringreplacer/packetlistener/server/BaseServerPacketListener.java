package io.github.rothes.protocolstringreplacer.packetlistener.server;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.PsrLocalization;
import io.github.rothes.protocolstringreplacer.api.capture.CaptureInfo;
import io.github.rothes.protocolstringreplacer.api.capture.CaptureInfoImpl;
import io.github.rothes.protocolstringreplacer.api.exceptions.JsonSyntaxException;
import io.github.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.BasePacketListener;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;
import io.github.rothes.protocolstringreplacer.replacer.ReplaceMode;
import io.github.rothes.protocolstringreplacer.replacer.ReplacerManager;
import io.github.rothes.protocolstringreplacer.replacer.containers.ChatJsonContainer;
import io.github.rothes.protocolstringreplacer.replacer.containers.ItemStackContainer;
import io.github.rothes.protocolstringreplacer.replacer.containers.Replaceable;
import io.github.rothes.protocolstringreplacer.replacer.containers.SimpleTextContainer;
import io.github.rothes.protocolstringreplacer.util.SpigotUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public abstract class BaseServerPacketListener extends BasePacketListener {

    protected static final ProtocolStringReplacer plugin = ProtocolStringReplacer.getInstance();
    protected final BiPredicate<ReplacerConfig, PsrUser> filter;
    protected final ListenType listenType;
    private static final String DIRECT_NOT_REPLACED = "PSR Direct Not Replaced - 蔐魬鴯鋆颽漚铼";

    protected BaseServerPacketListener(PacketType packetType, ListenType listenType) {
        super(packetType);
        this.listenType = listenType;
        filter = (replacerConfig, user) -> containType(replacerConfig) && checkFilter(user, replacerConfig);
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

    @Override
    protected void register() {
        boolean enabled = ProtocolStringReplacer.getInstance().getConfig()
                .getBoolean("Options.Features.Packet-Listener.Listen-Type-Enabled." + listenType.getName(), false);
        if (enabled) {
            ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter);
        }
    }

    protected final boolean containType(ReplacerConfig replacerConfig) {
        return replacerConfig.getListenTypeList().contains(listenType);
    }

    protected final boolean checkFilter(PsrUser user, ReplacerConfig replacerConfig) {
        return replacerConfig.isEnabled() && checkPermission(user, replacerConfig) && replacerConfig.acceptsLocale(user.getClientLocale());
    }

    protected final boolean checkPermission(PsrUser user, ReplacerConfig replacerConfig) {
        String permission = replacerConfig.getPermissionLimit();
        return permission.isEmpty() || user.hasPermission(permission);
    }

    protected final boolean checkWindowTitle(PsrUser user, ReplacerConfig replacerConfig) {
        String currentWindowTitle = user.getCurrentWindowTitle();
        List<String> windowTitles = replacerConfig.getWindowTitleLimit();
        if (windowTitles.isEmpty()) {
            return true;
        }
        if (currentWindowTitle == null) {
            return replacerConfig.windowTitleLimitIgnoreInventory();
        } else {
            return windowTitles.contains(currentWindowTitle);
        }
    }

    protected static ChatJsonContainer deployContainer(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user,
                                                       @Nonnull String json, List<ReplacerConfig> replacers) {
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
            throw new JsonSyntaxException("Unable to create default children. Please check your Json format.\n"
                    + "Original Json: " + json + "\n"
                    + "Replaced Json: " + container.getJsons().get(0).getText() + "\n"
                    + "If you need support, please provide the stacktrace below.", t);
        }
        try {
            container.createTexts(container);
        } catch (Throwable t) {
            throw new JsonSyntaxException("Unable to create Texts. Please check your Json format.\n"
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
        String replacedDirect = getReplacedDirect(packetEvent, user, listenType, json, replacers);
        if (replacedDirect == null) {
            return null;
        }
        //noinspection StringEquality
        if (replacedDirect != DIRECT_NOT_REPLACED && plugin.getConfigManager().directSkips) {
            return SpigotUtils.serializeComponents(TextComponent.fromLegacyText(replacedDirect));
        }

        //noinspection StringEquality
        ChatJsonContainer container = deployContainer(packetEvent, user,
                (replacedDirect != DIRECT_NOT_REPLACED) ?
                        SpigotUtils.serializeComponents(TextComponent.fromLegacyText(replacedDirect)) : json, replacers);

        if (container != null) {
            return container.getResult();
        } else {
            return null;
        }
    }

    @Nullable
    protected static String getReplacedDirect(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user, @Nonnull ListenType listenType,
                                            @Nonnull String json, List<ReplacerConfig> replacers) {
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

            List<Replaceable> jsons = container.getJsons();
            for (Replaceable j : jsons) {
                StringBuilder sb1 = new StringBuilder();
                for (BaseComponent baseComponent : ComponentSerializer.parse(j.getText())) {
                    sb1.append(baseComponent.toLegacyText());
                }

                String ds = sb1.toString();
                String replaceDirect = ProtocolStringReplacer.getInstance().getReplacerManager().replaceDirect(ds, replacers);
                if (!replaceDirect.equals(ds)) {
                    j.setText(SpigotUtils.serializeComponents(TextComponent.fromLegacyText(replaceDirect)));
                }
            }

            info.setJsons(jsons);
            ProtocolStringReplacer.getInstance().getReplacerManager().replaceContainerJsons(container, replacers);
            try {
                container.createDefaultChildren();
            } catch (Throwable t) {
                throw new JsonSyntaxException("Unable to create default children. Please check your Json format.\n"
                        + "Original Json: " + json + "\n"
                        + "Replaced Json: " + jsons.get(0).getText() + "\n"
                        + "If you need support, please provide the stacktrace below.", t);
            }
            try {
                container.createTexts(container);
            } catch (Throwable t) {
                throw new JsonSyntaxException("Unable to create Texts. Please check your Json format.\n"
                        + "Original Json: " + json + "\n"
                        + "Replaced Json: " + jsons.get(0).getText() + "\n"
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
                                                                          @Nonnull String json, BiPredicate<ReplacerConfig, PsrUser> filter) {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, filter);
        String replacedJson = getReplacedJson(packetEvent, user, listenType, json, replacers);

        if (replacedJson != null) {
            return WrappedChatComponent.fromJson(replacedJson);
        } else {
            return null;
        }
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
            assert info != null;
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

    protected static ItemStack replaceItemStack(@Nonnull PacketEvent packetEvent, @Nonnull PsrUser user, @Nonnull ListenType listenType,
                                              @Nonnull ItemStack itemStack, List<ReplacerConfig> nbt,
                                              List<ReplacerConfig> lore, List<ReplacerConfig> entries, boolean saveUserCache) {
        try {
//        if (!itemStack.hasItemMeta()) {
//            return false;
//        }
            if (itemStack.getType() == Material.AIR) {
                return itemStack;
            }

            ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
            ItemStackContainer container = new ItemStackContainer(itemStack);

            if (!container.isFromCache() && cacheItemStack(container, nbt, lore, entries)) {
                cacheItemStack(container, nbt, lore, entries);
            }
            if (container.getMetaCache().isBlocked()) {
                packetEvent.setCancelled(true);
                return null;
            }

            int[] papiIndexes = container.getMetaCache().getPlaceholderIndexes();
            if (papiIndexes.length != 0) {
                if (!container.isFromCache()) {
                    // Otherwise this breaks data in cache
                    container = new ItemStackContainer(itemStack);
                }
                container.cloneItem();
                container.entriesPeriod();
                container.createDefaultChildrenDeep();
                container.createTexts(container);

                replacerManager.setPapi(user, container.getTexts(), papiIndexes);
            }
            ItemStack result = container.getResult();

            if (saveUserCache && !container.getOriginalNbtString().equals(container.getNbtString())) {
                user.saveUserItemRestoreCache(itemStack, result);
            }
            if (user.isCapturing(listenType)) {
                captureItemStackInfo(user, itemStack, listenType, nbt, lore, entries);
            }
            return result;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return itemStack;
    }

    private static void captureItemStackInfo(@Nonnull PsrUser user, @Nonnull ItemStack itemStack,
                                             @Nonnull ListenType listenType, List<ReplacerConfig> nbt,
                                             List<ReplacerConfig> lore, List<ReplacerConfig> entries) {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        ItemStackContainer container = new ItemStackContainer(itemStack, false);
        try {
            Material type = container.getItemType();
            CaptureInfo info = new CaptureInfoImpl();
            info.setTime(System.currentTimeMillis());
            info.setUser(user);
            info.setListenType(listenType);

            ComponentBuilder extraBuilder = new ComponentBuilder(PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Capture-Info.Extra-Prefix")).color(ChatColor.BLUE).bold(true).append("").reset();
            container.createDefaultChildren();
            container.createJsons(container);
            List<Replaceable> jsons = container.getJsons();
            extraBuilder.append("[Nbt Json] ").color(ChatColor.GOLD)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(jsons.get(0).getText() + "\n"
                            + PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Capture-Info.Click-To-Copy"))))
                    .event(new ClickEvent(ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 15 ?
                            ClickEvent.Action.COPY_TO_CLIPBOARD : ClickEvent.Action.SUGGEST_COMMAND, jsons.get(0).getText()));
            replacerManager.replaceJsonReplaceable(jsons.get(0), matchItemType(nbt, type));
            container.childrenResult();

//        if (container.displayNamePeriod()) {
//            container.createJsons(container);
//            jsons = container.getJsons();
//            extraBuilder.append("  ").reset().append("[Name Json] ").color(ChatColor.GOLD)
//                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(jsons.get(0).getText() + "\n"
//                            + PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Capture-Info.Click-To-Copy"))))
//                    .event(new ClickEvent(ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 15 ?
//                            ClickEvent.Action.COPY_TO_CLIPBOARD : ClickEvent.Action.SUGGEST_COMMAND, jsons.get(0).getText()));
//            ProtocolStringReplacer.getInstance().getReplacerManager().replaceJsonReplaceable(jsons.get(0), matchItemType(lore, type));
//            container.childrenResult();
//        }

            container.lorePeriod();
            container.createJsons(container);
            jsons = container.getJsons();
            extraBuilder.append("  ").reset().append("[Lore Json] ").color(ChatColor.GOLD)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(jsons.get(0).getText() + "\n"
                            + PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Capture-Info.Click-To-Copy"))))
                    .event(new ClickEvent(ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 15 ?
                            ClickEvent.Action.COPY_TO_CLIPBOARD : ClickEvent.Action.SUGGEST_COMMAND, jsons.get(0).getText()));
            info.setExtra(extraBuilder.create());
            ProtocolStringReplacer.getInstance().getReplacerManager().replaceJsonReplaceable(jsons.get(0), matchItemType(lore, type));
            container.childrenResult();

            container.entriesPeriod();
            container.createJsons(container);
            jsons = container.getJsons();

            List<String> originalJsons = jsons.stream().map(Replaceable::getText).collect(Collectors.toList());

            List<String> directs = new ArrayList<>(originalJsons.size());
            for (String json : originalJsons) {
                StringBuilder sb = new StringBuilder();
                for (BaseComponent baseComponent : ComponentSerializer.parse(json)) {
                    sb.append(baseComponent.toLegacyText());
                }

                directs.add(sb.toString());
            }
            info.setDirects(directs);

            List<ReplacerConfig> filtered = matchItemType(entries, type);
            for (Replaceable json : jsons) {
                StringBuilder sb = new StringBuilder();
                for (BaseComponent baseComponent : ComponentSerializer.parse(json.getText())) {
                    sb.append(baseComponent.toLegacyText());
                }

                String directString = sb.toString();
                String replaceDirect = ProtocolStringReplacer.getInstance().getReplacerManager().replaceDirect(directString, filtered);
                if (!replaceDirect.equals(directString)) {
                    json.setText(SpigotUtils.serializeComponents(TextComponent.fromLegacyText(replaceDirect)));
                }
            }

            info.setJsons(jsons);
            ProtocolStringReplacer.getInstance().getReplacerManager().replaceContainerJsons(container, filtered);
            try {
                container.createDefaultChildrenDeep();
            } catch (Throwable t) {
                throw new JsonSyntaxException("Unable to create default children.", t);
            }
            try {
                container.createTexts(container);
            } catch (Throwable t) {
                throw new JsonSyntaxException("Unable to create Texts.", t);
            }
            info.setTexts(container.getTexts());
            user.addCaptureInfo(listenType, info);
        } catch (Throwable t) {
            throw new JsonSyntaxException("Failed to handle ItemStack (capture). Please check your nbt format.\n"
                    + "Original Nbt: " + container.getOriginalNbtString() + "\n"
                    + "Last replaced Nbt: " + container.getNbtString() + "\n"
                    + "If you need support, please provide the stacktrace below.", t);
        }
    }

    private static boolean handleItemStackPeriod(ReplacerManager replacerManager, Material type,
                                                 ItemStackContainer container, List<ReplacerConfig> configs) {
        List<ReplacerConfig> filtered = matchItemType(configs, type);
        container.createJsons(container);
        if (checkBlocked(container.getJsons().get(0).getText(), filtered, replacerManager)) {
            container.getMetaCache().setBlocked(true);
            return true;
        }
        replacerManager.replaceJsonReplaceable(container.getJsons().get(0), filtered);
        container.childrenResult();
        return false;
    }

    private static boolean cacheItemStack(@Nonnull ItemStackContainer container, List<ReplacerConfig> nbt,
                                          List<ReplacerConfig> display, List<ReplacerConfig> entries) {
        try {
            ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
            Material type = container.getItemType();

            container.createDefaultChildren();
            if (handleItemStackPeriod(replacerManager, type, container, nbt)) {
                return true;
            }
            container.childrenResult();
//        if (container.displayNamePeriod()) {
//            if (handleItemStackPeriod(replacerManager, type, container, display)) {
//                return true;
//            }
//        }
            container.lorePeriod();
            if (handleItemStackPeriod(replacerManager, type, container, display)) {
                return true;
            }
            container.childrenResult();

            container.entriesPeriod();
            container.createJsons(container);

            boolean direct = false;

            List<ReplacerConfig> filtered = matchItemType(entries, type);
            for (Replaceable json : container.getJsons()) {
                StringBuilder sb = new StringBuilder();
                try {
                    for (BaseComponent baseComponent : ComponentSerializer.parse(json.getText())) {
                        sb.append(baseComponent.toLegacyText());
                    }
                } catch (Throwable t) {
                    throw new JsonSyntaxException("Failed to parse replaced lores.", t);
                }

                String directString = sb.toString();

                if (ProtocolStringReplacer.getInstance().getReplacerManager().isDirectBlocked(directString, filtered)) {
                    container.getMetaCache().setBlocked(true);
                    return true;
                }
                String replaceDirect = ProtocolStringReplacer.getInstance().getReplacerManager().replaceDirect(directString, filtered);
                if (!replaceDirect.equals(directString)) {
                    BaseComponent[] baseComponents = TextComponent.fromLegacyText(replaceDirect);
                    BaseComponent head = baseComponents[0];
                    // Consistent with Vanilla.
                    if (head.isBoldRaw() == null) {
                        head.setBold(false);
                    }
                    if (head.isUnderlinedRaw() == null) {
                        head.setUnderlined(false);
                    }
                    if (head.isStrikethroughRaw() == null) {
                        head.setStrikethrough(false);
                    }
                    if (head.isObfuscatedRaw() == null) {
                        head.setObfuscated(false);
                    }
                    // Must set false.
                    for (BaseComponent baseComponent : baseComponents) {
                        if (baseComponent.isItalicRaw() == null) {
                            baseComponent.setItalic(false);
                        }
                    }

                    json.setText(SpigotUtils.serializeComponents(baseComponents));
                    direct = true;
                }

            }
            if (direct && plugin.getConfigManager().directSkips) {
                container.createDefaultChildrenDeep();
                container.createTexts(container);
            } else {
                if (replacerManager.isJsonBlocked(container, entries)) {
                    container.getMetaCache().setBlocked(true);
                    return true;
                }
                replacerManager.replaceContainerJsons(container, entries);
                try {
                    container.createDefaultChildrenDeep();
                } catch (Throwable t) {
                    throw new JsonSyntaxException("Failed to create default children with replaced lores.", t);
                }
                try {
                    container.createTexts(container);
                } catch (Throwable t) {
                    throw new JsonSyntaxException("Failed to create texts with replaced lores.", t);
                }
                if (replacerManager.isTextBlocked(container, entries)) {
                    container.getMetaCache().setBlocked(true);
                    return true;
                }
                replacerManager.replaceContainerTexts(container, entries);
            }

            Integer[] ints = replacerManager.getPapiIndexes(container.getTexts()).toArray(new Integer[0]);
            int[] indexes = new int[ints.length];
            for (int i = 0; i < ints.length; i++) {
                indexes[i] = ints[i];
            }
            container.getMetaCache().setPlaceholderIndexes(indexes);
            container.getResult();
        } catch (Throwable t) {
            throw new JsonSyntaxException("Failed to handle ItemStack. Please check your nbt format.\n"
                    + "Original Nbt: " + container.getOriginalNbtString() + "\n"
                    + "Last replaced Nbt: " + container.getNbtString() + "\n"
                    + "If you need support, please provide the stacktrace below.", t);
        }
        return false;
    }

    private static List<ReplacerConfig> matchItemType(List<ReplacerConfig> raw, Material material) {
        List<ReplacerConfig> result = new ArrayList<>(raw.size());
        for (ReplacerConfig cfg : raw) {
            if (cfg.acceptedItemTypes().isEmpty() || cfg.acceptedItemTypes().contains(material)) {
                result.add(cfg);
            }
        }
        return result;
    }

    private static boolean checkBlocked(String json, List<ReplacerConfig> replacerConfigs, ReplacerManager replacerManager) {
        for (ReplacerConfig replacerConfig : replacerConfigs) {
            if (replacerManager.getBlocked(json, replacerConfig, ReplaceMode.JSON)) {
                return true;
            }
        }
        return false;
    }

}
