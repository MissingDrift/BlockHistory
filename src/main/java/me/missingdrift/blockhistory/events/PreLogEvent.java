package me.missingdrift.blockhistory.events;

import me.missingdrift.blockhistory.Actor;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class PreLogEvent extends Event implements Cancellable {

    protected boolean cancelled = false;
    protected Actor owner;

    public PreLogEvent(Actor owner) {
        this.owner = owner;
    }

    @Deprecated
    public String getOwner() {
        return owner.getName();
    }

    public Actor getOwnerActor() {
        return owner;
    }

    public void setOwner(Actor owner) {
        this.owner = owner;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
