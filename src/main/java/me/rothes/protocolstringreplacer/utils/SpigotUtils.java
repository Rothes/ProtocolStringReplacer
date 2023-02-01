package me.rothes.protocolstringreplacer.utils;

import com.google.gson.Gson;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.lang.reflect.Field;

public class SpigotUtils {

    private static final Gson psrSerializer;

    static {
        Gson temp = null;
        try {
            for (Field declaredField : ComponentSerializer.class.getDeclaredFields()) {
                if (declaredField.getType() == Gson.class) {
                    declaredField.setAccessible(true);
                    Gson gson = (Gson) declaredField.get(null);
                    temp = gson.newBuilder().disableHtmlEscaping().create();
                    break;
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        psrSerializer = temp;
    }

    public static String serializeComponents(BaseComponent... components) {
        if ( components.length == 1 ) {
            return psrSerializer.toJson( components[0] );
        } else {
            return psrSerializer.toJson( new TextComponent( components ) );
        }
    }

}
