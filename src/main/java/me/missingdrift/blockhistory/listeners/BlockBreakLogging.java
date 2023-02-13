package me.missingdrift.blockhistory.listeners;

import me.missingdrift.blockhistory.Actor;
import me.missingdrift.blockhistory.blockhistory;
import me.missingdrift.blockhistory.Logging;
import me.missingdrift.blockhistory.config.WorldConfig;
import me.missingdrift.blockhistory.util.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import static me.missingdrift.blockhistory.config.Config.getWorldConfig;
import static me.missingdrift.blockhistory.config.Config.isLogging;
import static me.missingdrift.blockhistory.util.LoggingUtil.smartblockhistoryBreak;
import static me.missingdrift.blockhistory.util.LoggingUtil.smartblockhistoryReplace;
import static me.missingdrift.blockhistory.util.LoggingUtil.smartLogFallables;

public class BlockBreakLogging extends LoggingListener {
    public BlockBreakLogging(blockhistory lb) {
        super(lb);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isLogging(event.getBlock().getWorld(), Logging.BLOCKBREAK)) {
            WorldConfig wcfg = getWorldConfig(event.getBlock().getWorld());
            if (wcfg == null) {
                return;
            }

            final Actor actor = Actor.actorFromEntity(event.getPlayer());
            final Block origin = event.getBlock();
            final Material type = origin.getType();

            if (wcfg.isLogging(Logging.CHESTACCESS) && BukkitUtils.getContainerBlocks().contains(type) && !BukkitUtils.getShulkerBoxBlocks().contains(type)) {
                consumer.queueContainerBreak(actor, origin.getState());
            } else if (type == Material.ICE) {
                // When in creative mode ice doesn't form water
                if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                    smartblockhistoryBreak(consumer, actor, origin);
                } else {
                    smartblockhistoryReplace(consumer, actor, origin, Bukkit.createBlockData(Material.WATER));
                }
            } else {
                smartblockhistoryBreak(consumer, actor, origin);
            }
            smartLogFallables(consumer, actor, origin);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (isLogging(event.getBlockClicked().getWorld(), Logging.BLOCKBREAK)) {
            BlockData clickedBlockData = event.getBlockClicked().getBlockData();
            if (clickedBlockData instanceof Waterlogged) {
                Waterlogged clickedWaterlogged = (Waterlogged) clickedBlockData;
                if (clickedWaterlogged.isWaterlogged()) {
                    Waterlogged clickedWaterloggedWithoutWater = (Waterlogged) clickedWaterlogged.clone();
                    clickedWaterloggedWithoutWater.setWaterlogged(false);
                    consumer.queueBlockReplace(Actor.actorFromEntity(event.getPlayer()), event.getBlockClicked().getLocation(), clickedWaterlogged, clickedWaterloggedWithoutWater);
                }
            } else {
                consumer.queueBlockBreak(Actor.actorFromEntity(event.getPlayer()), event.getBlockClicked().getState());
            }
        }
    }
}
