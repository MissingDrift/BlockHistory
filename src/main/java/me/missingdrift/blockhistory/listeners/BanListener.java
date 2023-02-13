package me.missingdrift.blockhistory.listeners;

import me.missingdrift.blockhistory.CommandsHandler;
import me.missingdrift.blockhistory.blockhistory;
import me.missingdrift.blockhistory.QueryParams;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import static me.missingdrift.blockhistory.config.Config.banPermission;
import static me.missingdrift.blockhistory.config.Config.isLogged;
import static org.bukkit.Bukkit.getScheduler;

public class BanListener implements Listener {
    private final CommandsHandler handler;
    private final blockhistory blockhistory;

    public BanListener(blockhistory blockhistory) {
        this.blockhistory = blockhistory;
        handler = blockhistory.getCommandsHandler();
    }

    @EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        final String[] split = event.getMessage().split(" ");
        if (split.length > 1 && split[0].equalsIgnoreCase("/ban") && blockhistory.hasPermission(event.getPlayer(), banPermission)) {
            final QueryParams p = new QueryParams(blockhistory);
            p.setPlayer(split[1].equalsIgnoreCase("g") ? split[2] : split[1]);
            p.since = 0;
            p.silent = false;
            getScheduler().runTaskAsynchronously(blockhistory, new Runnable() {
                @Override
                public void run() {
                    for (final World world : blockhistory.getServer().getWorlds()) {
                        if (isLogged(world)) {
                            p.world = world;
                            try {
                                handler.new CommandRollback(event.getPlayer(), p, false);
                            } catch (final Exception ex) {
                            }
                        }
                    }
                }
            });
        }
    }
}
