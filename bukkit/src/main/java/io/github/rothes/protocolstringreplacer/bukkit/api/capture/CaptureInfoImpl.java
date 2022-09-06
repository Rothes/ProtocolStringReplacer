package io.github.rothes.protocolstringreplacer.bukkit.api.capture;

import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.containers.Replaceable;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.ListenType;
import io.github.rothes.protocolstringreplacer.bukkit.utils.ColorUtils;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class CaptureInfoImpl implements CaptureInfo {

    private List<String> jsons;
    private List<String> texts;
    private ListenType listenType;
    private Long time;
    private PsrUser user;
    private String description;

    /**
     * Get the Json replaceables in container.
     *
     * @return Json replacesable list.
     */
    @Override
    public @NotNull List<String> getJsons() {
        return jsons;
    }

    /**
     * Get the text replaceables in container.
     *
     * @return Text replacesable list.
     */
    @Override
    public @NotNull List<String> getTexts() {
        return texts;
    }

    /**
     * Get the Listen-Type of this capture.
     *
     * @return Listen-Type.
     */
    @Override
    public @NotNull ListenType getListenType() {
        return listenType;
    }

    /**
     * Get the capture time of this capture.
     *
     * @return Time.
     */
    @Override
    public @NotNull Long getTime() {
        return time;
    }

    /**
     * Get the PsrUser that capture the packet.
     *
     * @return PsrUser that capture the packet.
     */
    @Override
    public @NotNull PsrUser getUser() {
        return user;
    }

    /**
     * Get the description added by container.
     *
     * @return Description.
     */
    @Nullable
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Set the Json replaceables.
     *
     * @param replaceables The Json replaceables to set.
     * @throws IllegalStateException if Json replaceables are already been set.
     */
    @Override
    public void setJsons(@NotNull List<Replaceable> replaceables) {
        Validate.notNull(replaceables, "Json replaceables cannot be null");
        if (this.jsons != null) {
            throw new IllegalStateException("Json replaceables have already been set");
        }
        List<String> jsons = new ArrayList<>();
        for (Replaceable replaceable : replaceables) {
            jsons.add(ColorUtils.showColorCodes(replaceable.getText()));
        }
        this.jsons = jsons;
    }

    /**
     * Set the text replaceables.
     *
     * @param replaceables The text replaceables to set.
     * @throws IllegalStateException if text replaceables are already been set.
     */
    @Override
    public void setTexts(@NotNull List<Replaceable> replaceables) {
        Validate.notNull(replaceables, "Text replaceables cannot be null");
        if (this.texts != null) {
            throw new IllegalStateException("Text replaceables have already been set");
        }
        List<String> texts = new ArrayList<>();
        for (Replaceable replaceable : replaceables) {
            texts.add(ColorUtils.showColorCodes(replaceable.getText()));
        }
        this.texts = texts;
    }

    /**
     * Set the Listen-Type of this capture.
     *
     * @param listenType The Listen-Type to set.
     * @throws IllegalStateException if Listen-Type is already been set.
     */
    @Override
    public void setListenType(@NotNull ListenType listenType) {
        Validate.notNull(listenType, "ListenType cannot be null");
        if (this.listenType != null) {
            throw new IllegalStateException("ListenType has already been set");
        }
        this.listenType = listenType;
    }

    /**
     * Set the capture time of this capture.
     *
     * @param time The time to set.
     * @throws IllegalStateException if capture time is already been set.
     */
    @Override
    public void setTime(@NotNull Long time) {
        Validate.notNull(time, "Time cannot be null");
        if (this.listenType != null) {
            throw new IllegalStateException("Capture time has already been set");
        }
        this.time = time;
    }

    /**
     * Set the PsrUser that capture the packet.
     *
     * @param user The PsrUser to set.
     * @throws IllegalStateException if PsrUser is already been set.
     */
    @Override
    public void setUser(@NotNull PsrUser user) {
        Validate.notNull(user, "PsrUser cannot be null");
        if (this.user != null) {
            throw new IllegalStateException("PsrUser has already been set");
        }
        this.user = user;
    }

    /**
     * Set the description.
     *
     * @param description The description to set.
     * @throws IllegalStateException if description is already been set.
     */
    @Override
    public void setDescription(@NotNull String description) {
        Validate.notNull(description, "Description String cannot be null");
        if (this.description != null) {
            throw new IllegalStateException("Description has already been set");
        }
        this.description = description;
    }

}
