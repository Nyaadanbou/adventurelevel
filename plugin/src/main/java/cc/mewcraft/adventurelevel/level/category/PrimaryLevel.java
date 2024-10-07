package cc.mewcraft.adventurelevel.level.category;

import cc.mewcraft.adventurelevel.event.AdventureLevelExpChangeEvent;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.ezylang.evalex.Expression;
import com.google.common.collect.RangeMap;
import org.bukkit.entity.ExperienceOrb;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class PrimaryLevel extends AbstractLevel {

    private final Map<ExperienceOrb.SpawnReason, Double> experienceModifiers;

    public PrimaryLevel(
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
        final ExperienceOrb orb = event.getExperienceOrb();
        final double amount = orb.getExperience();
        final double modifier = experienceModifiers.get(orb.getSpawnReason());
        int result = BigDecimal
                .valueOf(amount * modifier) // apply global modifiers
                .setScale(0, RoundingMode.HALF_DOWN)
                .intValue();

        if (AdventureLevelExpChangeEvent.getHandlerList().getRegisteredListeners().length != 0) {
            int previousExp = totalExperience;
            int changingExp = result;
            AdventureLevelExpChangeEvent event1 = new AdventureLevelExpChangeEvent(
                    event.getPlayer(),
                    AdventureLevelExpChangeEvent.Action.OFFSET,
                    previousExp,
                    changingExp
            );
            if (!event1.callEvent()) return;
            result = event1.getChangingExp();
        }

        this.addExperience(result);
    }
}
