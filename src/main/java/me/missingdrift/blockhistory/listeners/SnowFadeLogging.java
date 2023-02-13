package me.missingdrift.blockhistory.listeners;

import me.missingdrift.blockhistory.Actor;
import me.missingdrift.blockhistory.blockhistory;
import me.missingdrift.blockhistory.Logging;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFadeEvent;

import static me.missingdrift.blockhistory.config.Config.isLogging;

public class SnowFadeLogging extends LoggingListener {
    public SnowFadeLogging(blockhistory lb) {
        super(lb);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        if (isLogging(event.getBlock().getWorld(), Logging.SNOWFADE)) {
            final Material type = event.getBlock().getType();
            if (type == Material.SNOW || type == Material.ICE) {
                consumer.queueBlockReplace(new Actor("SnowFade"), event.getBlock().getState(), event.getNewState());
            }
        }
    }
}
