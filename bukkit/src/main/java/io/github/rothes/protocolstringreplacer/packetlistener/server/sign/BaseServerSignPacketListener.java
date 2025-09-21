package io.github.rothes.protocolstringreplacer.packetlistener.server.sign;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NbtApiException;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.PsrLocalization;
import io.github.rothes.protocolstringreplacer.api.capture.CaptureInfoImpl;
import io.github.rothes.protocolstringreplacer.api.exceptions.JsonSyntaxException;
import io.github.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.packetlistener.server.BaseServerPacketListener;
import io.github.rothes.protocolstringreplacer.util.SpigotUtils;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;
import io.github.rothes.protocolstringreplacer.replacer.ReplacerManager;
import io.github.rothes.protocolstringreplacer.replacer.containers.Replaceable;
import io.github.rothes.protocolstringreplacer.replacer.containers.SignNbtContainer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public abstract class BaseServerSignPacketListener extends BaseServerPacketListener {

    protected BaseServerSignPacketListener(PacketType packetType) {
        super(packetType, ListenType.SIGN);
    }

    protected void replaceSign(@NotNull PacketEvent packetEvent, @NotNull NBTContainer nbtContainer, @NotNull PsrUser user, @NotNull BiPredicate<ReplacerConfig, PsrUser> filter) {
        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, filter);
        CaptureInfoImpl info = null;

        SignNbtContainer container = new SignNbtContainer(nbtContainer);
        container.createDefaultChildren();
        container.createJsons(container);
        List<Replaceable> jsons = container.getJsons();
        String originalNbt = jsons.get(0).getText();
        if (user.isCapturing(listenType)) {
            info = new CaptureInfoImpl();
            info.setTime(System.currentTimeMillis());
            info.setUser(user);
            info.setListenType(listenType);
            ComponentBuilder extraBuilder = new ComponentBuilder(PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Capture-Info.Extra-Prefix"))
                    .color(ChatColor.BLUE).bold(true).append("").reset();
            extraBuilder.append("[Nbt Json] ").color(ChatColor.GOLD)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(originalNbt + "\n"
                            + PsrLocalization.getLocaledMessage("Sender.Commands.Capture.Capture-Info.Click-To-Copy"))))
                    .event(new ClickEvent(ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 15 ?
                            ClickEvent.Action.COPY_TO_CLIPBOARD : ClickEvent.Action.SUGGEST_COMMAND, originalNbt));
            info.setExtra(extraBuilder.create());
        }

        replacerManager.replaceJsonReplaceable(jsons.get(0), replacers);
        try {
            container.getResult();
        } catch (NbtApiException exception) {
            throw new JsonSyntaxException("Unable to parse Sign Nbt Json. Please check your Json format.\n"
                    + "Original Nbt Json: " + originalNbt + "\n"
                    + "Replaced Nbt Json: " + jsons.get(0).getText() + "\n"
                    + "If you need support, please provide the stacktrace below.", exception);
        }
        container.entriesPeriod();
        container.createJsons(container);

        jsons = container.getJsons();
        List<String> originalJsons = jsons.stream().map(Replaceable::getText).collect(Collectors.toList());

        List<String> directs = new ArrayList<>(originalJsons.size());
        for (String json : originalJsons) {
            StringBuilder sb = new StringBuilder();
            try {
                for (BaseComponent baseComponent : SpigotUtils.parseComponents(json)) {
                    if (baseComponent == null) continue; // If text is empty
                    sb.append(baseComponent.toLegacyText());
                }
            } catch (Throwable t) {
                throw new JsonSyntaxException("Unable to parse Sign Nbt Json. Please check your Json format.\n"
                        + "Original Nbt Json: " + originalNbt + "\n"
                        + "Replaced Nbt Json: " + container.getNbtString() + "\n"
                        + "If you need support, please provide the stacktrace below.", t);
            }

            directs.add(sb.toString());
        }

        if (user.isCapturing(listenType)) {
            assert info != null;
            info.setDirects(directs);
        }

        for (int i = 0; i < directs.size(); i++) {
            String directString = directs.get(i);
            if (replacerManager.isDirectBlocked(directString, replacers)) {
                packetEvent.setCancelled(true);
                return;
            }
            String replaceDirect = replacerManager.replaceDirect(directString, replacers);
            if (!replaceDirect.equals(directString)) {
                jsons.get(i).setText(SpigotUtils.serializeComponents(TextComponent.fromLegacyText(replaceDirect)));
            }
        }

        if (user.isCapturing(listenType)) {
            assert info != null;
            info.setJsons(jsons);
        }

        if (replacerManager.isJsonBlocked(container, replacers)) {
            packetEvent.setCancelled(true);
            return;
        }
        replacerManager.replaceContainerJsons(container, replacers);

        try {
            container.createDefaultChildrenDeep();
        } catch (Throwable t) {
            throw new JsonSyntaxException("Unable to create default children. Please check your Json format.\n"
                    + "Original Jsons: " + originalJsons + "\n"
                    + "Replaced Jsons: " + jsons + "\n"
                    + "If you need support, please provide the stacktrace below.", t);
        }
        try {
            container.createTexts(container);
        } catch (Throwable t) {
            throw new JsonSyntaxException("Unable to create Texts. Please check your Json format.\n"
                    + "Original Jsons: " + originalJsons + "\n"
                    + "Replaced Jsons: " + jsons + "\n"
                    + "If you need support, please provide the stacktrace below.", t);
        }

        if (user.isCapturing(listenType)) {
            assert info != null;
            info.setTexts(container.getTexts());
            user.addCaptureInfo(listenType, info);
        }

        if (replacerManager.isTextBlocked(container, replacers)) {
            packetEvent.setCancelled(true);
            return;
        }
        replacerManager.replaceContainerTexts(container, replacers);
        container.getResult();
    }

}
