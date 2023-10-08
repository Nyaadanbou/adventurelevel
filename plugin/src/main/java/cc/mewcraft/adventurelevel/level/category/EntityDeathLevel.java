package cc.mewcraft.adventurelevel.level.category;

import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import com.ezylang.evalex.Expression;

import com.google.common.collect.RangeMap;


@SuppressWarnings("UnstableApiUsage")
public class EntityDeathLevel extends AbstractLevel {
    public EntityDeathLevel(
            final AdventureLevelPlugin plugin,
            final int maxLevel,
            final RangeMap<Integer, Expression> levelToExpFormulae,
            final RangeMap<Integer, Expression> expToLevelFormulae,
            final RangeMap<Integer, Expression> nextLevelFormulae
    ) {
        super(plugin, maxLevel, levelToExpFormulae, expToLevelFormulae, nextLevelFormulae);
    }
}
