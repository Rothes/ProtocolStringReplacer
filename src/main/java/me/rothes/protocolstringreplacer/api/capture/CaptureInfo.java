package me.rothes.protocolstringreplacer.api.capture;

import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.containers.Replaceable;
import me.rothes.protocolstringreplacer.utils.SpigotUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public interface CaptureInfo {

    /**
     * Get the Json replaceables in container.
     *
     * @return Json replacesable list.
     */
    @NotNull List<String> getJsons();

    /**
     * Get the text replaceables in container.
     *
     * @return Text replacesable list.
     */
    @NotNull List<String> getTexts();

    /**
     * Get the direct string in container.
     *
     * @return Direct string list.
     */
    @NotNull List<String> getDirects();

    /**
     * Get the Listen-Type of this capture.
     *
     * @return Listen-Type.
     */
    @NotNull ListenType getListenType();

    /**
     * Get the capture time of this capture.
     *
     * @return Time.
     */
    long getTime();

    /**
     * Get the PsrUser that capture the packet.
     *
     * @return PsrUser that capture the packet.
     */
    @NotNull PsrUser getUser();

    /**
     * Get the description added by container.
     *
     * @return Description.
     */
    @Nullable
    String getDescription();

    /**
     * Get the extra info added by container.
     *
     * @return extra info.
     */
    @Nullable
    BaseComponent[] getExtra();

    /**
     * Set the Json replaceables.
     *
     * @param replaceables The Json replaceables to set.
     * @throws IllegalStateException if Json replaceables are already been set.
     */
    void setJsons(@NotNull List<Replaceable> replaceables);

    /**
     * Set the text replaceables.
     *
     * @param replaceables The text replaceables to set.
     * @throws IllegalStateException if text replaceables are already been set.
     */
    void setTexts(@NotNull List<Replaceable> replaceables);

    /**
     * Set the direct strings.
     *
     * @param strings The direct strings to set.
     * @throws IllegalStateException if direct strings are already been set.
     */
    void setDirects(@NotNull List<String> strings);

    /**
     * Set the Listen-Type of this capture.
     *
     * @param listenType The Listen-Type to set.
     * @throws IllegalStateException if Listen-Type is already been set.
     */
    void setListenType(@NotNull ListenType listenType);

    /**
     * Set the capture time of this capture.
     *
     * @param time The time to set.
     * @throws IllegalStateException if capture time is already been set.
     */
    void setTime(long time);

    /**
     * Set the user that capture the packet.
     *
     * @param user The PsrUser to set
     * @throws IllegalStateException if PsrUser is already been set.
     */
    void setUser(@NotNull PsrUser user);

    /**
     * Set the description.
     *
     * @param description The description to set.
     * @throws IllegalStateException if description is already been set.
     */
    void setDescription(@NotNull String description);

    /**
     * Set the extra info.
     *
     * @param extra The extra info to set.
     */
    void setExtra(@NotNull BaseComponent[] extra);

    /**
     * Get the capture count.
     */
    int getCount();

    /**
     * Set the capture count.
     *
     * @param count The count to set.
     */
    void setCount(int count);

    default boolean isSimilar(CaptureInfo captureInfo) {
        return Objects.equals(captureInfo.getDescription(), getDescription())
                && Objects.equals(captureInfo.getJsons(), getJsons())
                && Objects.equals(captureInfo.getTexts(), getTexts())
                && SpigotUtils.compareComponents(captureInfo.getExtra(), getExtra());
    }

}
