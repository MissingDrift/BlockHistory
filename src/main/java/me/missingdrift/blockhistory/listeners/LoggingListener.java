package me.missingdrift.blockhistory.listeners;

import me.missingdrift.blockhistory.Consumer;
import me.missingdrift.blockhistory.blockhistory;
import org.bukkit.event.Listener;

public class LoggingListener implements Listener {
    protected final Consumer consumer;

    public LoggingListener(blockhistory lb) {
        consumer = lb.getConsumer();
    }
}
