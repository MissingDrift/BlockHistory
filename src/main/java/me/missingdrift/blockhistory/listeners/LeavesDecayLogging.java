package me.missingdrift.blockhistory.listeners;

import me.missingdrift.blockhistory.Actor;
import me.missingdrift.blockhistory.blockhistory;
import me.missingdrift.blockhistory.Logging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.LeavesDecayEvent;

import static me.missingdrift.blockhistory.config.Config.isLogging;
import static me.missingdrift.blockhistory.util.LoggingUtil.smartblockhistoryBreak;
import static me.missingdrift.blockhistory.util.LoggingUtil.smartLogFallables;

public class LeavesDecayLogging extends LoggingListener {
    public LeavesDecayLogging(blockhistory lb) {
        super(lb);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (isLogging(event.getBlock().getWorld(), Logging.LEAVESDECAY)) {
            smartblockhistoryBreak(consumer, new Actor("LeavesDecay"), event.getBlock());
            smartLogFallables(consumer, new Actor("LeavesDecay"), event.getBlock());
        }
    }
}
