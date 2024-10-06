package cc.mewcraft.adventurelevel.level.category;

import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import com.ezylang.evalex.Expression;

import com.google.common.collect.RangeMap;


@SuppressWarnings("UnstableApiUsage")
public class FishingLevel extends AbstractLevel {
    public FishingLevel(
            final AdventureLevelPlugin plugin,
            final int maxLevel,
            final RangeMap<Integer, Expression> convertLevelToExpFormula,
            final RangeMap<Integer, Expression> convertExpToLevelFormula,
            final RangeMap<Integer, Expression> expUntilNextLevelFormula
    ) {
        super(plugin, maxLevel, convertLevelToExpFormula, convertExpToLevelFormula, expUntilNextLevelFormula);
    }
}
