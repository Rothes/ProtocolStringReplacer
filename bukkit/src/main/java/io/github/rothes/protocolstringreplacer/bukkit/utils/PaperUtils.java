package io.github.rothes.protocolstringreplacer.bukkit.utils;

import io.papermc.paper.text.PaperComponents;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class PaperUtils {

    private static GsonComponentSerializer paperGsonComponentSerializer = PaperComponents.gsonSerializer();

    public static GsonComponentSerializer getPaperGsonComponentSerializer() {
        return paperGsonComponentSerializer;
    }

}
