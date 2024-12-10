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
        // 这里直接调用了 loadSync 是因为插件重载会让缓存失效.
        // 使用 loadSync 可以在缓存失效时也能重新加载得到数据 (通过数据库).
        // 除了插件重载的情况下, loadSync 都会直接返回缓存的数据, 不会走数据库.
        SimpleUserData data = userDataManager.loadSync(event.getPlayer());

        if (!data.isPopulated()) {
            // Cancel event if data is not completed.
            // This avoids potential experience loss.
            logger.warn("[{}] Player data is not populated, canceling PlayerPickupExperienceEvent", event.getPlayer().getName());
            event.setCancelled(true);
            return;
        }

        // Handle primary level
        data.getLevel(LevelCategory.PRIMARY).handleEvent(event);

        // Handle other levels
        LevelCategory levelCategory = LevelCategoryUtils.get(event.getExperienceOrb().getSpawnReason());
        if (levelCategory != null) {
            data.getLevel(levelCategory).handleEvent(event);
        }
    }
}
