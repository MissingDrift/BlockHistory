package me.missingdrift.blockhistory;

import org.bukkit.entity.Player;

public class ToolData {
    public boolean enabled;
    public QueryParams params;
    public ToolMode mode;

    public ToolData(Tool tool, blockhistory blockhistory, Player player) {
        enabled = tool.defaultEnabled && blockhistory.hasPermission(player, "blockhistory.tools." + tool.name);
        params = tool.params.clone();
        mode = tool.mode;
    }
}
