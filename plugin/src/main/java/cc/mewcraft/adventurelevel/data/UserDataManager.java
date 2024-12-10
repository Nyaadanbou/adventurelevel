package cc.mewcraft.adventurelevel.data;

import cc.mewcraft.adventurelevel.event.AdventureLevelDataLoadEvent;
import cc.mewcraft.adventurelevel.file.UserDataStorage;
import cc.mewcraft.adventurelevel.message.UserDataMessenger;
import cc.mewcraft.adventurelevel.message.packet.UserDataPacket;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import cc.mewcraft.adventurelevel.util.PlayerUtils;
import cc.mewcraft.adventurelevel.util.UserDataUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.composite.CompositeTerminable;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Singleton
public class UserDataManager implements Terminable, UserDataRepository {
    private final Logger logger;

    private final UserDataStorage storage;
    private final UserDataMessenger messenger;

    // 我们通过监听 PlayerQuitEvent 来移除无用的数据,
    // 不使用 expireAfterAccess / expireAfterWrite.
    private final LoadingCache<UUID, SimpleUserData> cache = CacheBuilder.newBuilder()
            .removalListener(new UserDataRemovalListener())
            .build(new UserDataLoader());

    // 记录所有运行中的异步任务
    private final CompositeTerminable tasks = CompositeTerminable.create();

    @Inject
    public UserDataManager(
            final Logger logger,
            final UserDataStorage storage,
            final UserDataMessenger messenger
    ) {
        this.logger = logger;
        this.storage = storage;
        this.messenger = messenger;
    }

    /**
     * 加载并返回玩家数据. 如果玩家数据已经缓存, 则返回缓存的数据.
     *
     * @param uuid 玩家的 UUID
     * @return 玩家数据
     */
    public @NonNull SimpleUserData loadSync(final @NonNull UUID uuid) {
        final SimpleUserData data = cache.getUnchecked(uuid);
        new AdventureLevelDataLoadEvent(data).callEvent(); // 触发事件, 通知其他系统
        return data;
    }

    /**
     * 加载并返回玩家数据. 如果玩家数据已经缓存, 则返回缓存的数据.
     *
     * @param player 玩家
     * @return 玩家数据
     */
    public @NonNull SimpleUserData loadSync(final @NonNull OfflinePlayer player) {
        return loadSync(player.getUniqueId());
    }

    /**
     * 保存玩家数据到数据库.
     *
     * @param userData 玩家数据
     * @return 保存后的玩家数据
     */
    public @NonNull CompletableFuture<SimpleUserData> save(final @NonNull SimpleUserData userData) {
        if (!userData.isPopulated()) {
            return CompletableFuture.failedFuture(new IllegalStateException("user data is not populated"));
        }
        return CompletableFuture.supplyAsync(() -> {
            storage.save(userData);
            return userData;
        });
    }

    /**
     * 从缓存中卸载玩家数据.
     *
     * @param uuid 玩家的 UUID
     * @return 卸载后的玩家数据的 UUID
     * @since 1.4.0
     */
    public @NonNull UUID unload(final @NonNull UUID uuid) {
        cache.invalidate(uuid);
        return uuid;
    }

    /**
     * 从缓存中卸载玩家数据.
     *
     * @param player 玩家
     * @return 卸载后的玩家数据的 UUID
     * @since 1.4.0
     */
    public @NonNull UUID unload(final @NonNull OfflinePlayer player) {
        return unload(player.getUniqueId());
    }

    /**
     * Cleans up all cached player data. This will force the data to be reloaded from data storage.
     */
    public void cleanup() {
        // We need to save all ONLINE players data before shutdown.
        // Doing so we can safely and completely reload the plugin.
        cache.asMap().values()
                .stream()
                .filter(t -> t.isPopulated() && UserDataUtils.isConnected(t))
                .forEach(storage::save);

        // Invalidate all cached data
        cache.invalidateAll();
    }

    /**
     * This should save all cached player data to file when being called.
     */
    @Override public void close() throws Exception {
        // Clean up cached data
        cleanup();

        // Shutdown all async tasks of loading player data
        tasks.close();
    }

    /**
     * 本函数与 {@link #getCached(UUID)} 的区别在于返回值类型.
     *
     * @see #getCached(UUID)
     */
    public @Nullable SimpleUserData getCached0(@NonNull final UUID uuid) {
        SimpleUserData cached = cache.getIfPresent(uuid);
        if (cached != null && cached.isPopulated()) {
            return cached;
        }
        return null;
    }

    public @Nullable SimpleUserData getCached0(final @NonNull OfflinePlayer player) {
        return getCached0(player.getUniqueId());
    }

    /**
     * 本函数与 {@link #loadAsync(UUID)} 的区别在于返回值类型.
     *
     * @see #loadAsync(UUID)
     */
    public @NonNull CompletableFuture<SimpleUserData> loadAsync0(@NonNull final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> loadSync(uuid), AdventureLevelPlugin.instance().getVirtualExecutor());
    }

    public @NonNull CompletableFuture<SimpleUserData> loadAsync0(final @NonNull OfflinePlayer player) {
        return loadAsync0(player.getUniqueId());
    }

    @Override public @Nullable UserData getCached(@NonNull final UUID uuid) {
        return getCached0(uuid);
    }

    @Override public @NonNull CompletableFuture<UserData> loadAsync(@NonNull final UUID uuid) {
        return loadAsync0(uuid).thenApply(Function.identity());
    }


    // CacheLoader & RemovalListener


    private class UserDataLoader extends CacheLoader<UUID, SimpleUserData> {
        @Override public @NonNull SimpleUserData load(
                final @NonNull UUID key
        ) {
            final SimpleUserData data = new SimpleUserData(key);
            logger.info("[{}] Created empty userdata in cache: {}", PlayerUtils.getName(key), data);

            // Get data from message store first
            final UserDataPacket message = messenger.get(key);
            if (message != null) {
                UserDataUpdater.update(data, message).setPopulated(true);
            }
            // The message store does not have the data,
            // so load the data from file (or database).
            else {
                SimpleUserData data2 = storage.load(key);
                if (data2 == null) {
                    data2 = storage.create(key); // Not existing in data storage - create one
                }
                UserDataUpdater.update(data, data2).setPopulated(true);
            }

            logger.info("[{}] Populated empty userdata in cache: {}", PlayerUtils.getName(key), data);

            return data;
        }
    }

    /**
     * Methods define the callback when an entry is removed from the cache.
     */
    private class UserDataRemovalListener implements RemovalListener<UUID, SimpleUserData> {
        @Override public void onRemoval(final RemovalNotification<UUID, SimpleUserData> notification) {
            final UUID key = Objects.requireNonNull(notification.getKey(), "key");
            final SimpleUserData data = Objects.requireNonNull(notification.getValue(), "data");
            logger.info("[{}] Removed userdata from cache: {}", PlayerUtils.getName(key), data);
        }
    }
}
