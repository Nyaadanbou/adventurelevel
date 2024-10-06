package cc.mewcraft.adventurelevel.hooks.luckperms;

import cc.mewcraft.adventurelevel.data.PlayerData;
import cc.mewcraft.adventurelevel.data.PlayerDataManager;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import jakarta.inject.Inject;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class LevelContextCalculator {
    private final PlayerDataManager playerDataManager;

    @Inject
    public LevelContextCalculator(final @NotNull PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public void register() {
        // Implementation Notes:
        // The LuckPerms contexts are requested at early stage of player initialization.
        // Usually the PlayerData is not available upon requesting, so we must return
        // "dummy" values when not available yet.

        LuckPermsProvider.get().getContextManager().registerCalculator((target, consumer) -> {
            PlayerData data = playerDataManager.load((OfflinePlayer) target);
            consumer.accept("adventure-level",
                    data.complete()
                            ? String.valueOf(data.getLevel(LevelCategory.MAIN).getLevel())
                            : "0"
            );
        });
    }
}
