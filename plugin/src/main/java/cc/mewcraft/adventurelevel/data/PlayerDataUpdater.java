package cc.mewcraft.adventurelevel.data;

import cc.mewcraft.adventurelevel.level.LevelFactory;
import cc.mewcraft.adventurelevel.level.category.Level;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import cc.mewcraft.adventurelevel.message.packet.PlayerDataPacket;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Given a reference of PlayerData and a data source, this class provides convenient methods to update the states of
 * given PlayerData from the specific data source.
 * <p>
 * By design, the given reference of PlayerData does not have its states fully loaded initially (cuz we want fast create
 * it on the main thread), and then we update the states at a later point of time using an asynchronous callback, to
 * allow the PlayerData to be fully loaded.
 *
 * @see RealPlayerData#RealPlayerData(AdventureLevelPlugin, UUID)
 */
public final class PlayerDataUpdater {

    /**
     * Updates states of the specific PlayerData from given PlayerDataPacket.
     *
     * @param data   a data to be updated - this will be the return value
     * @param source a data sent on the network from which the states are copied
     * @return an updated {@code data} (the reference remains unchanged)
     */
    public static @NotNull PlayerData update(final @NotNull PlayerData data, final @NotNull PlayerDataPacket source) {
        for (final LevelCategory category : LevelCategory.values()) {
            Level level = data.asMap().computeIfAbsent(category, LevelFactory::newLevel);
            level.setExperience(source.getExpByCategory(category));
        }

        return data; // reference remains unchanged
    }

    /**
     * Updates states of the specific PlayerData from another PlayerData.
     *
     * @param data   a data to be updated - this will be the return value
     * @param source a data loaded from disk which the states are copied from
     * @return an updated {@code data} (the reference remains unchanged)
     */
    public static @NotNull PlayerData update(final @NotNull PlayerData data, final @NotNull PlayerData source) {
        for (final LevelCategory category : LevelCategory.values()) {
            Level level = data.asMap().computeIfAbsent(category, LevelFactory::newLevel);
            level.setExperience(source.getLevel(category).getExperience());
        }

        return data; // reference remains unchanged
    }

    private PlayerDataUpdater() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
