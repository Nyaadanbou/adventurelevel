package cc.mewcraft.adventurelevel.listener.data;

import cc.mewcraft.adventurelevel.data.PlayerData;
import cc.mewcraft.adventurelevel.data.PlayerDataManager;
import cc.mewcraft.adventurelevel.message.PlayerDataMessenger;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.slf4j.Logger;

/**
 * 负责创建和清理 {@link PlayerData} 实例.
 */
public abstract class UserdataListener implements Listener {

    private final Logger logger;
    private final PlayerDataManager playerDataManager;
    private final PlayerDataMessenger playerDataMessenger;

    protected UserdataListener(
            final Logger logger,
            final PlayerDataManager playerDataManager,
            final PlayerDataMessenger playerDataMessenger
    ) {
        this.logger = logger;
        this.playerDataManager = playerDataManager;
        this.playerDataMessenger = playerDataMessenger;
    }

    /**
     * 加载玩家数据.
     *
     * @param player 玩家
     */
    protected void loadPlayerData(final Player player) {
        playerDataManager.load(player);
    }

    /**
     * 保存玩家数据.
     *
     * @param player 玩家
     */
    protected void savePlayerData(final Player player) {
        final PlayerData data = playerDataManager.load(player);

        if (data.complete()) {
            playerDataMessenger.publish(data);
            playerDataManager.save(data);
            playerDataManager.unload(player);
        } else {
            logger.warn(
                    "Possible errors! {} quit the server but their data is marked as incomplete - aborting to publish data to the network", player.getName()
            );
        }
    }
}
