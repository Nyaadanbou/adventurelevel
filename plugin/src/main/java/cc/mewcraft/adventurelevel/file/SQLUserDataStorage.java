package cc.mewcraft.adventurelevel.file;

import cc.mewcraft.adventurelevel.data.SimpleUserData;
import cc.mewcraft.adventurelevel.level.LevelFactory;
import cc.mewcraft.adventurelevel.level.category.Level;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import cc.mewcraft.adventurelevel.util.PlayerUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

@Singleton
public class SQLUserDataStorage extends AbstractUserDataStorage {

    private static final String DATA_POOL_NAME = "AdventureLevelHikariPool";

    // SQL config
    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;
    private final String parameters;

    private final int hikariMaximumPoolSize;
    private final int hikariMinimumIdle;
    private final int hikariMaximumLifetime;
    private final int hikariKeepAliveTime;
    private final int hikariConnectionTimeOut;

    // Table names
    private final String userdataTable;

    // SQL queries
    private final String insertUserdataQuery;
    private final String selectUserdataQuery;

    // Hikari instances
    private HikariDataSource connectionPool;

    // Logger
    private final Logger logger;

    @Inject
    public SQLUserDataStorage(
            final Logger logger
    ) {
        super();

        this.logger = logger;

        final AdventureLevelPlugin plugin = AdventureLevelPlugin.instance();

        this.host = requireNonNull(plugin.getConfig().getString("database.credentials.host"));
        this.port = requireNonNull(plugin.getConfig().getString("database.credentials.port"));
        this.database = requireNonNull(plugin.getConfig().getString("database.credentials.database"));
        this.username = requireNonNull(plugin.getConfig().getString("database.credentials.username"));
        this.password = requireNonNull(plugin.getConfig().getString("database.credentials.password"));
        this.parameters = requireNonNull(plugin.getConfig().getString("database.credentials.parameters"));

        this.hikariMaximumPoolSize = plugin.getConfig().getInt("database.connection_pool.maximum_pool_size");
        this.hikariMinimumIdle = plugin.getConfig().getInt("database.connection_pool.minimum_idle");
        this.hikariMaximumLifetime = plugin.getConfig().getInt("database.connection_pool.maximum_lifetime");
        this.hikariKeepAliveTime = plugin.getConfig().getInt("database.connection_pool.keep_alive_time");
        this.hikariConnectionTimeOut = plugin.getConfig().getInt("database.connection_pool.connection_timeout");

        this.userdataTable = requireNonNull(plugin.getConfig().getString("database.table_names.userdata"));

        this.insertUserdataQuery = """
                INSERT INTO %userdata_table%
                (`uuid`, `name`, `primary_exp`, `player_death_exp`, `entity_death_exp`, `furnace_exp`, `breed_exp`, `villager_trade_exp`, `fishing_exp`, `block_break_exp`, `exp_bottle_exp`, `grindstone_exp`)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE
                `uuid` = VALUES(`uuid`),
                `name` = VALUES(`name`),
                `primary_exp` = VALUES(`primary_exp`),
                `player_death_exp` = VALUES(`player_death_exp`),
                `entity_death_exp` = VALUES(`entity_death_exp`),
                `furnace_exp` = VALUES(`furnace_exp`),
                `breed_exp` = VALUES(`breed_exp`),
                `villager_trade_exp` = VALUES(`villager_trade_exp`),
                `fishing_exp` = VALUES(`fishing_exp`),
                `block_break_exp` = VALUES(`block_break_exp`),
                `exp_bottle_exp` = VALUES(`exp_bottle_exp`),
                `grindstone_exp` = VALUES(`grindstone_exp`);"""
                .replace("%userdata_table%", userdataTable);
        this.selectUserdataQuery = """
                SELECT * FROM %userdata_table% WHERE uuid = ?;"""
                .replace("%userdata_table%", userdataTable);
    }

    private void setupTables(Connection conn) throws SQLException {
        try (PreparedStatement stmt1 = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS
                %userdata_table% (
                `uuid` varchar(36) NOT NULL PRIMARY KEY,
                `name` varchar(16),
                `primary_exp` int(11) DEFAULT 0,
                `player_death_exp` int(11) DEFAULT 0,
                `entity_death_exp` int(11) DEFAULT 0,
                `furnace_exp` int(11) DEFAULT 0,
                `breed_exp` int(11) DEFAULT 0,
                `villager_trade_exp` int(11) DEFAULT 0,
                `fishing_exp` int(11) DEFAULT 0,
                `block_break_exp` int(11) DEFAULT 0,
                `exp_bottle_exp` int(11) DEFAULT 0,
                `grindstone_exp` int(11) DEFAULT 0);"""
                .replace("%userdata_table%", userdataTable)
        )) {
            stmt1.execute();
        }
    }

    @Override public void init() {
        HikariConfig hikariConfig = new HikariConfig();

        // Set jdbc driver connection url
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + parameters);
        hikariConfig.setPoolName(DATA_POOL_NAME);

        // Set authenticate
        hikariConfig.setPassword(password);
        hikariConfig.setUsername(username);

        // Set various additional parameters
        hikariConfig.setMaximumPoolSize(hikariMaximumPoolSize);
        hikariConfig.setMinimumIdle(hikariMinimumIdle);
        hikariConfig.setMaxLifetime(hikariMaximumLifetime);
        hikariConfig.setKeepaliveTime(hikariKeepAliveTime);
        hikariConfig.setConnectionTimeout(hikariConnectionTimeOut);

        // Establish connection & Create tables if needed
        connectionPool = new HikariDataSource(hikariConfig);
        try (Connection conn = connectionPool.getConnection()) {
            this.setupTables(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override public void close() {
        if (connectionPool != null) {
            connectionPool.close();
        }
    }

    @Override public @NonNull SimpleUserData create(final @NonNull UUID uuid) {
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertUserdataQuery)
        ) {
            // Construct a map of empty levels
            ConcurrentHashMap<LevelCategory, Level> levels = new ConcurrentHashMap<>() {{
                put(LevelCategory.PRIMARY, LevelFactory.newLevel(LevelCategory.PRIMARY));
                put(LevelCategory.PLAYER_DEATH, LevelFactory.newLevel(LevelCategory.PLAYER_DEATH));
                put(LevelCategory.ENTITY_DEATH, LevelFactory.newLevel(LevelCategory.ENTITY_DEATH));
                put(LevelCategory.FURNACE, LevelFactory.newLevel(LevelCategory.FURNACE));
                put(LevelCategory.BREED, LevelFactory.newLevel(LevelCategory.BREED));
                put(LevelCategory.VILLAGER_TRADE, LevelFactory.newLevel(LevelCategory.VILLAGER_TRADE));
                put(LevelCategory.FISHING, LevelFactory.newLevel(LevelCategory.FISHING));
                put(LevelCategory.BLOCK_BREAK, LevelFactory.newLevel(LevelCategory.BLOCK_BREAK));
                put(LevelCategory.EXP_BOTTLE, LevelFactory.newLevel(LevelCategory.EXP_BOTTLE));
                put(LevelCategory.GRINDSTONE, LevelFactory.newLevel(LevelCategory.GRINDSTONE));
            }};

            SimpleUserData data = new SimpleUserData(uuid, levels);

            stmt.setString(1, uuid.toString());
            stmt.setString(2, PlayerUtils.getName(uuid).toLowerCase());
            stmt.setInt(3, 0);
            stmt.setInt(4, 0);
            stmt.setInt(5, 0);
            stmt.setInt(6, 0);
            stmt.setInt(7, 0);
            stmt.setInt(8, 0);
            stmt.setInt(9, 0);
            stmt.setInt(10, 0);
            stmt.setInt(11, 0);
            stmt.setInt(12, 0);
            stmt.execute();

            logger.info("[{}] Created new userdata in SQL data storage: {}", PlayerUtils.getName(uuid), data);
            return data;
        } catch (SQLException e) {
            throw new IllegalStateException("failed to create new userdata in SQL data storage for player " + PlayerUtils.getName(uuid), e);
        }
    }

    @Override public @Nullable SimpleUserData load(final @NonNull UUID uuid) {
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectUserdataQuery)
        ) {
            // Note: string comparisons are case-insensitive by default in the configuration of SQL server database
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Read values from the query results
                    int primaryXp = rs.getInt(3);
                    int playerDeathXp = rs.getInt(4);
                    int entityDeathXp = rs.getInt(5);
                    int furnaceXp = rs.getInt(6);
                    int breedXp = rs.getInt(7);
                    int villagerTradeXp = rs.getInt(8);
                    int fishingXp = rs.getInt(9);
                    int blockBreakXp = rs.getInt(10);
                    int expBottleXp = rs.getInt(11);
                    int grindstoneXp = rs.getInt(12);

                    // Construct the map of levels with loaded xp
                    ConcurrentHashMap<LevelCategory, Level> levels = new ConcurrentHashMap<>() {{
                        put(LevelCategory.PRIMARY, LevelFactory.newLevel(LevelCategory.PRIMARY).withExperience(primaryXp));
                        put(LevelCategory.PLAYER_DEATH, LevelFactory.newLevel(LevelCategory.PLAYER_DEATH).withExperience(playerDeathXp));
                        put(LevelCategory.ENTITY_DEATH, LevelFactory.newLevel(LevelCategory.ENTITY_DEATH).withExperience(entityDeathXp));
                        put(LevelCategory.FURNACE, LevelFactory.newLevel(LevelCategory.FURNACE).withExperience(furnaceXp));
                        put(LevelCategory.BREED, LevelFactory.newLevel(LevelCategory.BREED).withExperience(breedXp));
                        put(LevelCategory.VILLAGER_TRADE, LevelFactory.newLevel(LevelCategory.VILLAGER_TRADE).withExperience(villagerTradeXp));
                        put(LevelCategory.FISHING, LevelFactory.newLevel(LevelCategory.FISHING).withExperience(fishingXp));
                        put(LevelCategory.BLOCK_BREAK, LevelFactory.newLevel(LevelCategory.BLOCK_BREAK).withExperience(blockBreakXp));
                        put(LevelCategory.EXP_BOTTLE, LevelFactory.newLevel(LevelCategory.EXP_BOTTLE).withExperience(expBottleXp));
                        put(LevelCategory.GRINDSTONE, LevelFactory.newLevel(LevelCategory.GRINDSTONE).withExperience(grindstoneXp));
                    }};

                    SimpleUserData data = new SimpleUserData(uuid, levels);
                    data.setPopulated(true);
                    logger.info("[{}] Loaded userdata from SQL data storage: {}", PlayerUtils.getName(uuid), data);
                    return data;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        logger.warn("[{}] Userdata not found in SQL data storage", PlayerUtils.getName(uuid));
        return null;
    }

    @Override public void save(final @NonNull SimpleUserData data) {
        if (!data.isPopulated()) {
            logger.info("[{}] Skipped saving userdata to SQL data storage", PlayerUtils.getName(data.getUuid()));
            return;
        }

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertUserdataQuery)
        ) {
            stmt.setString(1, data.getUuid().toString());
            stmt.setString(2, PlayerUtils.getName(data.getUuid()).toLowerCase());
            stmt.setInt(3, data.getLevel(LevelCategory.PRIMARY).getExperience());
            stmt.setInt(4, data.getLevel(LevelCategory.PLAYER_DEATH).getExperience());
            stmt.setInt(5, data.getLevel(LevelCategory.ENTITY_DEATH).getExperience());
            stmt.setInt(6, data.getLevel(LevelCategory.FURNACE).getExperience());
            stmt.setInt(7, data.getLevel(LevelCategory.BREED).getExperience());
            stmt.setInt(8, data.getLevel(LevelCategory.VILLAGER_TRADE).getExperience());
            stmt.setInt(9, data.getLevel(LevelCategory.FISHING).getExperience());
            stmt.setInt(10, data.getLevel(LevelCategory.BLOCK_BREAK).getExperience());
            stmt.setInt(11, data.getLevel(LevelCategory.EXP_BOTTLE).getExperience());
            stmt.setInt(12, data.getLevel(LevelCategory.GRINDSTONE).getExperience());
            stmt.execute();

            logger.info("[{}] Saved userdata to SQL data storage: {}", PlayerUtils.getName(data.getUuid()), data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
