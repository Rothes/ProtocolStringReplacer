package io.github.rothes.protocolstringreplacer.replacer.containers;

import io.github.rothes.protocolstringreplacer.replacer.helpers.ItemHelper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.jetbrains.annotations.NotNull;

public class ItemContentContainer extends AbstractContainer<Item> {

    protected ItemHelper helper;

    public ItemContentContainer(@NotNull Item item) {
        super(item);
    }

    public ItemContentContainer(@NotNull Item item, @NotNull Container<?> root) {
        super(item, root);
    }

    @Override
    public void createDefaultChildren() {
        helper = ItemHelper.parse(content);
        if (helper.hasName()) {
            children.add(new ComponentsContainer(helper.getName(), root) {
                @Override
                public BaseComponent[] getResult() {
                    BaseComponent[] result = super.getResult();
                    helper.setName(result);
                    return result;
                }
            });
        }
        if (helper.hasLore()) {
            int size = helper.getLoreSize();
            for (int line = 0; line < size; line++) {
                int finalLine = line;
                children.add(new ComponentsContainer(helper.getLore(line), root) {
                    @Override
                    public BaseComponent[] getResult() {
                        BaseComponent[] result = super.getResult();
                        helper.setLore(finalLine, result);
                        return result;
                    }
                });
            }
        }
        super.createDefaultChildren();
    }

    @Override
    public Item getResult() {
        Item result = super.getResult();
        helper.saveChanges();
        return result;
    }

}
