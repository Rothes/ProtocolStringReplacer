package io.github.rothes.protocolstringreplacer.bukkit.replacer.containers;

import io.github.rothes.protocolstringreplacer.bukkit.ProtocolStringReplacer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BookMetaContainer extends AbstractContainer<BookMeta> {

    protected Object pages;

    public BookMetaContainer(@NotNull BookMeta bookMeta) {
        super(bookMeta);
    }

    public BookMetaContainer(@NotNull BookMeta bookMeta, @NotNull Container<?> root) {
        super(bookMeta, root);
    }

    @Override
    public void createDefaultChildren() {
        if (content.hasAuthor()) {
            children.add(new SimpleTextContainer(content.getAuthor(), root) {
                @Override
                public String getResult() {
                    String result = super.getResult();
                    BookMetaContainer.this.content.setAuthor(result);
                    return result;
                }
            });
        }
        if (content.hasTitle()) {
            children.add(new SimpleTextContainer(content.getTitle(), root) {
                @Override
                public String getResult() {
                    String result = super.getResult();
                    BookMetaContainer.this.content.setTitle(result);
                    return result;
                }
            });
        }
        if (content.hasPages()) {
            if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 12) {
                List<BaseComponent[]> get = content.spigot().getPages();
                if (get != null) {
                    List<BaseComponent[]> pages = new ArrayList<>(get);
                    for (int i = 0; i < pages.size(); i++) {
                        int finalI = i;
                        children.add(new ChatJsonContainer(ComponentSerializer.toString(pages.get(finalI)), root, true) {
                            @Override
                            public String getResult() {
                                // Avoid deserializing BaseComponents to json.
                                pages.set(finalI, getComponentsContainer().getResult());
                                return "";
                            }
                        });
                    }
                    this.pages = pages;
                }

            } else {
                List<String> get = content.getPages();
                if (get != null) {
                    List<String> pages = new ArrayList<>(get);
                    for (int i = 0; i < pages.size(); i++) {
                        int finalI = i;
                        children.add(new SimpleTextContainer(pages.get(finalI), root) {
                            @Override
                            public String getResult() {
                                String result = super.getResult();
                                pages.set(finalI, result);
                                return result;
                            }
                        });
                    }
                    this.pages = pages;
                }
            }
        }
        super.createDefaultChildren();
    }

    @SuppressWarnings("unchecked")
    @Override
    public BookMeta getResult() {
        BookMeta result = super.getResult();
        if (pages != null) {
            if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 12) {
                content.spigot().setPages((List<BaseComponent[]>) pages);
            } else {
                content.setPages((List<String>) pages);
            }
        }
        return result;
    }

}
