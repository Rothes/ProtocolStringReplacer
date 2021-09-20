package me.rothes.protocolstringreplacer.replacer.containers;

import org.jetbrains.annotations.NotNull;

public class SimpleTextContainer extends AbstractContainer<String> {

    public SimpleTextContainer(@NotNull String string) {
        super(string);
    }

    public SimpleTextContainer(@NotNull String string, @NotNull Container<?> root) {
        super(string, root);
    }

    @Override
    public void createDefaultChildren() {
        // Nothing more to create here.
    }

    @Override
    public void createTexts(@NotNull Container<?> container) {
        super.createTexts(container);
        root.addText(new ReplaceableImpl());
    }

    @Override
    public String getResult() {
        return super.getResult();
    }

    private class ReplaceableImpl implements Replaceable {

        @Override
        public String getText() {
            return content;
        }

        @Override
        public void setText(String newText) {
            content = newText;
        }

    }

}
