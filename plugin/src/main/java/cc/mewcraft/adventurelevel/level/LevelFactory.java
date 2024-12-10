package cc.mewcraft.adventurelevel.level;

import cc.mewcraft.adventurelevel.level.category.Level;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.Objects;

public final class LevelFactory {
    public static @NonNull Level newLevel(@NonNull LevelCategory category) {
        AdventureLevelPlugin plugin = AdventureLevelPlugin.instance();
        ConfigurationSection config;
        return switch (category) {

            // Create Primary Level
            case PRIMARY: {
                config = Objects.requireNonNull(plugin.getConfig().getConfigurationSection("primary_level"));
                yield LevelBuilder.builder(plugin, config).build(category);
            }

            // Create Categorical Levels
            case PLAYER_DEATH:
            case ENTITY_DEATH:
            case FURNACE:
            case BREED:
            case VILLAGER_TRADE:
            case FISHING:
            case BLOCK_BREAK:
            case EXP_BOTTLE:
            case GRINDSTONE: {
                File file = plugin.getDataFolder().toPath()
                        .resolve("calc")
                        .resolve(category.name().toLowerCase() + ".yml")
                        .toFile();
                config = YamlConfiguration.loadConfiguration(file);
                yield LevelBuilder.builder(plugin, config).build(category);
            }
        };
    }
}
