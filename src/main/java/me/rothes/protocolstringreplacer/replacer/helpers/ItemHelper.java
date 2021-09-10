package me.rothes.protocolstringreplacer.replacer.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemHelper {

    private String id = null;
    private Integer count = null;

    private JsonElement jsonElement = null;
    private JsonObject jsonDisplay = null;
    private JsonArray jsonLore = null;

    private boolean hasName = false;
    private boolean hasLore = false;

    private BaseComponent[] name = null;
    private List<BaseComponent[]> lore = null;

    private ItemHelper() {

    }

    @NotNull
    public static ItemHelper parse(@Nonnull Item item) {
        Validate.notNull(item, "ItemTag cannot be null");
        ItemHelper helper = new ItemHelper();

        helper.id = item.getId();
        helper.count = item.getCount();
        JsonElement element = new JsonParser().parse(item.getTag().getNbt());
        helper.jsonElement = element;
        JsonObject root = element.getAsJsonObject();
        JsonObject display = root.getAsJsonObject("display");
        if (display != null) {
            helper.jsonDisplay = display;
            JsonPrimitive name = display.getAsJsonPrimitive("Name");
            helper.hasName = name != null;
            if (helper.hasName) {
                String diaplayNameJson = name.toString();
                diaplayNameJson = diaplayNameJson.substring(1, diaplayNameJson.length() - 1);
                helper.name = ComponentSerializer.parse(StringEscapeUtils.unescapeJson(diaplayNameJson));
            }

            JsonArray lore = display.getAsJsonArray("Lore");
            helper.hasLore = lore != null;
            if (helper.hasLore) {
                helper.jsonLore = lore;
                helper.lore = new ArrayList<>(lore.size());
                for (int i1 = 0; i1 < lore.size(); i1++) {
                    String loreJson = lore.get(i1).getAsJsonPrimitive().toString();
                    loreJson = loreJson.substring(1, loreJson.length() - 1);
                    helper.lore.set(i1, ComponentSerializer.parse(StringEscapeUtils.unescapeJson(loreJson)));
                }
            }
        }
        return helper;
    }

    public boolean hasName() {
        return hasName;
    }

    @Nullable
    public BaseComponent[] getName() {
        return name;
    }

    public void setName(BaseComponent[] name) {
        this.name = name;
        String result = ComponentSerializer.toString(name);
        this.jsonDisplay.add("Name", new JsonParser().parse("'" + result + "'"));
    }

    public boolean hasLore() {
        return hasLore;
    }

    public int getLoreSize() {
        return this.lore.size();
    }

    public BaseComponent[] getLore(int line) {
        return this.lore.get(line);
    }

    public void setLore(int line, BaseComponent[] loreLine) {
        this.lore.set(line, loreLine);
        String result = ComponentSerializer.toString(loreLine);
        this.jsonLore.set(line, new JsonParser().parse("'" + result + "'"));
    }

    public Item getItem() {
        checkJson(jsonElement.getAsJsonObject());
        return new Item(id, count, ItemTag.ofNbt(jsonElement.toString()));
    }

    private void checkJson(@NotNull JsonObject root) {
        JsonObject skullOwner = root.getAsJsonObject("SkullOwner");
        if (skullOwner != null) {
            JsonArray id = skullOwner.getAsJsonArray("Id");
            if (id != null && id.size() == 5) {
                id.set(0, new JsonPrimitive(Integer.valueOf(id.get(1).getAsJsonPrimitive().toString())));
                id.set(1, id.get(2));
                id.set(2, id.get(3));
                id.remove(3);
            }
        }
    }

}
