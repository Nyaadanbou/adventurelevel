package cc.mewcraft.adventurelevel.listener.data;

import cc.mewcraft.adventurelevel.data.PlayerDataManager;
import cc.mewcraft.adventurelevel.message.PlayerDataMessenger;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.william278.husksync.data.DataSnapshot;
import net.william278.husksync.event.BukkitDataSaveEvent;
import net.william278.husksync.event.BukkitSyncCompleteEvent;
import net.william278.husksync.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * 运行在一个服务器网络里的 {@link UserdataListener}.
 * <p>
 * 如果插件在一个服务器网络里运行, 并有跨服传送与数据同步的场景, 使用 {@link NetworkUserdataListener}.
 */
@Singleton
public class NetworkUserdataListener extends UserdataListener {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("mm:ss.SSS");

    @Inject
    public NetworkUserdataListener(
            final Logger logger,
            final PlayerDataManager playerDataManager,
            final PlayerDataMessenger playerDataMessenger
    ) {
        super(logger, playerDataManager, playerDataMessenger);
    }

    // HuskSync Events 文档:
    // https://william278.net/docs/husksync/api-events

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final BukkitSyncCompleteEvent event) {
        // 该事件发生意味着 HuskSync 已经将玩家数据同步到当前服务器.

        logger.info("[{}] BukkitSyncCompleteEvent is fired, executing listener NetworkUserdataListener#on", LocalDateTime.now().format(FORMATTER));

        final User user = event.getUser();
        final UUID userUuid = user.getUuid();

        loadPlayerData(userUuid);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final BukkitDataSaveEvent event) {
        // 该事件发生意味着 HuskSync 已经将玩家数据保存到数据库.

        final User user = event.getUser();
        final UUID userUuid = user.getUuid();
        final DataSnapshot.SaveCause saveCause = event.getSaveCause();

        if (isSameSaveCause(saveCause, DataSnapshot.SaveCause.DISCONNECT) ||
            isSameSaveCause(saveCause, DataSnapshot.SaveCause.SERVER_SHUTDOWN)
        ) {
            savePlayerData(userUuid);
        }
    }

    private boolean isSameSaveCause(final DataSnapshot.SaveCause cause1, final DataSnapshot.SaveCause cause2) {
        return Objects.equals(cause1.name(), cause2.name());
    }
}