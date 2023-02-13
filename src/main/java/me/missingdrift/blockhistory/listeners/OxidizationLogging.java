package me.missingdrift.blockhistory.listeners;

import static me.missingdrift.blockhistory.config.Config.isLogging;

import me.missingdrift.blockhistory.Actor;
import me.missingdrift.blockhistory.blockhistory;
import me.missingdrift.blockhistory.Logging;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFormEvent;

public class OxidizationLogging extends LoggingListener {
    public OxidizationLogging(blockhistory lb) {
        super(lb);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPhysics(BlockFormEvent event) {
        if (isLogging(event.getBlock().getWorld(), Logging.OXIDIZATION)) {
            final Material type = event.getNewState().getType();
            if (type.name().contains("COPPER")) {
                consumer.queueBlockReplace(new Actor("NaturalOxidization"), event.getBlock().getState(), event.getNewState());
            }
        }
    }

}
