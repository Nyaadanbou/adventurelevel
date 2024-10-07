package cc.mewcraft.adventurelevel.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * 当玩家的冒险等级发生变化时触发.
 */
public class AdventureLevelExpChangeEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;
    private final Action action;
    private final int previousExp;
    private int changingExp;

    public AdventureLevelExpChangeEvent(
            final @NonNull Player who,
            final Action action,
            final int previousExp,
            final int changingExp
    ) {
        super(who);
        this.action = action;
        this.previousExp = previousExp;
        this.changingExp = changingExp;
    }

    /**
     * 获取等级变化前的经验值.
     *
     * @return 玩家冒险等级变化前的经验值
     */
    public int getPreviousExp() {
        return previousExp;
    }

    /**
     * 获取本次等级变化的数值. 数值的含义请参考 {@link #getAction()}.
     *
     * @return 本次等级变化的数值
     */
    public int getChangingExp() {
        return changingExp;
    }

    /**
     * 设置本次等级变化的数值. 数值的含义请参考 {@link #getAction()}.
     *
     * @param changingExp 玩家冒险等级变化后的经验值
     */
    public void setChangingExp(final int changingExp) {
        this.changingExp = changingExp;
    }

    /**
     * 获取等级变化的类型.
     *
     * @return 等级变化的类型
     */
    public AdventureLevelExpChangeEvent.@NonNull Action getAction() {
        return action;
    }

    @Override public boolean isCancelled() {
        return cancelled;
    }

    @Override public void setCancelled(final boolean cancel) {
        cancelled = cancel;
    }

    @Override public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NonNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * 等级变化的类型.
     */
    public enum Action {
        /**
         * 在当前经验值的基础之上, 增加或减少定量的经验值.
         */
        OFFSET,
        /**
         * 无视已有的经验值, 直接将其覆盖为指定的数值.
         */
        OVERWRITE,
    }
}
