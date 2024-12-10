package cc.mewcraft.adventurelevel.listener.data;

import cc.mewcraft.adventurelevel.data.UserDataManager;
import cc.mewcraft.adventurelevel.message.UserDataMessenger;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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
            final UserDataManager userDataManager,
            final UserDataMessenger userDataMessenger
    ) {
        super(logger, userDataManager, userDataMessenger);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(final PlayerJoinEvent event) {
        loadUserData(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST) // use the lowest priority, so we handle it as soon as possible
    public void on(final PlayerQuitEvent event) {
        // Player quit the server, which means the player either:
        // - fully disconnecting from the network, or
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

        saveUserData(event.getPlayer().getUniqueId());
    }
}
