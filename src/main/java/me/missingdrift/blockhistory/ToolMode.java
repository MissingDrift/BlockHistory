package me.missingdrift.blockhistory;

public enum ToolMode {
    CLEARLOG("blockhistory.clearlog"),
    LOOKUP("blockhistory.lookup"),
    REDO("blockhistory.rollback"),
    ROLLBACK("blockhistory.rollback"),
    WRITELOGFILE("blockhistory.rollback");
    private final String permission;

    private ToolMode(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
