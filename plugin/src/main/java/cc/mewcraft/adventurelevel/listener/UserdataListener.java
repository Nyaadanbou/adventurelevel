package cc.mewcraft.adventurelevel.listener;

import cc.mewcraft.adventurelevel.data.PlayerData;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserdataListener implements Listener {

    private final AdventureLevelPlugin plugin;

    @Inject
    public UserdataListener(final AdventureLevelPlugin plugin) {
        this.plugin = plugin;
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

        PlayerData data = plugin.getPlayerDataManager().load(event.getPlayer());

        // In either case, we need to publish the data to the network, because:

        // Case 1: If the player is switching to another server,
        //   a new data instance can be created in the server that
        //   the player is switching to, without querying database.

        // Case 2: If the player is disconnecting from the network,
        //   the published data will just be garbage-collected
        //   by the JVMs of receiving servers.

        if (data.complete()) {
            plugin.getPlayerDataMessenger().publish(data);
            plugin.getPlayerDataManager().save(data);
        } else {
            plugin.getSLF4JLogger().warn("Possible errors occurred! The player quit the server but their data is marked as not completed - aborting to publish data to the network");
        }

        // We don't invalidate the data entry from cache
        // as the cache loader will evict it automatically.
        // Not removing the cache immediately after the player quit
        // may also help reduce the potential database traffic.
    }
}
