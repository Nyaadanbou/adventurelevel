package cc.mewcraft.adventurelevel.plugin;

import cc.mewcraft.adventurelevel.data.PlayerDataManager;

import org.jetbrains.annotations.NotNull;

public interface AdventureLevel {

    @NotNull PlayerDataManager getPlayerDataManager();

}
