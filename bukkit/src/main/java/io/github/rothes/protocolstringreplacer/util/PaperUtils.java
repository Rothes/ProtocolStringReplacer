package io.github.rothes.protocolstringreplacer.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.papermc.paper.text.PaperComponents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class PaperUtils {

    private static final GsonComponentSerializer paperGsonComponentSerializer = PaperComponents.gsonSerializer();
    private static final Gson psrSerializer = paperGsonComponentSerializer.populator().apply(new GsonBuilder().disableHtmlEscaping()).create();

    public static GsonComponentSerializer getPaperGsonComponentSerializer() {
        return paperGsonComponentSerializer;
    }

    public static String serializeComponent(Component component) {
        return psrSerializer.toJson(component);
    }

}
