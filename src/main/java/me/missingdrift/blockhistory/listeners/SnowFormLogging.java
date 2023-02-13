package me.missingdrift.blockhistory.listeners;

import me.missingdrift.blockhistory.Actor;
import me.missingdrift.blockhistory.blockhistory;
import me.missingdrift.blockhistory.Logging;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFormEvent;

import static me.missingdrift.blockhistory.config.Config.isLogging;

public class SnowFormLogging extends LoggingListener {
    public SnowFormLogging(blockhistory lb) {
        super(lb);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        if (isLogging(event.getBlock().getWorld(), Logging.SNOWFORM)) {
            final Material type = event.getNewState().getType();
            if (type == Material.SNOW || type == Material.ICE) {
                consumer.queueBlockReplace(new Actor("SnowForm"), event.getBlock().getState(), event.getNewState());
            }
        }
    }
}
