package me.rothes.protocolstringreplacer.events;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PsrReloadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final ReloadState reloadState;
    private final CommandSender caller;

    public PsrReloadEvent(ReloadState reloadState, CommandSender caller) {
        this.reloadState = reloadState;
        this.caller = caller;
    }

    public ReloadState getReloadState() {
        return reloadState;
    }

    public CommandSender getCaller() {
        return caller;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public enum ReloadState {
        BEFORE,
        FINISH
    }

}
