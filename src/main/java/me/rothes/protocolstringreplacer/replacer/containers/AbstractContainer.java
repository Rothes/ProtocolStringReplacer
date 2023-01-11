package me.rothes.protocolstringreplacer.replacer.containers;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContainer<T> implements Container<T> {

    protected T content;
    protected Container<?> root;
    protected List<Replaceable> jsonReplaceables = null;
    protected List<Replaceable> textReplaceables = null;
    protected List<Container<?>> children = new ArrayList<>();

    public AbstractContainer(@NotNull T t) {
        Validate.notNull(t, "Content cannot be null");
        this.content = t;
        this.root = this;
    }

    public AbstractContainer(@NotNull T t, @NotNull Container<?> root) {
        Validate.notNull(t, "Content cannot be null");
        Validate.notNull(root, "Root Container cannot be null");
        this.content = t;
        this.root = root;
    }

    @Override
    public List<Replaceable> getJsons() {
        if (jsonReplaceables == null) {
            throw new IllegalStateException("JsonReplaceables has not been created");
        }
        return jsonReplaceables;
    }

    @Override
    public List<Replaceable> getTexts() {
        if (textReplaceables == null) {
            throw new IllegalStateException("TextReplaceables has not been created");
        }
        return textReplaceables;
    }

    @Override
    public void createDefaultChildren() {
        if (!children.isEmpty()) {
            for (Container<?> child : children) {
                child.createDefaultChildren();
            }
        }
    }

    @Override
    public void createJsons(@NotNull Container<?> container) {
        Validate.notNull(container, "Container cannot be null");
        if (container == this) {
            jsonReplaceables = new ArrayList<>();
        }
        if (!children.isEmpty()) {
            for (Container<?> child : children) {
                child.createJsons(container);
            }
        }
    }

    @Override
    public void createTexts(@NotNull Container<?> container) {
        Validate.notNull(container, "Container cannot be null");
        if (container == this) {
            textReplaceables = new ArrayList<>();
        }
        if (!children.isEmpty()) {
            for (Container<?> child : children) {
                child.createTexts(container);
            }
        }
    }

    @Override
    public void addJson(@NotNull Replaceable replaceable) {
        Validate.notNull(replaceable, "JsonReplaceable cannot be null");
        if (this == root) {
            jsonReplaceables.add(replaceable);
        } else {
            root.addJson(replaceable);
        }
    }

    @Override
    public void addText(@NotNull Replaceable replaceable) {
        Validate.notNull(replaceable, "Replaceable cannot be null");
        if (this != root) {
            root.addText(replaceable);
        } else {
            textReplaceables.add(replaceable);
        }
    }

    @Override
    public void reset() {
        children = new ArrayList<>();
        jsonReplaceables = null;
        textReplaceables = null;
    }

    @Override
    public Container<?> getRoot() {
        return root;
    }

    @Override
    public List<Container<?>> getChildren() {
        return children;
    }

    @Override
    public T getResult() {
        if (!children.isEmpty()) {
            for (Container<?> child : children) {
                child.getResult();
            }
        }
        return content;
    }

}
