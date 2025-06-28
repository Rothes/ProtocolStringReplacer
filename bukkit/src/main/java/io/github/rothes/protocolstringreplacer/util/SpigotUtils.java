package io.github.rothes.protocolstringreplacer.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
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
import net.md_5.bungee.chat.ChatVersion;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.chat.KeybindComponentSerializer;
import net.md_5.bungee.chat.ScoreComponentSerializer;
import net.md_5.bungee.chat.SelectorComponentSerializer;
import net.md_5.bungee.chat.TextComponentSerializer;
import net.md_5.bungee.chat.TranslatableComponentSerializer;
import net.md_5.bungee.chat.VersionedComponentSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class SpigotUtils {

    private static final Gson psrSerializer;
    private static final JsonParser jsonParser = new JsonParser();

    static {
        Gson temp = null;
        ProtocolStringReplacer plugin = ProtocolStringReplacer.getInstance();
        try {
            Class<?> clazz;
            Object instance = null;
            if (plugin.getServerMajorVersion() == 21 && plugin.getServerMinorVersion() >= 5
                    || plugin.getServerMinorVersion() > 21) {
                clazz = VersionedComponentSerializer.class;
                instance = VersionedComponentSerializer.forVersion(ChatVersion.V1_21_5);
            } else {
                clazz = ComponentSerializer.class;
            }
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.getType() == Gson.class) {
                    declaredField.setAccessible(true);
                    temp = (Gson) declaredField.get(instance);
                    try {
                        temp = temp.newBuilder().disableHtmlEscaping().create();
                    } catch (NoSuchMethodError e) {
                        GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping();
                        try {
                            gsonBuilder.registerTypeAdapter(BaseComponent.class, new ComponentSerializer())
                                    .registerTypeAdapter(TextComponent.class, newInstance(TextComponentSerializer.class))
                                    .registerTypeAdapter(TranslatableComponent.class, newInstance(TranslatableComponentSerializer.class))
                                    .registerTypeAdapter(KeybindComponent.class, newInstance(KeybindComponentSerializer.class))
                                    .registerTypeAdapter(ScoreComponent.class, newInstance(ScoreComponentSerializer.class))
                                    .registerTypeAdapter(SelectorComponent.class, newInstance(SelectorComponentSerializer.class))
                                    .registerTypeAdapter(Entity.class, newInstance(EntitySerializer.class))
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

    public static BaseComponent[] parseComponents(String json) {
        try {
            JsonElement jsonElement = jsonParser.parse(json);
            if (jsonElement.isJsonArray()) {
                return psrSerializer.fromJson(jsonElement, BaseComponent[].class);
            } else {
                return new BaseComponent[] { psrSerializer.fromJson(jsonElement, BaseComponent.class) };
            }

        } catch (Throwable t) {
            return ComponentSerializer.parse(json);
        }
    }

    public static String serializeComponents(BaseComponent... components) {
        try {
            if (components.length == 1) {
                return psrSerializer.toJson(components[0]);
            } else {
                return psrSerializer.toJson(new TextComponent(components));
            }
        } catch (Throwable t) {
            // ComponentSerializer Gson may be modified during Runtime.
            return ComponentSerializer.toString(components);
        }
    }

    @SuppressWarnings({"deprecation"})
    public static boolean compareComponents(BaseComponent[] a, BaseComponent[] b) {
        if (a == null && b != null || a != null && b == null) {
            return false;
        }
        if (a == null) {
            return true;
        }
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0, length = a.length; i < length; i++) {
            BaseComponent component = a[i];
            BaseComponent other = b[i];
            if (!component.toLegacyText().equals(other.toLegacyText())) {
                return false;
            } else if (component.getHoverEvent() != other.getHoverEvent() && component.getHoverEvent() != null
                    && !compareComponents(component.getHoverEvent().getValue(), other.getHoverEvent().getValue())) {
                return false;
            }
        }
        return true;
    }

    private static Object newInstance(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

}
