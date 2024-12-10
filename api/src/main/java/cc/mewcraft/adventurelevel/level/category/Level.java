package cc.mewcraft.adventurelevel.level.category;

import cc.mewcraft.adventurelevel.level.modifier.ExperienceModifier;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import net.kyori.examination.Examinable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

/**
 * 等级的数量只是一个计算的结果, 它是从经验值 {@link #calculateTotalLevel(int)} 计算而来的.
 */
public interface Level extends Examinable {

    /**
     * 处理玩家捡起经验球的事件.
     *
     * @param event 玩家捡起经验球的事件
     */
    void handleEvent(PlayerPickupExperienceEvent event);

    /**
     * 计算达到给定等级所需的经验值.
     *
     * @param level 指定的等级
     * @return 达到给定等级所需的经验值
     */
    int calculateTotalExperience(int level);

    /**
     * 计算从当前等级到下一级所需的经验值.
     *
     * @param currentLevel 当前等级
     * @return 从当前等级到下一级所需的经验值
     */
    int calculateNeededExperience(int currentLevel);

    /**
     * 计算给定的经验值 {@code totalExp} 对应的等级数量.
     * <p>
     * 返回的等级数量是一个 {@code double}, 小数部分是到下一级的进度. 其中 {@code 0} 表示零进度, {@code 1} 表示满进度.
     * <p>
     * 注意: 这是 {@link #calculateTotalExperience(int)} 的逆运算.
     *
     * @param totalExp 总经验值
     * @return 给定的经验值对应的等级数量
     */
    double calculateTotalLevel(int totalExp);

    /**
     * 返回当前经验值.
     *
     * @return 当前的经验值
     */
    int getExperience();

    /**
     * 设置经验值.
     *
     * @param value 新的经验值
     * @return 旧的经验值
     */
    int setExperience(int value);

    /**
     * 增加经验值.
     *
     * @param value 增加的经验值
     * @return 旧的经验值
     */
    int addExperience(int value);

    /**
     * 获取指定类型的经验修饰符.
     *
     * @return 获得经验值时应用的修饰符
     */
    @NonNull Map<String, ExperienceModifier> getExperienceModifiers(ExperienceModifier.Type type);

    /**
     * 添加指定的经验修饰符.
     *
     * @param key 修饰符的键
     * @param modifier 获得经验值时应用的修饰符
     */
    void addExperienceModifier(String key, ExperienceModifier modifier, ExperienceModifier.Type type);

    /**
     * 移除指定的经验修饰符.
     *
     * @param key 要移除的修饰符的键
     */
    void removeExperienceModifier(String key, ExperienceModifier.Type type);

    /**
     * 清除所有经验修饰符.
     */
    void clearExperienceModifiers();

    /**
     * 返回等级数量.
     *
     * @return 当前的等级数量
     */
    int getLevel();

    /**
     * 设置等级数量.
     *
     * @param level 新的等级数量
     * @return 旧的等级数量
     */
    int setLevel(int level);

    /**
     * 增加等级数量.
     *
     * @param level 要增加的等级数量
     * @return 旧的等级数量
     */
    int addLevel(int level);

    /**
     * 返回最大等级数量.
     *
     * @return 最大等级数量
     */
    int getMaxLevel();

    /**
     * 设置经验值.
     *
     * @param value 新的经验值
     * @return 此实例
     */
    @SuppressWarnings("unchecked")
    default <T extends Level> T withExperience(int value) {
        this.setExperience(value);
        return (T) this;
    }

    /**
     * 设置等级数量.
     *
     * @param level 新的等级数量
     * @return 此实例
     */
    @SuppressWarnings("unchecked")
    default <T extends Level> T withLevel(int level) {
        this.setLevel(level);
        return (T) this;
    }
}
