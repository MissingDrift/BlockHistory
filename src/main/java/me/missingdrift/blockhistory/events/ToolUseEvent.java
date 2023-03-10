package me.missingdrift.blockhistory.events;

import me.missingdrift.blockhistory.QueryParams;
import me.missingdrift.blockhistory.Tool;
import me.missingdrift.blockhistory.ToolBehavior;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class ToolUseEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancel;
    private final Tool tool;
    private final ToolBehavior behavior;
    private final QueryParams params;

    public ToolUseEvent(Player who, Tool tool, ToolBehavior behavior, QueryParams params) {
        super(who);
        this.tool = tool;
        this.behavior = behavior;
        this.params = params;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public Tool getTool() {
        return tool;
    }

    public ToolBehavior getBehavior() {
        return behavior;
    }

    public QueryParams getParams() {
        return params;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
