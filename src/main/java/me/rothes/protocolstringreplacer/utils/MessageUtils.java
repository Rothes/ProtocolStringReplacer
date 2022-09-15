package me.rothes.protocolstringreplacer.utils;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.PsrLocalization;
import me.rothes.protocolstringreplacer.api.capture.CaptureInfo;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang.Validate;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageUtils {

    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static void sendCaptureInfo(@Nonnull PsrUser user, @Nonnull CaptureInfo info) {
        Validate.notNull(info, "CaptureInfo cannot be null");

        String time = dateFormat.format(new Date(info.getTime()));
        ComponentBuilder hoverTextBuilder = new ComponentBuilder("").append("§3§l§m----------------------§3§l Captured Contents §m----------------------\n")
                .append("§b§lCommons: \n");
        for (String text : info.getTexts()) {
            hoverTextBuilder.append("§6§l- §r" + text + "\n");
        }
        String json = "";
        hoverTextBuilder.append("\n§b§lJsons: ");
        if (info.getJsons().isEmpty()) {
            hoverTextBuilder.append("§fN/A");
        } else {
            for (String json1 : info.getJsons()) {
                json = json1;
                hoverTextBuilder.append("§6§l- §r" + json + "\n");
            }
            hoverTextBuilder.append("§aClick to copy Json");
        }
        ClickEvent clickEvent = ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 15 ?
                new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, json) : new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, json);
        ComponentBuilder captureMessageBuilder = new ComponentBuilder("").append("§3 §l" + info.getListenType().getName() + "§3: §b" + time)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverTextBuilder.create()))
                .event(clickEvent);
        user.sendFilteredMessage(captureMessageBuilder.create());
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
