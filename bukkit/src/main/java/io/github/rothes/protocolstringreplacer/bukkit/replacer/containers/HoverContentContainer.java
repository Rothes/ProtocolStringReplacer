package io.github.rothes.protocolstringreplacer.bukkit.replacer.containers;

import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.jetbrains.annotations.NotNull;

public class HoverContentContainer extends AbstractContainer<Content> {

    public HoverContentContainer(@NotNull Content content) {
        super(content);
    }

    public HoverContentContainer(@NotNull Content content, @NotNull Container<?> root) {
        super(content, root);
    }

    @Override
    public void createDefaultChildren() {
        if (content instanceof Text) {
            children.add(new TextContentContainer((Text) content, root) {
                @Override
                public Text getResult() {
                    Text result = super.getResult();
                    content = result;
                    return result;
                }
            });
        } else if (content instanceof Item) {
            children.add(new ItemContentContainer((Item) content, root) {
                @Override
                public Item getResult() {
                    Item result = super.getResult();
                    content = result;
                    return result;
                }
            });
        } else if (content instanceof Entity) {
            children.add(new EntityContentContainer((Entity) content, root) {
                @Override
                public Entity getResult() {
                    Entity result = super.getResult();
                    content = result;
                    return result;
                }
            });
        }
        super.createDefaultChildren();
    }

}
