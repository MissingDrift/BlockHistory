package me.missingdrift.blockhistory.listeners;

import me.missingdrift.blockhistory.Actor;
import me.missingdrift.blockhistory.blockhistory;
import me.missingdrift.blockhistory.Logging;
import me.missingdrift.blockhistory.config.WorldConfig;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFertilizeEvent;
import static me.missingdrift.blockhistory.config.Config.getWorldConfig;

public class BlockFertilizeLogging extends LoggingListener {
    public BlockFertilizeLogging(blockhistory lb) {
        super(lb);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        final WorldConfig wcfg = getWorldConfig(event.getBlock().getLocation().getWorld());
        if (wcfg != null) {
            if (!wcfg.isLogging(Logging.BONEMEALSTRUCTUREGROW)) {
                return;
            }
            final Actor actor;
            if (event.getPlayer() != null) {
                actor = Actor.actorFromEntity(event.getPlayer());
            } else {
                actor = new Actor("Dispenser");
            }
            for (final BlockState state : event.getBlocks()) {
                consumer.queueBlockReplace(actor, state.getBlock().getState(), state);
            }
        }
    }
}
