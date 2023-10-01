package cc.mewcraft.adventurelevel.level.category;

import cc.mewcraft.adventurelevel.AdventureLevelPlugin;
import com.ezylang.evalex.Expression;
import com.google.common.collect.RangeMap;
import com.google.inject.Inject;

@SuppressWarnings("UnstableApiUsage")
public class EntityDeathLevel extends AbstractLevel {
    @Inject
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
