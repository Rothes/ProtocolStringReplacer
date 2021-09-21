package me.rothes.protocolstringreplacer.replacer.containers;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class ChatJsonContainer extends AbstractContainer<String> {

    private boolean createComponents = false;
    private ComponentsContainer componentsContainer = null;

    public ChatJsonContainer(@NotNull String json) {
        super(json);
    }

    public ChatJsonContainer(@NotNull String json, boolean createComponents) {
        super(json);
        this.createComponents = createComponents;
    }

    public ChatJsonContainer(@NotNull String json, @NotNull Container<?> root) {
        super(json, root);
    }

    public ChatJsonContainer(@NotNull String json, @NotNull Container<?> root, boolean createComponents) {
        super(json, root);
        this.createComponents = createComponents;
    }

    @Override
    public void createDefaultChildren() {
        if (createComponents) {
            componentsContainer = new ComponentsContainer(ComponentSerializer.parse(content), root);
            children.add(componentsContainer);
        }
        super.createDefaultChildren();
    }

    @Override
    public void createJsons(@NotNull Container<?> container) {
        super.createJsons(container);
        root.addJson(new ReplaceableImpl());
    }

    @Override
    public String getResult() {
        if (componentsContainer != null) {
            return ComponentSerializer.toString(componentsContainer.getResult());
        } else {
            return super.getResult();
        }
    }

    public ComponentsContainer getComponentsContainer() {
        return componentsContainer;
    }

    private class ReplaceableImpl implements Replaceable {

        @Override
        public String getText() {
            return content;
        }

        @Override
        public void setText(String text) {
            content = text;
        }

    }

}
