package me.rothes.protocolstringreplacer.events;

import me.rothes.protocolstringreplacer.api.user.PsrUser;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PsrReloadEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final ReloadState reloadState;
    private final PsrUser user;
    private boolean cancelled = false;

    public PsrReloadEvent(ReloadState reloadState, PsrUser user) {
        this.reloadState = reloadState;
        this.user = user;
    }

    public ReloadState getReloadState() {
        return reloadState;
    }

    public PsrUser getUser() {
        return user;
    }

    public CommandSender getActor() {
        return user.getSender();
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum ReloadState {
        BEFORE,
        FINISH
    }

}
