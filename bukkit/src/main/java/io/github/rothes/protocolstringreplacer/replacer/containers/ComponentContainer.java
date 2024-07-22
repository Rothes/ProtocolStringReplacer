package io.github.rothes.protocolstringreplacer.replacer.containers;

import io.github.rothes.protocolstringreplacer.util.ColorUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;

public class ComponentContainer extends AbstractContainer<BaseComponent> {

    private static final boolean TRANSLATE_FALLBACK = supportFallback();

    private static boolean supportFallback() {
        for (Method method : TranslatableComponent.class.getDeclaredMethods()) {
            if (method.getName().equals("getFallback")) {
                return true;
            }
        }

        return false;
    }

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
                    if (result.length() >= length && result.substring(0, length).equals(color)) {
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

            if (TRANSLATE_FALLBACK && translatableComponent.getFallback() != null) {
                children.add(new SimpleTextContainer(translatableComponent.getFallback(), root) {
                    @Override
                    public @NotNull String getResult() {
                        String result = super.getResult();
                        translatableComponent.setFallback(result);
                        return result;
                    }
                });
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
