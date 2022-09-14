package me.rothes.protocolstringreplacer.replacer.containers;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Content;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HoverEventContainer extends AbstractContainer<HoverEvent> {

    public HoverEventContainer(@NotNull HoverEvent hoverEvent) {
        super(hoverEvent);
    }

    public HoverEventContainer(@NotNull HoverEvent hoverEvent, @NotNull Container<?> root) {
        super(hoverEvent, root);
    }

    @Override
    public void createDefaultChildren() {
        if (ProtocolStringReplacer.getInstance().getServerMajorVersion() > 15 && !content.isLegacy()) {
            List<Content> contents = content.getContents();
            for (int i = 0; i < contents.size(); i++) {
                int finalI = i;
                children.add(new HoverContentContainer(contents.get(finalI), root) {
                    @Override
                    public Content getResult() {
                        Content result = super.getResult();
                        contents.set(finalI, result);
                        return result;
                    }
                });
            }
        } else {
            children.add(new ComponentsContainer(content.getValue(), root));
        }
        super.createDefaultChildren();
    }

}
