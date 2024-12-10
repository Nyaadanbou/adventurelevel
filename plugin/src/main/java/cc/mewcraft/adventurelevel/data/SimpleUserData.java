package cc.mewcraft.adventurelevel.data;

import cc.mewcraft.adventurelevel.level.category.Level;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class SimpleUserData implements UserData, Examinable {

    /**
     * The player's UUID.
     */
    private final UUID uuid;

    /**
     * All {@link Level} instances of the player.
     */
    private final ConcurrentHashMap<LevelCategory, Level> levels;

    /**
     * A variable indicating whether this player data has been fully loaded. A {@code true} means the data
     * has been fully loaded, and getters will return current values; otherwise, a {@code false} means it's
     * not been fully loaded and the returned values should not be used.
     */
    private final AtomicBoolean isPopulated = new AtomicBoolean(false);

    /**
     * This constructor is used to fast create an empty PlayerData in the main thread.
     * <p>
     * An async callback should be used to populate its states at a later point of time.
     *
     * @param uuid the uuid of backed player
     */
    public SimpleUserData(
            final UUID uuid
    ) {
        this.uuid = uuid;
        this.levels = new ConcurrentHashMap<>();
    }

    /**
     * You must pass in a complete data to this constructor.
     *
     * @param uuid the uuid of backed player
     * @param levels the map must already be filled with instances of all levels
     */
    public SimpleUserData(
            final UUID uuid,
            final ConcurrentHashMap<LevelCategory, Level> levels
    ) {
        this.uuid = uuid;
        this.levels = levels;
    }

    public boolean isPopulated() {
        return isPopulated.get();
    }

    public void setPopulated(boolean mark) {
        isPopulated.set(mark);
    }

    public @NonNull Map<LevelCategory, Level> asMap() {
        return levels;
    }

    @Override public @NonNull UUID getUuid() {
        return uuid;
    }

    @Override public @NonNull Level getLevel(final LevelCategory category) {
        return Objects.requireNonNull(levels.get(category), "category");
    }

    @Override public @NotNull Stream<? extends ExaminableProperty> examinableProperties() {
        return Stream.of(
                ExaminableProperty.of("uuid", this.uuid),
                ExaminableProperty.of("levels", this.levels),
                ExaminableProperty.of("isPopulated", this.isPopulated)
        );
    }

    @Override public String toString() {
        if (isPopulated()) {
            return "SimpleUserData{uuid=" + uuid + ", primaryExp=" + getLevel(LevelCategory.PRIMARY).getExperience() + "}";
        } else {
            return "SimpleUserData{uuid=" + uuid + "}";
        }
    }
}
