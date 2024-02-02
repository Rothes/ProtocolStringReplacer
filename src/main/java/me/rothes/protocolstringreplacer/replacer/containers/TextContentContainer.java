package me.rothes.protocolstringreplacer.replacer.containers;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.jetbrains.annotations.NotNull;

public class TextContentContainer extends AbstractContainer<Text> {

    public TextContentContainer(@NotNull Text text) {
        super(text);
    }

    public TextContentContainer(@NotNull Text text, @NotNull Container<?> root) {
        super(text, root);
    }

    @Override
    public void createDefaultChildren() {
        Object object = content.getValue();
        if (object instanceof BaseComponent[]) {
            children.add(new ComponentsContainer((BaseComponent[]) object, root) {
                @Override
                public BaseComponent[] getResult() {
                    BaseComponent[] result = super.getResult();
                    TextContentContainer.this.content = new Text(result);
                    return result;
                }
            });
        } else {
            children.add(new SimpleTextContainer((String) object, root) {
                @Override
                public String getResult() {
                    String result = super.getResult();
                    TextContentContainer.this.content = new Text(result);
                    return result;
                }
            });
        }
        super.createDefaultChildren();
    }

}
