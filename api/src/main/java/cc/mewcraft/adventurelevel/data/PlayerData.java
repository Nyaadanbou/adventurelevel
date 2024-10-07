package cc.mewcraft.adventurelevel.data;

import cc.mewcraft.adventurelevel.level.category.Level;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import net.kyori.examination.Examinable;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.UUID;

public interface PlayerData extends Examinable {

    /**
     * This DUMMY instance is only used in the cases where:
     * <ol>
     *     <li>requested data does not exist in the datasource</li>
     *     <li>exceptions have occurred upon fetching the data</li>
     * </ol>
     */
    DummyPlayerData DUMMY = new DummyPlayerData();

    /**
     * Returns the UUID of the player.
     */
    @NonNull UUID getUuid();

    /**
     * Returns the level of the specified category.
     *
     * @param category the category of the level
     * @return the level of the specified category
     */
    @NonNull Level getLevel(LevelCategory category);

    /**
     * Returns a map containing all {@link Level} instances.
     */
    @NonNull Map<LevelCategory, Level> asMap();

    /**
     * 方便获取 {@link #getUuid()} 对应的 {@link Player}.
     */
    @Nullable Player getPlayer();

    /**
     * 相当于调用 {@link Player#isOnline()}.
     */
    boolean isOnline();

    /**
     * 相当于调用 {@link Player#isConnected()}.
     */
    boolean isConnected();

    /**
     * Checks whether this PlayerData has been fully loaded, i.e., its states are valid and up-to-date.
     *
     * @return true if this PlayerData has been fully loaded (states are up-to-date); otherwise false
     */
    boolean complete();

    /**
     * Marks this data as not fully loaded.
     *
     * @return this object
     */
    @NonNull PlayerData markAsIncomplete();

    /**
     * Marks this data as fully loaded.
     *
     * @return this object
     */
    @NonNull PlayerData markAsComplete();

    /**
     * Returns a simple string representation of this object.
     */
    @NonNull String toSimpleString();
}
