package cc.mewcraft.adventurelevel.data;

import cc.mewcraft.adventurelevel.file.DataStorage;
import cc.mewcraft.adventurelevel.message.PlayerDataMessenger;
import cc.mewcraft.adventurelevel.message.packet.PlayerDataPacket;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import com.google.common.base.Predicates;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.lucko.helper.Schedulers;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.terminable.composite.CompositeTerminable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
public class PlayerDataManagerImpl implements PlayerDataManager {
    private final AdventureLevelPlugin plugin;
    private final Logger logger;

    private final DataStorage storage;
    private final PlayerDataMessenger messenger;

    // 我们通过监听 PlayerQuitEvent 来移除无用的数据,
    // 不使用 expireAfterAccess / expireAfterWrite.
    private final LoadingCache<UUID, PlayerData> cache = CacheBuilder.newBuilder()
            .removalListener(new PlayerDataRemovalListener())
            .build(new PlayerDataLoader());

    // 记录所有运行中的异步任务
    private final CompositeTerminable tasks = CompositeTerminable.create();

    // 延迟多久后从消息中获取数据
    private final long networkLatencyMilliseconds;

    private class PlayerDataLoader extends CacheLoader<UUID, PlayerData> {
        @Override public @NonNull PlayerData load(
                final @NonNull UUID key
        ) {
            RealPlayerData data = new RealPlayerData(plugin, key);
            logger.info("Created userdata in cache: {}", data.toSimpleString());
            Schedulers.builder()
                    .async()
                    .after(networkLatencyMilliseconds, TimeUnit.MILLISECONDS)
                    .run(() -> {
                        if (data.complete()) {
                            return; // It is already complete - do nothing
                        }
                        // Get data from message store first
                        PlayerDataPacket message = messenger.get(key);
                        if (message != null) {
                            PlayerDataUpdater.update(data, message).markAsComplete();
                        } else {
                            // The message store does not have the data,
                            // so load the data from file (or database).
                            PlayerData fromFile = storage.load(key);
                            if (fromFile.equals(PlayerData.DUMMY)) {
                                fromFile = storage.create(key); // Not existing in disk - create one
                            }
                            PlayerDataUpdater.update(data, fromFile).markAsComplete();
                        }
                        logger.info("Loaded userdata into cache: {}", data.toSimpleString());
                    })
                    .bindWith(tasks);
            return data;
        }
    }

    /**
     * Methods define the callback when an entry is removed from the cache.
     */
    private class PlayerDataRemovalListener implements RemovalListener<UUID, PlayerData> {
        @Override public void onRemoval(final RemovalNotification<UUID, PlayerData> notification) {
            PlayerData data = Objects.requireNonNull(notification.getValue(), "data");
            logger.info("Unloaded userdata from cache: {}", data.toSimpleString());
        }
    }

    @Inject
    public PlayerDataManagerImpl(
            final AdventureLevelPlugin plugin,
            final Logger logger,
            final DataStorage storage,
            final PlayerDataMessenger messenger
    ) {
        this.plugin = plugin;
        this.logger = logger;
        this.storage = storage;
        this.messenger = messenger;
        this.networkLatencyMilliseconds = Math.max(0, plugin.getConfig().getLong("synchronization.network_latency_milliseconds"));
    }

    @Override public @NonNull Map<UUID, PlayerData> asMap() {
        return cache.asMap();
    }

    @Override public @NonNull PlayerData load(final @NonNull UUID uuid) {
        return cache.getUnchecked(uuid);
    }

    @Override public @NonNull Promise<PlayerData> save(final @NonNull PlayerData playerData) {
        return !playerData.complete()
                ? Promise.supplyingExceptionallyAsync(() -> playerData)
                : Promise.supplyingAsync(() -> {
                    storage.save(playerData);
                    return playerData;
                });
    }

    @Override public @NonNull UUID unload(final @NonNull UUID uuid) {
        cache.invalidate(uuid);
        return uuid;
    }

    @Override public void refresh(final @NonNull UUID uuid) {
        cache.refresh(uuid);
    }

    @Override public void cleanup() {
        // We need to save all ONLINE players data before shutdown.
        // Doing so we can safely and completely reload the plugin.
        cache.asMap().values()
                .stream()
                .filter(Predicates.and(
                        PlayerData::complete,
                        PlayerData::isOnline
                ))
                .forEach(storage::save);

        // Invalidate all cached data
        cache.invalidateAll();
    }

    @Override public void close() throws Exception {
        // Clean up cached data
        cleanup();

        // Shutdown all async tasks of loading player data
        tasks.close();
    }
}
