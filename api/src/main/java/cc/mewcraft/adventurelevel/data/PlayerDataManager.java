package cc.mewcraft.adventurelevel.data;

import me.lucko.helper.promise.Promise;
import me.lucko.helper.terminable.Terminable;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.UUID;

public interface PlayerDataManager extends Terminable {
    @NonNull PlayerData load(@NonNull UUID uuid);

    default @NonNull PlayerData load(@NonNull OfflinePlayer player) {
        return load(player.getUniqueId());
    }

    @NonNull Promise<PlayerData> save(@NonNull PlayerData playerData);

    default @NonNull Promise<PlayerData> save(@NonNull OfflinePlayer player) {
        return save(load(player.getUniqueId()));
    }

    @NonNull UUID unload(@NonNull UUID uuid);

    default @NonNull UUID unload(@NonNull OfflinePlayer player) {
        return unload(player.getUniqueId());
    }

    void refresh(@NonNull UUID uuid);

    @NonNull Map<UUID, PlayerData> asMap();

    /**
     * Cleans up all cached player data. This will force the data to be reloaded from data storage.
     */
    void cleanup();

    /**
     * Implementation Requirement: This should save all cached player data to file when being called.
     */
    @Override void close() throws Exception;
}
