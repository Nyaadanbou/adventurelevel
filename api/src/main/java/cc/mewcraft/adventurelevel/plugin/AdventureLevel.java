package cc.mewcraft.adventurelevel.plugin;

import cc.mewcraft.adventurelevel.data.PlayerDataManager;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface AdventureLevel {

    @NonNull PlayerDataManager getPlayerDataManager();

}
