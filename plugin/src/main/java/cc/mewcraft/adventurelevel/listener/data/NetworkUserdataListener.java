package cc.mewcraft.adventurelevel.listener.data;

import cc.mewcraft.adventurelevel.data.UserDataManager;
import cc.mewcraft.adventurelevel.message.UserDataMessenger;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.william278.husksync.event.BukkitSyncCompleteEvent;
import net.william278.husksync.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * 运行在一个服务器网络里的 {@link UserdataListener}.
 * <p>
 * 如果插件在一个服务器网络里运行, 并有跨服传送与数据同步的场景, 使用 {@link NetworkUserdataListener}.
 */
@Singleton
public class NetworkUserdataListener extends UserdataListener {

    @Inject
    public NetworkUserdataListener(
            final Logger logger,
            final UserDataManager userDataManager,
            final UserDataMessenger userDataMessenger
    ) {
        super(logger, userDataManager, userDataMessenger);
    }

    // HuskSync Events 文档:
    // https://william278.net/docs/husksync/api-events

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final BukkitSyncCompleteEvent event) {
        // 该事件发生意味着 HuskSync 已经将玩家数据同步到当前服务器.
        final User user = event.getUser();
        final UUID userUuid = user.getUuid();
        loadUserData(userUuid);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final PlayerQuitEvent event) {
        // 冒险等级应该尽可能早的保存, 这样可以尽早的同步到服务器网络.
        saveUserData(event.getPlayer().getUniqueId());
    }

    /*@EventHandler(priority = EventPriority.LOWEST)
    public void on(final BukkitDataSaveEvent event) {
        // 该事件发生意味着 HuskSync 已经将玩家数据保存到数据库.

        final User user = event.getUser();
        final UUID userUuid = user.getUuid();
        final DataSnapshot.SaveCause saveCause = event.getSaveCause();
        if (isSameSaveCause(saveCause, DataSnapshot.SaveCause.DISCONNECT) ||
            isSameSaveCause(saveCause, DataSnapshot.SaveCause.SERVER_SHUTDOWN)
        ) {
            saveUserData(userUuid);
        }
    }

    private boolean isSameSaveCause(final DataSnapshot.SaveCause cause1, final DataSnapshot.SaveCause cause2) {
        return Objects.equals(cause1.name(), cause2.name());
    }*/
}