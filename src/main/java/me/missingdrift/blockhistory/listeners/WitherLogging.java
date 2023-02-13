package me.missingdrift.blockhistory.listeners;

import me.missingdrift.blockhistory.Actor;
import me.missingdrift.blockhistory.blockhistory;
import me.missingdrift.blockhistory.Logging;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import static me.missingdrift.blockhistory.config.Config.isLogging;

public class WitherLogging extends LoggingListener {
    public WitherLogging(blockhistory lb) {
        super(lb);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Wither && isLogging(event.getBlock().getWorld(), Logging.WITHER)) {
            consumer.queueBlockReplace(Actor.actorFromEntity(event.getEntity()), event.getBlock().getState(), event.getBlockData()); // Wither walked through a block.
        }
    }
}
