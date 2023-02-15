package me.rothes.protocolstringreplacer.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.KeybindComponent;
import net.md_5.bungee.api.chat.ScoreComponent;
import net.md_5.bungee.api.chat.SelectorComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.EntitySerializer;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.ItemSerializer;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.chat.hover.content.TextSerializer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.chat.KeybindComponentSerializer;
import net.md_5.bungee.chat.ScoreComponentSerializer;
import net.md_5.bungee.chat.SelectorComponentSerializer;
import net.md_5.bungee.chat.TextComponentSerializer;
import net.md_5.bungee.chat.TranslatableComponentSerializer;

import java.lang.reflect.Field;

public class SpigotUtils {

    private static final Gson psrSerializer;

    static {
        Gson temp = null;
        try {
            for (Field declaredField : ComponentSerializer.class.getDeclaredFields()) {
                if (declaredField.getType() == Gson.class) {
                    declaredField.setAccessible(true);
                    temp = (Gson) declaredField.get(null);
                    try {
                        temp = temp.newBuilder().disableHtmlEscaping().create();
                    } catch (NoSuchMethodError e) {
                        GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping();
                        try {
                            gsonBuilder.registerTypeAdapter(BaseComponent.class, new ComponentSerializer())
                                    .registerTypeAdapter(TextComponent.class, new TextComponentSerializer())
                                    .registerTypeAdapter(TranslatableComponent.class, new TranslatableComponentSerializer())
                                    .registerTypeAdapter(KeybindComponent.class, new KeybindComponentSerializer())
                                    .registerTypeAdapter(ScoreComponent.class, new ScoreComponentSerializer())
                                    .registerTypeAdapter(SelectorComponent.class, new SelectorComponentSerializer())
                                    .registerTypeAdapter(Entity.class, new EntitySerializer())
                                    .registerTypeAdapter(Text.class, new TextSerializer())
                                    .registerTypeAdapter(Item.class, new ItemSerializer())
                                    .registerTypeAdapter(ItemTag.class, new ItemTag.Serializer());
                        } catch (NoClassDefFoundError ignored) {
                            // Some types are not available on legacy servers.
                        }
                        temp = gsonBuilder.create();
                    }
                    break;
                }
            }
        } catch (Throwable e) {
            ProtocolStringReplacer.error("Unable to disableHtmlEscaping for SpigotComponentSerializer:", e);
        }
        psrSerializer = temp;
    }

    public static String serializeComponents(BaseComponent... components) {
        try {
            if (components.length == 1) {
                ProtocolStringReplacer.warn(components[0].getClass().getCanonicalName());
                return psrSerializer.toJson(components[0]);
            } else {
                return psrSerializer.toJson(new TextComponent(components));
            }
        } catch (Throwable t) {
            // ComponentSerializer Gson may be modified during Runtime.
            return ComponentSerializer.toString(components);
        }
    }

}
