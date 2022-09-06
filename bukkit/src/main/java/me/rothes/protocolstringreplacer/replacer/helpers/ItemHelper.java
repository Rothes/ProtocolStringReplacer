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
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemHelper {

    private Item item;

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
        Validate.notNull(item, "Item cannot be null");
        ItemHelper helper = new ItemHelper();

        helper.item = item;

        ItemTag tag = item.getTag();
        if (tag != null) {
            JsonElement element = new JsonParser().parse(tag.getNbt());
            helper.jsonElement = element;
            JsonObject root = element.getAsJsonObject();
            if (root != null) {
                helper.jsonDisplay = root.getAsJsonObject("display");
                if (helper.jsonDisplay != null) {

                    JsonPrimitive name = helper.jsonDisplay.getAsJsonPrimitive("Name");
                    helper.hasName = name != null;
                    if (helper.hasName) {
                        String diaplayNameJson = name.toString();
                        diaplayNameJson = diaplayNameJson.substring(1, diaplayNameJson.length() - 1);
                        helper.name = ComponentSerializer.parse(StringEscapeUtils.unescapeJava(diaplayNameJson));
                    }

                    helper.jsonLore = helper.jsonDisplay.getAsJsonArray("Lore");
                    helper.hasLore = helper.jsonLore != null;
                    if (helper.hasLore) {
                        helper.lore = new ArrayList<>(helper.jsonLore.size());
                        for (int i1 = 0; i1 < helper.jsonLore.size(); i1++) {
                            String loreJson = helper.jsonLore.get(i1).getAsJsonPrimitive().toString();
                            loreJson = loreJson.substring(1, loreJson.length() - 1);
                            helper.lore.add(i1, ComponentSerializer.parse(StringEscapeUtils.unescapeJava(loreJson)));
                        }
                    }
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
        if (name != null) {
            String result = ComponentSerializer.toString(name);
            this.jsonDisplay.add("Name", new JsonParser().parse("'" + result + "'"));
        } else {
            this.jsonDisplay.remove("Name");
        }
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

    public void saveChanges() {
        if (jsonDisplay == null) {
            return;
        }
        checkJson(jsonDisplay);
        item.setTag(ItemTag.ofNbt(jsonElement.toString()));
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
