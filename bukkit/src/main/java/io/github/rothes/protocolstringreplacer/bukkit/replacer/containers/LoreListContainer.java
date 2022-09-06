package io.github.rothes.protocolstringreplacer.bukkit.replacer.containers;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LoreListContainer extends AbstractContainer<List<String>> {

    public LoreListContainer(@NotNull List<String> lore) {
        super(lore);
    }

    public LoreListContainer(@NotNull List<String> lore, @NotNull Container<?> root) {
        super(lore, root);
    }

    @Override
    public void createDefaultChildren() {
        for (int i = 0; i < content.size(); i++) {
            int finalI = i;
            children.add(new SimpleTextContainer(content.get(finalI), root) {
                @Override
                public String getResult() {
                    String result = super.getResult();
                    LoreListContainer.this.content.set(finalI, result);
                    return result;
                }
            });
        }
        super.createDefaultChildren();
    }

}
