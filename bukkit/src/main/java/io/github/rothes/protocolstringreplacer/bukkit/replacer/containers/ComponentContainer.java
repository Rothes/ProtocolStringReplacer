package io.github.rothes.protocolstringreplacer.bukkit.replacer.containers;

import io.github.rothes.protocolstringreplacer.bukkit.utils.ColorUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ComponentContainer extends AbstractContainer<BaseComponent> {

    public ComponentContainer(@NotNull BaseComponent component) {
        super(component);
    }

    public ComponentContainer(@NotNull BaseComponent component, @NotNull Container<?> root) {
        super(component, root);
    }

    @Override
    public void createDefaultChildren() {
        if (content instanceof TextComponent) {
            TextComponent textComponent = (TextComponent) content;
            String color = ColorUtils.getTextColor(textComponent);
            children.add(new SimpleTextContainer(color + textComponent.getText(), root) {
                @Override
                public String getResult() {
                    String result = super.getResult();
                    int length = color.length();
                    if (result.substring(0, length).equals(color)) {
                        textComponent.setText(result.substring(length));
                    } else {
                        textComponent.setText(result);
                    }
                    return result;
                }
            });
        } else if (content instanceof TranslatableComponent) {
            TranslatableComponent translatableComponent = (TranslatableComponent) content;
            List<BaseComponent> with = translatableComponent.getWith();
            if (with != null) {
                for (BaseComponent component : with) {
                    children.add(new ComponentContainer(component, root));
                }
            }
        }

        HoverEvent hoverEvent = content.getHoverEvent();
        if (hoverEvent != null) {
            children.add(new HoverEventContainer(hoverEvent, root));
        }

        List<BaseComponent> extra = content.getExtra();
        if (extra != null) {
            for (BaseComponent component : extra) {
                children.add(new ComponentContainer(component, root));
            }
        }
        super.createDefaultChildren();
    }

}
