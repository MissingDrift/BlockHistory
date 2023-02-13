package me.missingdrift.blockhistory;

import java.util.Arrays;
import java.util.logging.Level;

import static me.missingdrift.blockhistory.config.Config.autoClearLog;
import static org.bukkit.Bukkit.*;

public class AutoClearLog implements Runnable {
    private final blockhistory blockhistory;

    AutoClearLog(blockhistory blockhistory) {
        this.blockhistory = blockhistory;
    }

    @Override
    public void run() {
        final CommandsHandler handler = blockhistory.getCommandsHandler();
        for (final String paramStr : autoClearLog) {
            if (!blockhistory.isCompletelyEnabled()) {
                return;
            }
            try {
                final QueryParams params = new QueryParams(blockhistory, getConsoleSender(), Arrays.asList(paramStr.split(" ")));
                params.noForcedLimit = true;
                handler.new CommandClearLog(getServer().getConsoleSender(), params, false);
            } catch (final Exception ex) {
                getLogger().log(Level.SEVERE, "Failed to schedule auto ClearLog: ", ex);
            }
        }
    }
}
