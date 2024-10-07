package cc.mewcraft.adventurelevel.event;

import cc.mewcraft.adventurelevel.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * 当玩家的数据加载完成时触发.
 */
public class AdventureLevelDataLoadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final PlayerData playerData;

    public AdventureLevelDataLoadEvent(PlayerData playerData) {
        super(!Bukkit.isPrimaryThread());
        this.playerData = playerData;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    @Override public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NonNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
