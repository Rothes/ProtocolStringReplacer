package me.rothes.protocolstringreplacer.utils;

import me.rothes.protocolstringreplacer.PSRLocalization;
import me.rothes.protocolstringreplacer.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang.Validate;

import javax.annotation.Nonnull;

public class MessageUtils {

    public static void sendPageButtons(@Nonnull User user, @Nonnull String command, int currentPage, int totalPage) {
        Validate.notNull(user, "User cannot be null");
        Validate.notNull(command, "Command String cannot be null");

        ComponentBuilder pageComponent = new ComponentBuilder("");
        if (currentPage > 1) {
            pageComponent.append(" ◀ ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    command + (currentPage - 1))).color(ChatColor.YELLOW);
        } else {
            pageComponent.append("   ");
        }
        pageComponent.append(PSRLocalization.getLocaledMessage("Utils.Message.Page-Info",
                String.valueOf(currentPage), String.valueOf(totalPage)));
        if (currentPage < totalPage) {
            pageComponent.append(" ▶ ").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    command + (currentPage + 1))).color(ChatColor.YELLOW);
        }
        user.sendFilteredMessage(pageComponent.create());
    }

}
