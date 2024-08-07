package io.github.rothes.protocolstringreplacer.replacer.containers;

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
        if (!content.isEmpty()) {
            root.addText(new ReplaceableImpl());
        }
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

        @Override
        public String toString() {
            return content;
        }

    }

}
