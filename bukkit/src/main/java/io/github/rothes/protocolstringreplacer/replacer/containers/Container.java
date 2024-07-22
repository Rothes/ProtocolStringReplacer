package io.github.rothes.protocolstringreplacer.replacer.containers;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Container<T> {

    List<Replaceable> getJsons();

    List<Replaceable> getTexts();

    void createDefaultChildren();

    void createJsons(@NotNull Container<?> container);

    void createTexts(@NotNull Container<?> container);

    void addJson(@NotNull Replaceable replaceable);

    void addText(@NotNull Replaceable replaceable);

    void reset();

    @NotNull
    Container<?> getRoot();

    @NotNull
    List<Container<?>> getChildren();

    @NotNull
    T getResult();

}
