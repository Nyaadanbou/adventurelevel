package cc.mewcraft.adventurelevel.event;

import cc.mewcraft.adventurelevel.data.UserData;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * 当玩家的数据加载完成时触发.
 */
public class AdventureLevelDataLoadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UserData userData;

    public AdventureLevelDataLoadEvent(UserData userData) {
        super(!Bukkit.isPrimaryThread());
        this.userData = userData;
    }

    /**
     * 返回加载的玩家数据. 该数据确保已经加载完成.
     *
     * @return 加载的玩家数据
     */
    public UserData getUserData() {
        return userData;
    }

    @Override public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NonNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
