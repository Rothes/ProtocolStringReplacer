package me.rothes.protocolstringreplacer.replacer.containers;

import de.tr7zw.changeme.nbtapi.NBTContainer;
import org.jetbrains.annotations.NotNull;

public class SignNbtContainer extends AbstractContainer<NBTContainer> {

    public SignNbtContainer(@NotNull NBTContainer nbtContainer) {
        super(nbtContainer);
    }

    public SignNbtContainer(@NotNull NBTContainer nbtContainer, @NotNull Container<?> root) {
        super(nbtContainer, root);
    }

    @Override
    public void createDefaultChildren() {
        children.add(new ChatJsonContainer(content.toString(), root, false) {
            @Override
            public @NotNull String getResult() {
                String result = super.getResult();
                SignNbtContainer.this.content.clearNBT();
                SignNbtContainer.this.content.mergeCompound(new NBTContainer(result));
                return result;
            }
        });
    }

    public void entriesPeriod() {
        children.clear();
        jsonReplaceables.clear();
        String key;
        for (int i = 1; i <= 4; i++) {
            key = "Text" + i;
            String original = content.getString(key);
            if (original == null || original.equals("null")) {
                original = "{\"text\":\"\"}";
            }
            String finalKey = key;
            String finalOriginal = original;
            children.add(new ChatJsonContainer(finalOriginal, root, true) {
                @Override
                public @NotNull String getResult() {
                    String result = super.getResult();
                    if (!result.equals(finalOriginal)) {
                        SignNbtContainer.this.content.setString(finalKey, result);
                    }
                    return result;
                }
            });
        }
    }

    public String getNbtString() {
        return content.toString();
    }

    public void createDefaultChildrenDeep() {
        super.createDefaultChildren();
    }

}
