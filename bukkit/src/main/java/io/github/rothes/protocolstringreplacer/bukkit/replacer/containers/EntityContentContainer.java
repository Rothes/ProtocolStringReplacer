package io.github.rothes.protocolstringreplacer.bukkit.replacer.containers;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.hover.content.Entity;
import org.jetbrains.annotations.NotNull;

public class EntityContentContainer extends AbstractContainer<Entity> {

    public EntityContentContainer(@NotNull Entity entity) {
        super(entity);
    }

    public EntityContentContainer(@NotNull Entity entity, @NotNull Container<?> root) {
        super(entity, root);
    }

    @Override
    public void createDefaultChildren() {
        children.add(new ComponentContainer(content.getName(), root) {
            @Override
            public BaseComponent getResult() {
                BaseComponent result = super.getResult();
                EntityContentContainer.this.content.setName(result);
                return result;
            }
        });
        super.createDefaultChildren();
    }

}
