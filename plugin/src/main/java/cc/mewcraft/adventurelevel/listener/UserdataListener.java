package cc.mewcraft.adventurelevel.listener;

import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;

@Singleton
public class UserdataListener implements Listener {

    private final AdventureLevelPlugin plugin;
    private final Logger logger;

    @Inject
    public UserdataListener(
            final AdventureLevelPlugin plugin,
            final Logger logger
    ) {
        this.plugin = plugin;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLogin(PlayerJoinEvent event) {
        plugin.getPlayerDataManager().load(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST) // use the lowest priority, so we handle it as soon as possible
    public void onQuit(PlayerQuitEvent event) {
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

        final var player = event.getPlayer();
        final var playerDataManager = plugin.getPlayerDataManager();
        final var playerDataMessenger = plugin.getPlayerDataMessenger();

        final var data = playerDataManager.load(player);
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
