package cc.mewcraft.adventurelevel.listener.data;

import cc.mewcraft.adventurelevel.data.PlayerDataManager;
import cc.mewcraft.adventurelevel.message.PlayerDataMessenger;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;

/**
 * 运行在一个单独服务器里的 {@link UserdataListener}.
 * <p>
 * 如果插件只在一个服务器上运行, 并没有跨服传送与数据同步的场景, 使用这个类.
 */
@Singleton
public class SimpleUserdataListener extends UserdataListener {

    @Inject
    public SimpleUserdataListener(
            final Logger logger,
            final PlayerDataManager playerDataManager,
            final PlayerDataMessenger playerDataMessenger
    ) {
        super(logger, playerDataManager, playerDataMessenger);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        loadPlayerData(player);
    }

    @EventHandler(priority = EventPriority.LOWEST) // use the lowest priority, so we handle it as soon as possible
    public void on(final PlayerQuitEvent event) {
        // Player quit the server, which means the player either:
        // - disconnecting from the network completely, or
        // - switching to another server in the network
        //
        // In either case, we need to publish the data to the network, because:
        //
        // Case 1: If the player is switching to another server,
        //   a new data instance can be created in the server that
        //   the player is switching to, without querying database.
        //
        // Case 2: If the player is disconnecting from the network,
        //   the published data will just be garbage-collected
        //   by the JVMs of receiving servers.
        //
        // We don't invalidate the data entry from cache
        // as the cache loader will evict it automatically.
        // Not removing the cache immediately after the player quit
        // may also help reduce the potential database traffic.

        final Player player = event.getPlayer();

        savePlayerData(player);
    }
}
