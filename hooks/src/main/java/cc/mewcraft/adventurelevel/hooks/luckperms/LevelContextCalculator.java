package cc.mewcraft.adventurelevel.hooks.luckperms;

import cc.mewcraft.adventurelevel.data.UserData;
import cc.mewcraft.adventurelevel.data.UserDataRepository;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import jakarta.inject.Inject;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class LevelContextCalculator {
    @NonNull private final UserDataRepository userDataRepository;

    @Inject
    public LevelContextCalculator(final @NotNull UserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
    }

    public void register() {
        // Implementation Notes:
        // The LuckPerms contexts are requested at early stage of player initialization.
        // Usually the PlayerData is not available upon requesting, so we must return
        // "dummy" values when not available yet.

        LuckPermsProvider.get().getContextManager().registerCalculator((target, consumer) -> {
            final UserData data = userDataRepository.getCached(((OfflinePlayer) target).getUniqueId());
            final String key = "adventure-level";
            final String value = data == null ? "0" : String.valueOf(data.getLevel(LevelCategory.PRIMARY));
            consumer.accept(key, value);
        });
    }
}
