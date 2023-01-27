package me.rothes.protocolstringreplacer.replacer.helpers;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTList;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemHelper {

    private Item item;

    private NBTContainer nbt;
    private NBTCompound display;
    private NBTList<String> loreNbt;

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
            helper.nbt = new NBTContainer(tag.getNbt());
            helper.display = helper.nbt.getCompound("display");
            if (helper.display != null) {
                if (helper.display.hasTag("Name")) {
                    helper.name = ComponentSerializer.parse(helper.display.getString("Name"));
                }
                if (helper.display.hasTag("Lore")) {
                    helper.loreNbt = helper.display.getStringList("Lore");
                    helper.lore = new ArrayList<>(helper.loreNbt.size());
                    for (String line : helper.loreNbt) {
                        helper.lore.add(ComponentSerializer.parse(line));
                    }
                }
            }
        }
        return helper;
    }

    public boolean hasName() {
        return name != null;
    }

    @Nullable
    public BaseComponent[] getName() {
        return name;
    }

    public void setName(BaseComponent[] name) {
        this.name = name;
        if (name != null) {
            String result = ComponentSerializer.toString(name);
            display.setString("Name", result);
        } else {
            display.removeKey("Name");
        }
    }

    public boolean hasLore() {
        return lore != null;
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
        loreNbt.set(line, result);
    }

    public void saveChanges() {
        if (display == null) {
            return;
        }
        item.setTag(ItemTag.ofNbt(nbt.toString()));
    }

}
