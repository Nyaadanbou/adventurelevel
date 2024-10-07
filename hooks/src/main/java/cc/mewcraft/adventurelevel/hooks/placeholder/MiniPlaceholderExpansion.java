package cc.mewcraft.adventurelevel.hooks.placeholder;

import cc.mewcraft.adventurelevel.data.PlayerData;
import cc.mewcraft.adventurelevel.data.PlayerDataManager;
import cc.mewcraft.adventurelevel.level.category.Level;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import io.github.miniplaceholders.api.Expansion;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.lucko.helper.terminable.Terminable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Singleton
public class MiniPlaceholderExpansion implements Terminable {
    private static final Tag EMPTY_TAG = Tag.selfClosingInserting(Component.empty());
    private final PlayerDataManager playerDataManager;
    private Expansion expansion;

    @Inject
    public MiniPlaceholderExpansion(final PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public MiniPlaceholderExpansion register() {
        expansion = Expansion.builder("adventurelevel")
                .filter(Player.class)

                // return current primary level
                .audiencePlaceholder("level", (audience, queue, ctx) -> {
                    PlayerData data = playerDataManager.load((Player) audience);
                    if (!data.complete()) return EMPTY_TAG;
                    String primaryLevel = String.valueOf(data.getLevel(LevelCategory.PRIMARY).getLevel());
                    return Tag.preProcessParsed(primaryLevel);
                })

                // return progress to next level in percent (1-99)
                .audiencePlaceholder("level_progress", (audience, queue, ctx) -> {
                    PlayerData data = playerDataManager.load((Player) audience);
                    if (!data.complete()) return EMPTY_TAG;
                    Level primaryLevel = data.getLevel(LevelCategory.PRIMARY);
                    int currExp = primaryLevel.getExperience();
                    double currLevel = primaryLevel.calculateTotalLevel(currExp);
                    String text = BigDecimal.valueOf(currLevel % 1)
                            .scaleByPowerOfTen(2)
                            .setScale(0, RoundingMode.FLOOR)
                            .toPlainString();
                    return Tag.preProcessParsed(text);
                })

                // return total experience
                .audiencePlaceholder("experience", (audience, queue, ctx) -> {
                    PlayerData data = playerDataManager.load((Player) audience);
                    if (!data.complete()) return EMPTY_TAG;
                    String primaryLevel = String.valueOf(data.getLevel(LevelCategory.PRIMARY).getExperience());
                    return Tag.preProcessParsed(primaryLevel);
                })

                // return experience gained for current progress
                .audiencePlaceholder("experience_progress", (audience, queue, ctx) -> {
                    PlayerData data = playerDataManager.load((Player) audience);
                    if (!data.complete()) return EMPTY_TAG;
                    Level primaryLevel = data.getLevel(LevelCategory.PRIMARY);
                    int level = primaryLevel.getLevel();
                    int currExp = primaryLevel.getExperience();
                    int totalExp = primaryLevel.calculateTotalExperience(level);
                    String text = String.valueOf(currExp - totalExp);
                    return Tag.preProcessParsed(text);
                })

                // return experience needed to get to next level from current level
                .audiencePlaceholder("experience_progress_max", (audience, queue, ctx) -> {
                    PlayerData data = playerDataManager.load((Player) audience);
                    if (!data.complete()) return EMPTY_TAG;
                    Level primaryLevel = data.getLevel(LevelCategory.PRIMARY);
                    int level = primaryLevel.getLevel();
                    int expUntilNextLevel = primaryLevel.calculateNeededExperience(level + 1);
                    String text = String.valueOf(expUntilNextLevel);
                    return Tag.preProcessParsed(text);
                })

                // build the expansion
                .build();

        expansion.register();

        return this;
    }

    @Override public void close() {
        expansion.unregister();
    }
}
