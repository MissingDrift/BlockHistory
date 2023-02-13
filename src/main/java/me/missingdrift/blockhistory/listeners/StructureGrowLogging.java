package me.missingdrift.blockhistory.listeners;

import me.missingdrift.blockhistory.Actor;
import me.missingdrift.blockhistory.blockhistory;
import me.missingdrift.blockhistory.Logging;
import me.missingdrift.blockhistory.config.WorldConfig;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.StructureGrowEvent;

import static me.missingdrift.blockhistory.config.Config.getWorldConfig;

public class StructureGrowLogging extends LoggingListener {
    public StructureGrowLogging(blockhistory lb) {
        super(lb);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        final WorldConfig wcfg = getWorldConfig(event.getWorld());
        if (wcfg != null) {
            if (!wcfg.isLogging(Logging.NATURALSTRUCTUREGROW)) {
                return;
            }
            if (!event.isFromBonemeal()) {
                final Actor actor = new Actor("NaturalGrow");
                for (final BlockState state : event.getBlocks()) {
                    consumer.queueBlockReplace(actor, state.getBlock().getState(), state);
                }
            }
        }
    }
}
