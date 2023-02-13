package me.missingdrift.blockhistory;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static me.missingdrift.blockhistory.config.Config.toolsByType;
import static org.bukkit.Bukkit.getServer;

public class Session {
    private static final Map<String, Session> sessions = new HashMap<>();
    public QueryParams lastQuery = null;
    public LookupCacheElement[] lookupCache = null;
    public int page = 1;
    public Map<Tool, ToolData> toolData;

    private Session(Player player) {
        toolData = new HashMap<>();
        final blockhistory blockhistory = blockhistory.getInstance();
        if (player != null) {
            for (final Tool tool : toolsByType.values()) {
                toolData.put(tool, new ToolData(tool, blockhistory, player));
            }
        }
    }

    public static boolean hasSession(CommandSender sender) {
        return sessions.containsKey(sender.getName().toLowerCase());
    }

    public static boolean hasSession(String playerName) {
        return sessions.containsKey(playerName.toLowerCase());
    }

    public static Session getSession(CommandSender sender) {
        return getSession(sender.getName());
    }

    public static Session getSession(String playerName) {
        Session session = sessions.get(playerName.toLowerCase());
        if (session == null) {
            session = new Session(getServer().getPlayer(playerName));
            sessions.put(playerName.toLowerCase(), session);
        }
        return session;
    }
}
