package me.rothes.protocolstringreplacer.api.capture;

import me.rothes.protocolstringreplacer.api.user.User;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.containers.Replaceable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

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
    @NotNull Long getTime();

    /**
     * Get the User that capture the packet.
     *
     * @return User that capture the packet.
     */
    @NotNull User getUser();

    /**
     * Get the description added by container.
     *
     * @return Description.
     */
    @Nullable
    String getDescription();

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
    void setTime(@NotNull Long time);

    /**
     * Set the user that capture the packet.
     *
     * @param user The User to set
     * @throws IllegalStateException if User is already been set.
     */
    void setUser(@NotNull User user);

    /**
     * Set the description.
     *
     * @param description The description to set.
     * @throws IllegalStateException if description is already been set.
     */
    void setDescription(@NotNull String description);

}
