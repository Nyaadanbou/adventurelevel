package cc.mewcraft.adventurelevel.listener;

import cc.mewcraft.adventurelevel.data.PlayerData;
import cc.mewcraft.adventurelevel.data.PlayerDataManager;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static org.bukkit.event.EventPriority.HIGH;

/**
 * This listener is the entry point of our level system.
 */
@Singleton
public class PickupExpListener implements Listener {

    private final PlayerDataManager playerDataManager;

    @Inject
    public PickupExpListener(final PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler(priority = HIGH, ignoreCancelled = true)
    public void onPickupExp(PlayerPickupExperienceEvent event) {
        PlayerData data = playerDataManager.load(event.getPlayer());
        if (!data.complete()) {
            // Cancel event if data is not completed.
            // This avoids potential experience loss.
            event.setCancelled(true);
            return;
        }

        // Handle main level
        data.getLevel(LevelCategory.MAIN).handleEvent(event);

        // Handle other levels
        LevelCategory levelCategory = LevelCategory.toLevelCategory(event.getExperienceOrb().getSpawnReason());
        if (levelCategory != null) {
            data.getLevel(levelCategory).handleEvent(event);
        }
    }
}
