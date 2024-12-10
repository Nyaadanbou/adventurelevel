package cc.mewcraft.adventurelevel.hooks.placeholder;

import cc.mewcraft.adventurelevel.data.UserData;
import cc.mewcraft.adventurelevel.data.UserDataRepository;
import cc.mewcraft.adventurelevel.level.category.Level;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.lucko.helper.terminable.Terminable;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Singleton
public class PAPIPlaceholderExpansion implements Terminable {
    @NonNull private final UserDataRepository userDataRepository;
    @MonotonicNonNull private AdventureLevelExpansion placeholderExpansion;

    @Inject
    public PAPIPlaceholderExpansion(@NonNull final UserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
    }

    public PAPIPlaceholderExpansion register() {
        placeholderExpansion = new AdventureLevelExpansion();
        placeholderExpansion.register();
        return this;
    }

    @SuppressWarnings("ConstantValue")
    @Override public void close() {
        // 服务器关闭时, 如果 PlaceholderAPI 先于本插件关闭, 则可能为空
        if (placeholderExpansion.getPlaceholderAPI() != null) {
            placeholderExpansion.unregister();
        }
    }

    private class AdventureLevelExpansion extends PlaceholderExpansion {
        @Override public @Nullable String onRequest(final OfflinePlayer player, final @NonNull String params) {
            UserData data = userDataRepository.getCached(player.getUniqueId());
            if (data == null) return "";

            Level primary = data.getLevel(LevelCategory.PRIMARY);

            return switch (params) {
                case "level" -> String.valueOf(primary.getLevel());
                case "level_progress" -> {
                    int currExp = primary.getExperience();
                    double currLevel = primary.calculateTotalLevel(currExp);
                    yield BigDecimal.valueOf(currLevel % 1)
                            .scaleByPowerOfTen(2)
                            .setScale(0, RoundingMode.FLOOR)
                            .toPlainString();
                }
                case "experience" -> String.valueOf(primary.getExperience());
                case "experience_progress" -> {
                    int level = primary.getLevel();
                    int currExp = primary.getExperience();
                    int totalExp = primary.calculateTotalExperience(level);
                    yield String.valueOf(currExp - totalExp);
                }
                case "experience_progress_max" -> {
                    int level = primary.getLevel();
                    int expUntilNextLevel = primary.calculateNeededExperience(level + 1);
                    yield String.valueOf(expUntilNextLevel);
                }
                default -> "";
            };
        }

        @Override public @NonNull String getIdentifier() {
            return "adventurelevel";
        }

        @Override public @NonNull String getAuthor() {
            return "Nailm";
        }

        @Override public @NonNull String getVersion() {
            return "1.0.0";
        }

        @Override public boolean persist() {
            return true;
        }
    }
}
