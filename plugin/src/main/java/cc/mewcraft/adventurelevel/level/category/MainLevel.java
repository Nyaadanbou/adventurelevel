package cc.mewcraft.adventurelevel.level.category;

import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.ezylang.evalex.Expression;
import org.bukkit.entity.ExperienceOrb;

import com.google.common.collect.RangeMap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class MainLevel extends AbstractLevel {

    private final Map<ExperienceOrb.SpawnReason, Double> experienceModifiers;

    public MainLevel(
            final AdventureLevelPlugin plugin,
            final int maxLevel,
            final RangeMap<Integer, Expression> convertLevelToExpFormula,
            final RangeMap<Integer, Expression> convertExpToLevelFormula,
            final RangeMap<Integer, Expression> expUntilNextLevelFormula,
            final Map<ExperienceOrb.SpawnReason, Double> experienceModifiers
    ) {
        super(plugin, maxLevel, convertLevelToExpFormula, convertExpToLevelFormula, expUntilNextLevelFormula);
        this.experienceModifiers = experienceModifiers;
    }

    @Override public void handleEvent(final PlayerPickupExperienceEvent event) {
        ExperienceOrb orb = event.getExperienceOrb();
        double amount = orb.getExperience();
        double modifier = experienceModifiers.get(orb.getSpawnReason());
        int result = BigDecimal
                .valueOf(amount * modifier) // apply global modifiers
                .setScale(0, RoundingMode.HALF_DOWN)
                .intValue();
        this.addExperience(result);
    }
}
