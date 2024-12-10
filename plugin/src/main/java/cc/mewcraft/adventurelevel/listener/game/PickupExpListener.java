package cc.mewcraft.adventurelevel.listener.game;

import cc.mewcraft.adventurelevel.data.SimpleUserData;
import cc.mewcraft.adventurelevel.data.UserDataManager;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import cc.mewcraft.adventurelevel.util.LevelCategoryUtils;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.slf4j.Logger;

import static org.bukkit.event.EventPriority.HIGH;

/**
 * This listener is the entry point of our level system.
 */
@Singleton
public class PickupExpListener implements Listener {

    private final Logger logger;
    private final UserDataManager userDataManager;

    @Inject
    public PickupExpListener(
            final Logger logger,
            final UserDataManager userDataManager
    ) {
        this.logger = logger;
        this.userDataManager = userDataManager;
    }

    @EventHandler(priority = HIGH, ignoreCancelled = true)
    public void onPickupExp(PlayerPickupExperienceEvent event) {
        SimpleUserData cachedData = userDataManager.getCached0(event.getPlayer());
        if (cachedData == null || !cachedData.isPopulated()) {
            // Load data asynchronously if not cached
            userDataManager.loadAsync0(event.getPlayer());

            // Cancel event if data is not ready yet.
            // This avoids potential experience loss.
            logger.warn("[{}] User data is not cached, canceling PlayerPickupExperienceEvent", event.getPlayer().getName());
            event.setCancelled(true);
            return;
        }

        // Handle primary level
        cachedData.getLevel(LevelCategory.PRIMARY).handleEvent(event);

        // Handle other levels
        LevelCategory levelCategory = LevelCategoryUtils.get(event.getExperienceOrb().getSpawnReason());
        if (levelCategory != null) {
            cachedData.getLevel(levelCategory).handleEvent(event);
        }
    }
}
