package cc.mewcraft.adventurelevel.level.category;

public enum LevelCategory {

    /**
     * 经验来源由实现定义
     */
    PRIMARY,
    /**
     * 经验来源是 {@link org.bukkit.entity.ExperienceOrb.SpawnReason#PLAYER_DEATH}
     */
    PLAYER_DEATH,
    /**
     * 经验来源是 {@link org.bukkit.entity.ExperienceOrb.SpawnReason#ENTITY_DEATH}
     */
    ENTITY_DEATH,
    /**
     * 经验来源是 {@link org.bukkit.entity.ExperienceOrb.SpawnReason#FURNACE}
     */
    FURNACE,
    /**
     * 经验来源是 {@link org.bukkit.entity.ExperienceOrb.SpawnReason#BREED}
     */
    BREED,
    /**
     * 经验来源是 {@link org.bukkit.entity.ExperienceOrb.SpawnReason#VILLAGER_TRADE}
     */
    VILLAGER_TRADE,
    /**
     * 经验来源是 {@link org.bukkit.entity.ExperienceOrb.SpawnReason#FISHING}
     */
    FISHING,
    /**
     * 经验来源是 {@link org.bukkit.entity.ExperienceOrb.SpawnReason#BLOCK_BREAK}
     */
    BLOCK_BREAK,
    /**
     * 经验来源是 {@link org.bukkit.entity.ExperienceOrb.SpawnReason#EXP_BOTTLE}
     */
    EXP_BOTTLE,
    /**
     * 经验来源是 {@link org.bukkit.entity.ExperienceOrb.SpawnReason#GRINDSTONE}
     */
    GRINDSTONE,

}
