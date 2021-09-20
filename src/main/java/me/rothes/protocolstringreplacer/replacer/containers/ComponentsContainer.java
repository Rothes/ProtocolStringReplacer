package me.rothes.protocolstringreplacer.replacer.containers;

import net.md_5.bungee.api.chat.BaseComponent;
import org.jetbrains.annotations.NotNull;

public class ComponentsContainer extends AbstractContainer<BaseComponent[]> {

    public ComponentsContainer(@NotNull BaseComponent[] components) {
        super(components);
    }

    public ComponentsContainer(@NotNull BaseComponent[] components, @NotNull Container<?> root) {
        super(components, root);
    }

    @Override
    public void createDefaultChildren() {
        for (int i = 0; i < content.length; i++) {
            BaseComponent component = content[i];
            int finalI = i;
            children.add(new ComponentContainer(component, root) {
                @Override
                public BaseComponent getResult() {
                    BaseComponent result = super.getResult();
                    ComponentsContainer.this.content[finalI] = result;
                    return result;
                }
            });
        }
        super.createDefaultChildren();
    }

}
