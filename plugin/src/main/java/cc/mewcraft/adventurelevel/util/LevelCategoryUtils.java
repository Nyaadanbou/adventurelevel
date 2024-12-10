package cc.mewcraft.adventurelevel.util;

import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import org.bukkit.entity.ExperienceOrb;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LevelCategoryUtils {

    /**
     * @param reason the spawn reason
     * @return returns an empty if the SpawnReason should not be counted
     */
    public static @Nullable LevelCategory get(ExperienceOrb.@NonNull SpawnReason reason) {
        return switch (reason) {
            case PLAYER_DEATH -> LevelCategory.PLAYER_DEATH;
            case ENTITY_DEATH -> LevelCategory.ENTITY_DEATH;
            case FURNACE -> LevelCategory.FURNACE;
            case BREED -> LevelCategory.BREED;
            case VILLAGER_TRADE -> LevelCategory.VILLAGER_TRADE;
            case FISHING -> LevelCategory.FISHING;
            case BLOCK_BREAK -> LevelCategory.BLOCK_BREAK;
            case EXP_BOTTLE -> LevelCategory.EXP_BOTTLE;
            case GRINDSTONE -> LevelCategory.GRINDSTONE;
            case CUSTOM, UNKNOWN -> null;
        };
    }

    private LevelCategoryUtils() {
        throw new UnsupportedOperationException();
    }

}
