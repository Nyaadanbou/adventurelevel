package cc.mewcraft.adventurelevel.level.category;

import cc.mewcraft.adventurelevel.level.modifier.ExperienceModifier;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import com.google.common.collect.RangeMap;
import net.kyori.examination.ExaminableProperty;
import net.kyori.examination.string.StringExaminer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractLevel implements Level {

    protected final AdventureLevelPlugin plugin;
    protected final int maxLevel;
    protected int totalExperience;
    protected final Map<String, ExperienceModifier> additiveModifiers;
    protected final Map<String, ExperienceModifier> multiplicativeModifiers;

    /**
     * @see #calculateTotalExperience(int)
     */
    protected final RangeMap<Integer, Expression> convertLevelToExpFormula;
    /**
     * @see #calculateTotalLevel(int)
     */
    protected final RangeMap<Integer, Expression> convertExpToLevelFormula;
    /**
     * @see #calculateNeededExperience(int)
     */
    protected final RangeMap<Integer, Expression> expUntilNextLevelFormula;

    public AbstractLevel(
            final AdventureLevelPlugin plugin,
            final int maxLevel,
            final RangeMap<Integer, Expression> convertLevelToExpFormula,
            final RangeMap<Integer, Expression> convertExpToLevelFormula,
            final RangeMap<Integer, Expression> expUntilNextLevelFormula
    ) {
        this.plugin = plugin;
        this.maxLevel = maxLevel;
        this.convertLevelToExpFormula = convertLevelToExpFormula;
        this.convertExpToLevelFormula = convertExpToLevelFormula;
        this.expUntilNextLevelFormula = expUntilNextLevelFormula;
        this.additiveModifiers = new HashMap<>();
        this.multiplicativeModifiers = new HashMap<>();
    }

    @Override public void handleEvent(final PlayerPickupExperienceEvent event) {
        this.addExperience(event.getExperienceOrb().getExperience());
    }

    @Override public int calculateTotalExperience(final int level) {
        try {
            return convertLevelToExpFormula
                    .get(level)
                    .with("x", level)
                    .evaluate()
                    .getNumberValue()
                    .intValue();
        } catch (EvaluationException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public int calculateNeededExperience(final int currentLevel) {
        try {
            return expUntilNextLevelFormula
                    .get(currentLevel)
                    .with("x", currentLevel)
                    .evaluate()
                    .getNumberValue()
                    .intValue();
        } catch (EvaluationException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public double calculateTotalLevel(final int totalExp) {
        try {
            return convertExpToLevelFormula
                    .get(totalExp)
                    .with("x", totalExp)
                    .evaluate()
                    .getNumberValue()
                    .doubleValue();
        } catch (EvaluationException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public int getExperience() {
        return totalExperience;
    }

    @Override public int setExperience(final int value) {
        int oldValue = totalExperience;
        if (value < 0) return oldValue;
        totalExperience = value;
        return oldValue;
    }

    @Override public int addExperience(final int value) {
        // Store old value
        int oldValue = totalExperience;

        if (value < 1) return oldValue; // return earlier for performance reasons

        double additiveMod = 0D;
        double multiplicativeMod = 1D;

        // Sum additive modifiers
        for (final ExperienceModifier mod : this.getExperienceModifiers(ExperienceModifier.Type.ADDITIVE).values()) {
            additiveMod += mod.getValue();
        }
        // Sum multiplicative modifiers
        for (final ExperienceModifier mod : this.getExperienceModifiers(ExperienceModifier.Type.MULTIPLICATIVE).values()) {
            multiplicativeMod *= mod.getValue();
        }

        totalExperience += (int) (value * Math.max(0, 1 + additiveMod) * Math.max(0, multiplicativeMod));
        return oldValue;
    }

    @Override public @NonNull Map<String, ExperienceModifier> getExperienceModifiers(ExperienceModifier.Type type) {
        return type == ExperienceModifier.Type.ADDITIVE ? additiveModifiers : multiplicativeModifiers;
    }

    @Override public void addExperienceModifier(final String key, final ExperienceModifier modifier, ExperienceModifier.Type type) {
        this.getExperienceModifiers(type).put(key.toLowerCase(Locale.ROOT), modifier);
    }

    @Override public void removeExperienceModifier(final String key, ExperienceModifier.Type type) {
        this.getExperienceModifiers(type).remove(key.toLowerCase(Locale.ROOT));
    }

    @Override public void clearExperienceModifiers() {
        additiveModifiers.clear();
        multiplicativeModifiers.clear();
    }

    @Override public int getLevel() {
        return (int) Math.min(this.getMaxLevel(), this.calculateTotalLevel(totalExperience)); // cap the level
    }

    @Override public int setLevel(int level) {
        int oldLevel = this.getLevel();
        int newTotalExp = this.calculateTotalExperience(level);
        this.setExperience(newTotalExp);
        return oldLevel;
    }

    @Override public int addLevel(final int level) {
        int oldLevel = this.getLevel();
        int newTotalExp = this.calculateTotalExperience(oldLevel + level);
        this.setExperience(newTotalExp);
        return oldLevel;
    }

    @Override public int getMaxLevel() {
        return maxLevel;
    }

    @Override public @NotNull Stream<? extends ExaminableProperty> examinableProperties() {
        return Stream.of(
                ExaminableProperty.of("totalExperience", this.totalExperience)
        );
    }

    @Override public String toString() {
        return StringExaminer.simpleEscaping().examine(this);
    }
}
