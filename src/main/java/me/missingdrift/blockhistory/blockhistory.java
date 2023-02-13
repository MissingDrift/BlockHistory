package me.missingdrift.blockhistory;

import me.missingdrift.blockhistory.addons.worldguard.WorldGuardLoggingFlagsAddon;
import me.missingdrift.blockhistory.config.Config;
import me.missingdrift.blockhistory.listeners.*;
import me.missingdrift.blockhistory.questioner.Questioner;
import me.missingdrift.blockhistory.util.BukkitUtils;
import me.missingdrift.blockhistory.util.MySQLConnectionPool;
import me.missingdrift.blockhistory.worldedit.WorldEditHelper;
import me.missingdrift.blockhistory.worldedit.WorldEditLoggingHook;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static me.missingdrift.blockhistory.config.Config.*;
import static org.bukkit.Bukkit.getPluginManager;

public class blockhistory extends JavaPlugin {
    private static blockhistory blockhistory = null;
    private MySQLConnectionPool pool;
    private Consumer consumer = null;
    private CommandsHandler commandsHandler;
    private boolean noDb = false, connected = true;
    private PlayerInfoLogging playerInfoLogging;
    private ScaffoldingLogging scaffoldingLogging;
    private Questioner questioner;
    private WorldGuardLoggingFlagsAddon worldGuardLoggingFlagsAddon;
    private boolean isConfigLoaded;
    private volatile boolean isCompletelyEnabled;

    public static blockhistory getInstance() {
        return blockhistory;
    }

    public boolean isCompletelyEnabled() {
        return isCompletelyEnabled;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public CommandsHandler getCommandsHandler() {
        return commandsHandler;
    }

    @Override
    public void onLoad() {
        blockhistory = this;
        BukkitUtils.isDoublePlant(Material.AIR); // Force static code to run
        try {
            Config.load(this);
            isConfigLoaded = true;
        } catch (final Exception ex) {
            getLogger().log(Level.SEVERE, "Could not load blockhistory config! " + ex.getMessage(), ex);
        }
        if (Config.worldGuardLoggingFlags) {
            if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
                getLogger().log(Level.SEVERE, "Invalid config! addons.worldguardLoggingFlags is set to true, but WorldGuard is not loaded.");
            } else {
                worldGuardLoggingFlagsAddon = new WorldGuardLoggingFlagsAddon(this);
                worldGuardLoggingFlagsAddon.onPluginLoad();
            }
        }
    }

    @Override
    public void onEnable() {
        final PluginManager pm = getPluginManager();
        if (!isConfigLoaded) {
            pm.disablePlugin(this);
            return;
        }
        consumer = new Consumer(this);
        try {
            getLogger().info("Connecting to " + user + "@" + url + "...");
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException ignored) {
                Class.forName("com.mysql.jdbc.Driver");
            }
            pool = new MySQLConnectionPool(url, user, password, mysqlUseSSL, mysqlRequireSSL);
            final Connection conn = getConnection(true);
            if (conn == null) {
                noDb = true;
                return;
            }
            final Statement st = conn.createStatement();
            final ResultSet rs = st.executeQuery("SHOW CHARACTER SET where charset='utf8mb4';");
            if (rs.next()) {
                Config.mb4 = true;
                // Allegedly JDBC driver since 2010 hasn't needed this. I did.
                st.executeUpdate("SET NAMES utf8mb4;");
            }
            conn.close();
            Updater updater = new Updater(this);
            updater.checkTables();
            MaterialConverter.initializeMaterials(getConnection());
            MaterialConverter.getOrAddMaterialId(Material.AIR); // AIR must be the first entry
            EntityTypeConverter.initializeEntityTypes(getConnection());
            if (updater.update()) {
                load(this);
            }
        } catch (final NullPointerException ex) {
            getLogger().log(Level.SEVERE, "Error while loading: ", ex);
        } catch (final Exception ex) {
            getLogger().log(Level.SEVERE, "Error while loading: " + ex.getMessage(), ex);
            pm.disablePlugin(this);
            return;
        }

        if (WorldEditHelper.hasWorldEdit()) {
            new WorldEditLoggingHook(this).hook();
        }
        commandsHandler = new CommandsHandler(this);
        getCommand("lb").setExecutor(commandsHandler);
        if (enableAutoClearLog && autoClearLogDelay > 0) {
            getServer().getScheduler().runTaskTimerAsynchronously(this, new AutoClearLog(this), 6000, autoClearLogDelay * 60 * 20);
        }
        new DumpedLogImporter(this).run();
        registerEvents();
        consumer.start();
        for (final Tool tool : toolsByType.values()) {
            if (pm.getPermission("blockhistory.tools." + tool.name) == null) {
                final Permission perm = new Permission("blockhistory.tools." + tool.name, tool.permissionDefault);
                pm.addPermission(perm);
            }
        }
        questioner = new Questioner(this);
        if (worldGuardLoggingFlagsAddon != null) {
            worldGuardLoggingFlagsAddon.onPluginEnable();
        }
        isCompletelyEnabled = true;
        getServer().getScheduler().runTaskAsynchronously(this, new Updater.PlayerCountChecker(this));
    }

    private void registerEvents() {
        final PluginManager pm = getPluginManager();
        pm.registerEvents(new ToolListener(this), this);
        pm.registerEvents(playerInfoLogging = new PlayerInfoLogging(this), this);
        if (askRollbackAfterBan) {
            pm.registerEvents(new BanListener(this), this);
        }
        if (isLogging(Logging.BLOCKPLACE)) {
            pm.registerEvents(new BlockPlaceLogging(this), this);
        }
        if (isLogging(Logging.LAVAFLOW) || isLogging(Logging.WATERFLOW)) {
            pm.registerEvents(new FluidFlowLogging(this), this);
        }
        if (isLogging(Logging.BLOCKBREAK)) {
            pm.registerEvents(new BlockBreakLogging(this), this);
        }
        if (isLogging(Logging.SIGNTEXT)) {
            pm.registerEvents(new SignChangeLogging(this), this);
        }
        if (isLogging(Logging.FIRE)) {
            pm.registerEvents(new BlockBurnLogging(this), this);
        }
        if (isLogging(Logging.SNOWFORM)) {
            pm.registerEvents(new SnowFormLogging(this), this);
        }
        if (isLogging(Logging.SNOWFADE)) {
            pm.registerEvents(new SnowFadeLogging(this), this);
        }
        if (isLogging(Logging.SCAFFOLDING)) {
            pm.registerEvents(scaffoldingLogging = new ScaffoldingLogging(this), this);
        }
        if (isLogging(Logging.CAULDRONINTERACT)) {
            pm.registerEvents(new CauldronLogging(this), this);
        }
        if (isLogging(Logging.CREEPEREXPLOSION) || isLogging(Logging.TNTEXPLOSION) || isLogging(Logging.GHASTFIREBALLEXPLOSION) || isLogging(Logging.ENDERDRAGON) || isLogging(Logging.MISCEXPLOSION)) {
            pm.registerEvents(new ExplosionLogging(this), this);
        }
        if (isLogging(Logging.LEAVESDECAY)) {
            pm.registerEvents(new LeavesDecayLogging(this), this);
        }
        if (isLogging(Logging.CHESTACCESS)) {
            pm.registerEvents(new ChestAccessLogging(this), this);
        }
        if (isLogging(Logging.BLOCKBREAK) || isLogging(Logging.BLOCKPLACE) || isLogging(Logging.SWITCHINTERACT) || isLogging(Logging.DOORINTERACT) || isLogging(Logging.CAKEEAT) || isLogging(Logging.DIODEINTERACT) || isLogging(Logging.COMPARATORINTERACT) || isLogging(Logging.NOTEBLOCKINTERACT)
                || isLogging(Logging.PRESUREPLATEINTERACT) || isLogging(Logging.TRIPWIREINTERACT) || isLogging(Logging.CROPTRAMPLE)) {
            pm.registerEvents(new InteractLogging(this), this);
        }
        if (isLogging(Logging.CREATURECROPTRAMPLE)) {
            pm.registerEvents(new CreatureInteractLogging(this), this);
        }
        if (isLogging(Logging.KILL)) {
            pm.registerEvents(new KillLogging(this), this);
        }
        if (isLogging(Logging.CHAT) || isLogging(Logging.PLAYER_COMMANDS) || isLogging(Logging.CONSOLE_COMMANDS) || isLogging(Logging.COMMANDBLOCK_COMMANDS)) {
            pm.registerEvents(new ChatLogging(this), this);
        }
        if (isLogging(Logging.ENDERMEN)) {
            pm.registerEvents(new EndermenLogging(this), this);
        }
        if (isLogging(Logging.WITHER)) {
            pm.registerEvents(new WitherLogging(this), this);
        }
        if (isLogging(Logging.NATURALSTRUCTUREGROW)) {
            pm.registerEvents(new StructureGrowLogging(this), this);
        }
        if (isLogging(Logging.BONEMEALSTRUCTUREGROW)) {
            pm.registerEvents(new BlockFertilizeLogging(this), this);
        }
        if (isLogging(Logging.GRASSGROWTH) || isLogging(Logging.MYCELIUMSPREAD) || isLogging(Logging.VINEGROWTH) || isLogging(Logging.MUSHROOMSPREAD) || isLogging(Logging.BAMBOOGROWTH) || isLogging(Logging.DRIPSTONEGROWTH) || isLogging(Logging.SCULKSPREAD)) {
            pm.registerEvents(new BlockSpreadLogging(this), this);
        }
        if (isLogging(Logging.DRAGONEGGTELEPORT)) {
            pm.registerEvents(new DragonEggLogging(this), this);
        }
        if (isLogging(Logging.LECTERNBOOKCHANGE)) {
            pm.registerEvents(new LecternLogging(this), this);
        }
        if (isLogging(Logging.OXIDIZATION)) {
            pm.registerEvents(new OxidizationLogging(this), this);
        }
        if (Config.isLoggingAnyEntities()) {
            if (!WorldEditHelper.hasFullWorldEdit()) {
                getLogger().severe("No compatible WorldEdit found, entity logging will not work!");
            } else {
                pm.registerEvents(new AdvancedEntityLogging(this), this);
                getLogger().info("Entity logging enabled!");
            }
        }
    }

    @Override
    public void onDisable() {
        isCompletelyEnabled = false;
        getServer().getScheduler().cancelTasks(this);
        if (consumer != null) {
            if (logPlayerInfo && playerInfoLogging != null) {
                for (final Player player : getServer().getOnlinePlayers()) {
                    playerInfoLogging.onPlayerQuit(player);
                }
            }
            getLogger().info("Waiting for consumer ...");
            consumer.shutdown();
            if (consumer.getQueueSize() > 0) {
                getLogger().info("Remaining queue size: " + consumer.getQueueSize() + ". Trying to write to a local file.");
                try {
                    consumer.writeToFile();
                    getLogger().info("Successfully dumped queue.");
                } catch (final FileNotFoundException ex) {
                    getLogger().info("Failed to write. Given up.");
                }
            }
        }
        if (pool != null) {
            pool.close();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (noDb) {
            sender.sendMessage(ChatColor.RED + "No database connected. Check your MySQL user/pw and database for typos. Start/restart your MySQL server.");
        }
        return true;
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission);
    }

    public Connection getConnection() {
        return getConnection(false);
    }

    public Connection getConnection(boolean testConnection) {
        try {
            final Connection conn = pool.getConnection();
            if (!connected) {
                getLogger().info("MySQL connection rebuild");
                connected = true;
            }
            return conn;
        } catch (final Exception ex) {
            if (testConnection) {
                getLogger().log(Level.SEVERE, "Could not connect to the Database! Please check your config! " + ex.getMessage());
            } else if (connected) {
                getLogger().log(Level.SEVERE, "Error while fetching connection: ", ex);
                connected = false;
            } else {
                getLogger().log(Level.SEVERE, "MySQL connection lost", ex);
            }
            return null;
        }
    }


    public List<BlockChange> getBlockChanges(QueryParams params) throws SQLException {
        final Connection conn = getConnection();
        Statement state = null;
        if (conn == null) {
            throw new SQLException("No connection");
        }
        try {
            state = conn.createStatement();
            final ResultSet rs = state.executeQuery(params.getQuery());
            final List<BlockChange> blockchanges = new ArrayList<>();
            while (rs.next()) {
                blockchanges.add(new BlockChange(rs, params));
            }
            return blockchanges;
        } finally {
            if (state != null) {
                state.close();
            }
            conn.close();
        }
    }

    public int getCount(QueryParams params) throws SQLException {
        if (params == null || params.world == null || !Config.isLogged(params.world)) {
            throw new IllegalArgumentException("World is not logged: " + ((params == null || params.world == null) ? "null" : params.world.getName()));
        }
        final Connection conn = getConnection();
        Statement state = null;
        if (conn == null) {
            throw new SQLException("No connection");
        }
        try {
            state = conn.createStatement();
            final QueryParams p = params.clone();
            p.needCount = true;
            final ResultSet rs = state.executeQuery(p.getQuery());
            if (!rs.next()) {
                return 0;
            }
            return rs.getInt(1);
        } finally {
            if (state != null) {
                state.close();
            }
            conn.close();
        }
    }

    @Override
    public File getFile() {
        return super.getFile();
    }

    public Questioner getQuestioner() {
        return questioner;
    }

    public ScaffoldingLogging getScaffoldingLogging() {
        return scaffoldingLogging;
    }
}
