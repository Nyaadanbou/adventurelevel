package cc.mewcraft.adventurelevel.listener.data;

import cc.mewcraft.adventurelevel.data.SimpleUserData;
import cc.mewcraft.adventurelevel.data.UserDataManager;
import cc.mewcraft.adventurelevel.message.UserDataMessenger;
import cc.mewcraft.adventurelevel.util.PlayerUtils;
import org.bukkit.event.Listener;
import org.slf4j.Logger;

import java.util.UUID;

/**
 * 负责创建和清理 {@link cc.mewcraft.adventurelevel.data.SimpleUserData} 实例.
 */
public abstract class UserdataListener implements Listener {

    protected final Logger logger;
    private final UserDataManager userDataManager;
    private final UserDataMessenger userDataMessenger;

    protected UserdataListener(
            final Logger logger,
            final UserDataManager userDataManager,
            final UserDataMessenger userDataMessenger
    ) {
        this.logger = logger;
        this.userDataManager = userDataManager;
        this.userDataMessenger = userDataMessenger;
    }

    /**
     * 加载玩家数据.
     *
     * @param playerUuid 玩家的 UUID
     */
    protected void loadUserData(final UUID playerUuid) {
        userDataManager.loadAsync(playerUuid);
    }

    /**
     * 保存玩家数据.
     *
     * @param playerUuid 玩家的 UUID
     */
    protected void saveUserData(final UUID playerUuid) {
        final SimpleUserData data = userDataManager.getCached0(playerUuid);

        if (data == null) {
            logger.warn("[{}] Attempted to save data but the data is not cached", PlayerUtils.getName(playerUuid));
            return;
        }

        if (data.isPopulated()) {
            userDataMessenger.publish(data);
            userDataManager.unload(playerUuid);
            userDataManager.save(data);
        } else {
            logger.warn("[{}] Possible errors! The player quit the server but the data is not populated - aborting to sync", PlayerUtils.getName(playerUuid));
        }
    }
}
