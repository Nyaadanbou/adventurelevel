package cc.mewcraft.adventurelevel.data;

import cc.mewcraft.adventurelevel.level.category.Level;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import cc.mewcraft.adventurelevel.level.modifier.ExperienceModifier;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DummyPlayerData implements PlayerData {
    private static final DummyLevel DUMMY_LEVEL = new DummyLevel();
    private static final UUID DUMMY_UUID = new UUID(0, 0);

    @Override public @NonNull UUID getUuid() {
        return DUMMY_UUID;
    }

    @Override public @NonNull Level getLevel(final LevelCategory category) {
        return DUMMY_LEVEL;
    }

    @Override public @NonNull Map<LevelCategory, Level> asMap() {
        return new HashMap<>();
    }

    @Override public @Nullable Player getPlayer() {
        return null;
    }

    @Override public boolean isOnline() {
        return false;
    }

    @Override public boolean isConnected() {
        return false;
    }

    @Override public boolean complete() {
        return false; // always incomplete
    }

    @Override public @NonNull PlayerData markAsIncomplete() {
        return this;
    }

    @Override public @NonNull PlayerData markAsComplete() {
        return this;
    }

    @Override public @NonNull String toSimpleString() {
        return getClass().getSimpleName();
    }

    /**
     * A Dummy Level that does nothing.
     */
    private static class DummyLevel implements Level {
        @Override public void handleEvent(final PlayerPickupExperienceEvent event) {}

        @Override public int calculateTotalExperience(final int level) {return 0;}

        @Override public int calculateNeededExperience(final int currentLevel) {return 0;}

        @Override public double calculateTotalLevel(final int totalExp) {return 0;}

        @Override public int getExperience() {return 0;}

        @Override public int setExperience(final int value) {return 0;}

        @Override public int addExperience(final int value) {return 0;}

        @Override public @NonNull Map<String, ExperienceModifier> getExperienceModifiers(final ExperienceModifier.Type type) {return new HashMap<>();}

        @Override public void addExperienceModifier(final String key, final ExperienceModifier modifier, final ExperienceModifier.Type type) {}

        @Override public void removeExperienceModifier(final String key, final ExperienceModifier.Type type) {}

        @Override public void clearExperienceModifiers() {}

        @Override public int getLevel() {return 0;}

        @Override public int setLevel(final int level) {return 0;}

        @Override public int addLevel(final int level) {return 0;}

        @Override public int getMaxLevel() {return 0;}
    }

}
