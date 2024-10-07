package cc.mewcraft.adventurelevel.data;

import cc.mewcraft.adventurelevel.event.AdventureLevelDataLoadEvent;
import cc.mewcraft.adventurelevel.level.category.Level;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import net.kyori.examination.ExaminableProperty;
import net.kyori.examination.string.StringExaminer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class RealPlayerData implements PlayerData {
    /**
     * The plugin.
     */
    private final AdventureLevelPlugin plugin;
    /**
     * The player's UUID.
     */
    private final UUID uuid;
    /**
     * A map containing all levels.
     */
    private final ConcurrentHashMap<LevelCategory, Level> levelData;
    /**
     * A variable indicating whether this player data has been fully loaded. If true (=complete), that means the data
     * has been fully loaded, and getters will return current values; otherwise, false (=incomplete) means it's not been
     * fully loaded and the returned values should not be used.
     */
    private final AtomicBoolean isComplete = new AtomicBoolean(false);

    /**
     * This constructor is used to fast create an empty PlayerData in the main thread.
     * <p>
     * An async callback is meant to be used to update its states at a later point of time.
     *
     * @param plugin the plugin instance
     * @param uuid   the uuid of backed player
     */
    public RealPlayerData(
            final AdventureLevelPlugin plugin,
            final UUID uuid
    ) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.levelData = new ConcurrentHashMap<>();
    }

    /**
     * You must pass in a complete set of data to this constructor.
     *
     * @param plugin    the plugin instance
     * @param uuid      the uuid of backed player
     * @param levelData the map must already be filled with instances of all levels
     */
    public RealPlayerData(
            final AdventureLevelPlugin plugin,
            final UUID uuid,
            final ConcurrentHashMap<LevelCategory, Level> levelData
    ) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.levelData = levelData;
    }

    @Override public @NonNull UUID getUuid() {
        return uuid;
    }

    @Override public @NonNull Level getLevel(LevelCategory category) {
        return Objects.requireNonNull(levelData.get(category), "category");
    }

    @Override public @NonNull Map<LevelCategory, Level> asMap() {
        return levelData;
    }

    @Override public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    @Override public boolean isOnline() {
        Player player = getPlayer();
        return player != null && player.isOnline();
    }

    @Override public boolean isConnected() {
        Player player = getPlayer();
        return player != null && player.isConnected();
    }

    @Override public boolean complete() {
        return isComplete.get();
    }

    @Override public @NonNull PlayerData markAsIncomplete() {
        isComplete.set(false);
        return this;
    }

    @Override public @NonNull PlayerData markAsComplete() {
        isComplete.set(true);
        new AdventureLevelDataLoadEvent(this).callEvent();
        return this;
    }

    @Override public @NonNull String toSimpleString() {
        if (complete()) {
            return "RealPlayerData{uuid=" + uuid + ", primaryExp=" + getLevel(LevelCategory.PRIMARY).getExperience() + "}";
        } else {
            return "RealPlayerData{uuid=" + uuid + "}";
        }
    }

    @Override public @NotNull Stream<? extends ExaminableProperty> examinableProperties() {
        return Stream.of(
                ExaminableProperty.of("uuid", this.uuid),
                ExaminableProperty.of("levelData", this.levelData),
                ExaminableProperty.of("isComplete", this.isComplete)
        );
    }

    @Override public String toString() {
        return StringExaminer.simpleEscaping().examine(this);
    }
}
