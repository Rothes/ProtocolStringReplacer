package me.rothes.protocolstringreplacer.utils;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.PsrLocalization;
import me.rothes.protocolstringreplacer.api.capture.CaptureInfo;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.Validate;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageUtils {

    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static void sendCaptureInfo(@Nonnull PsrUser user, @Nonnull CaptureInfo info, int index) {
        Validate.notNull(info, "CaptureInfo cannot be null");

        String time = dateFormat.format(new Date(info.getTime()));
        ComponentBuilder hoverTextBuilder = new ComponentBuilder("").append("§3§l§m----------------------§3§l Captured Contents §m----------------------\n")
                .append("§b§lCommons: \n");
        for (String text : info.getTexts()) {
            hoverTextBuilder.append("§6§l- ");
            hoverTextBuilder.append(ColorUtils.showColorCodes(text) + "\n").color(ChatColor.RESET);
        }
        hoverTextBuilder.append("\n§b§lJsons: " + (info.getJsons().isEmpty() ? "§fN/A\n" : "\n"));
        for (String json : info.getJsons()) {
            hoverTextBuilder.append("§6§l- ");
            hoverTextBuilder.append(ColorUtils.showColorCodes(json) + "\n").color(ChatColor.RESET);
        }
        hoverTextBuilder.append("\n§b§lDirects: " + (info.getJsons().isEmpty() ? "§fN/A\n" : "\n"));
        for (String direct : info.getDirects()) {
            hoverTextBuilder.append("§6§l- ");
            hoverTextBuilder.append(TextComponent.fromLegacyText(ColorUtils.showColorCodes(direct, true) + "\n"));
        }
        hoverTextBuilder.append("§aClick for clipboard");

        ComponentBuilder captureMessageBuilder = new ComponentBuilder("")
                .append("§3 §l" + info.getListenType().getName() + "§3: §b" + time
                        + (info.getCount() > 1 ? " §7x" + info.getCount() : ""))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverTextBuilder.create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/psr capture clipboard " + info.getListenType() + " " + index));
        user.sendFilteredMessage(captureMessageBuilder.create());
    }

    public static void sendCaptureInfoClipboard(@Nonnull PsrUser user, @Nonnull CaptureInfo info) {
        Validate.notNull(info, "CaptureInfo cannot be null");

        user.sendFilteredText("§b§lCommons: ");
        for (String text : info.getTexts()) {
            ClickEvent clickEvent = ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 15 ?
                    new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text) : new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, text);
            user.sendFilteredMessage(new ComponentBuilder("§6§l - ").event(clickEvent).append(ColorUtils.showColorCodes(text)).color(ChatColor.RESET).create());
        }
        user.sendFilteredText("§b§lJsons: " + (info.getJsons().isEmpty() ? "§fN/A" : ""));
        for (String json : info.getJsons()) {
            ClickEvent clickEvent = ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 15 ?
                    new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, json) : new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, json);
            user.sendFilteredMessage(new ComponentBuilder("§6§l - ").event(clickEvent).append(ColorUtils.showColorCodes(json)).color(ChatColor.RESET).create());
        }
        user.sendFilteredText("§b§lDirects: " + (info.getJsons().isEmpty() ? "§fN/A" : ""));
        for (String direct : info.getDirects()) {
            ClickEvent clickEvent = ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 15 ?
                    new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, direct) : new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, direct);
            user.sendFilteredMessage(new ComponentBuilder("§6§l - ").event(clickEvent).append(TextComponent.fromLegacyText(ColorUtils.showColorCodes(direct, true))).create());
        }
    }

    public static void sendPageButtons(@Nonnull PsrUser user, @Nonnull String command, int currentPage, int totalPage) {
        Validate.notNull(user, "PsrUser cannot be null");
        Validate.notNull(command, "Command String cannot be null");

        ComponentBuilder pageComponent = new ComponentBuilder("");
        if (currentPage > 1) {
            pageComponent.append(" ◀ ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    command + (currentPage - 1))).color(ChatColor.YELLOW);
        } else {
            pageComponent.append("   ");
        }
        pageComponent.append(PsrLocalization.getLocaledMessage("Utils.Message.Page-Info",
                String.valueOf(currentPage), String.valueOf(totalPage)));
        if (currentPage < totalPage) {
            pageComponent.append(" ▶ ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    command + (currentPage + 1))).color(ChatColor.YELLOW);
        }
        user.sendFilteredMessage(pageComponent.create());
    }

}
