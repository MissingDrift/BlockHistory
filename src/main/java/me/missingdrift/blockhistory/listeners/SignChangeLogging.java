package me.missingdrift.blockhistory.listeners;

import me.missingdrift.blockhistory.Actor;
import me.missingdrift.blockhistory.blockhistory;
import me.missingdrift.blockhistory.Logging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

import static me.missingdrift.blockhistory.config.Config.isLogging;

public class SignChangeLogging extends LoggingListener {
    public SignChangeLogging(blockhistory lb) {
        super(lb);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (isLogging(event.getBlock().getWorld(), Logging.SIGNTEXT)) {
            consumer.queueSignChange(Actor.actorFromEntity(event.getPlayer()), event.getBlock().getLocation(), event.getBlock().getBlockData(), event.getLines());
        }
    }
}
