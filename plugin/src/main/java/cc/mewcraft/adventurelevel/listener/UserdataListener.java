package cc.mewcraft.adventurelevel.listener;

import cc.mewcraft.adventurelevel.data.PlayerDataManager;
import cc.mewcraft.adventurelevel.message.PlayerDataMessenger;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.william278.husksync.event.BukkitDataSaveEvent;
import net.william278.husksync.event.BukkitSyncCompleteEvent;
import net.william278.husksync.user.BukkitUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.slf4j.Logger;

@Singleton
public class UserdataListener implements Listener {

    private final Logger logger;
    private final PlayerDataManager playerDataManager;
    private final PlayerDataMessenger playerDataMessenger;

    @Inject
    public UserdataListener(
            final Logger logger,
            final PlayerDataManager playerDataManager,
            final PlayerDataMessenger playerDataMessenger
    ) {
        this.logger = logger;
        this.playerDataManager = playerDataManager;
        this.playerDataMessenger = playerDataMessenger;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSyncComplete(BukkitSyncCompleteEvent event) {
        BukkitUser user = (BukkitUser) event.getUser();
        playerDataManager.load(user.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST) // use the lowest priority, so we handle it as soon as possible
    public void onDataSave(BukkitDataSaveEvent event) {
        BukkitUser user = (BukkitUser) event.getUser();
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

        final var player = user.getPlayer();

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
