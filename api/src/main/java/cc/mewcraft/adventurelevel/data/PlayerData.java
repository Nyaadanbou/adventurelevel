package cc.mewcraft.adventurelevel.data;

import cc.mewcraft.adventurelevel.level.category.Level;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public interface PlayerData {

    /**
     * This DUMMY instance is only used in the cases where:
     * <ol>
     *     <li>requested data does not exist in the datasource</li>
     *     <li>exceptions have occurred upon fetching the data</li>
     * </ol>
     */
    DummyPlayerData DUMMY = new DummyPlayerData();

    @NotNull UUID getUuid();

    @NotNull Level getLevel(LevelCategory category);

    @NotNull Map<LevelCategory, Level> asMap();

    /**
     * 方便获取 {@link #getUuid()} 对应的 {@link Player}.
     */
    @Nullable Player getPlayer();

    /**
     * 方便调用对应玩家的 {@link Player#isOnline()}.
     */
    boolean isOnline();

    /**
     * 方便调用对应玩家的 {@link Player#isConnected()}.
     */
    boolean isConnected();

    /**
     * Checks whether this PlayerData has been fully loaded, i.e., its states are valid and up-to-date.
     *
     * @return true if this PlayerData has been fully loaded (states are up-to-date); otherwise false
     */
    boolean complete();

    /**
     * Marks this PlayerData as not fully loaded.
     *
     * @return this object
     */
    PlayerData markAsIncomplete();

    /**
     * Marks this PlayerData as fully loaded.
     *
     * @return this object
     */
    PlayerData markAsComplete();
}
