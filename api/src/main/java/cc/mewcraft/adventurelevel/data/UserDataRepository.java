package cc.mewcraft.adventurelevel.data;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserDataRepository {

    /**
     * 从缓存获取玩家数据.
     * <p>
     * 如果玩家数据未缓存或未加载完成, 则返回 {@code null}.
     *
     * @param uuid 玩家的 UUID
     * @return 玩家数据
     * @since 1.5.0
     */
    @Nullable UserData getCached(@NonNull UUID uuid);

    /**
     * 从缓存获取玩家数据.
     * <p>
     * 如果玩家数据未缓存或未加载完成, 则返回的 {@link CompletableFuture} 将在数据加载完毕后完成.
     *
     * @param uuid 玩家的 UUID
     * @return 玩家数据
     * @since 1.5.0
     */
    @NonNull CompletableFuture<UserData> loadAsync(@NonNull UUID uuid);

}
