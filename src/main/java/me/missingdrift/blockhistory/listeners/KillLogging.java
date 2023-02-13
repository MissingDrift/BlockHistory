package me.missingdrift.blockhistory.listeners;

import me.missingdrift.blockhistory.Actor;
import me.missingdrift.blockhistory.blockhistory;
import me.missingdrift.blockhistory.Logging;
import me.missingdrift.blockhistory.config.Config.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import static me.missingdrift.blockhistory.config.Config.*;

public class KillLogging extends LoggingListener {

    public KillLogging(blockhistory lb) {
        super(lb);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent deathEvent) {
        EntityDamageEvent event = deathEvent.getEntity().getLastDamageCause();

        if (event != null && event.isCancelled() == false && isLogging(event.getEntity().getWorld(), Logging.KILL) && event.getEntity() instanceof LivingEntity) {
            final LivingEntity victim = (LivingEntity) event.getEntity();
            if (event instanceof EntityDamageByEntityEvent) {
                final Entity killer = ((EntityDamageByEntityEvent) event).getDamager();
                if (logKillsLevel == LogKillsLevel.PLAYERS && !(victim instanceof Player && killer instanceof Player)) {
                    return;
                } else if (logKillsLevel == LogKillsLevel.MONSTERS && !((victim instanceof Player || victim instanceof Monster) && killer instanceof Player || killer instanceof Monster)) {
                    return;
                }
                consumer.queueKill(killer, victim);
            } else if (deathEvent.getEntity().getKiller() != null) {
                consumer.queueKill(deathEvent.getEntity().getKiller(), victim);
            } else if (logEnvironmentalKills) {
                if (logKillsLevel == LogKillsLevel.PLAYERS && !(victim instanceof Player)) {
                    return;
                } else if (logKillsLevel == LogKillsLevel.MONSTERS && !((victim instanceof Player || victim instanceof Monster))) {
                    return;
                }
                consumer.queueKill(new Actor(event.getCause().toString()), victim);
            }
        }
    }
}
