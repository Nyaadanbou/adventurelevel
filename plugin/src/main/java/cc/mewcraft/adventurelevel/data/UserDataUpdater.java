package cc.mewcraft.adventurelevel.data;

import cc.mewcraft.adventurelevel.level.LevelFactory;
import cc.mewcraft.adventurelevel.level.category.Level;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import cc.mewcraft.adventurelevel.message.packet.UserDataPacket;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * 给定一个 {@link SimpleUserData} 的实例和 <b>数据源</b>, 你可以使用本工具类将 {@link SimpleUserData} 的状态更新为 <b>数据源</b> 的状态.
 * <p>
 * 目前数据源可以是以下几种类型:
 * <ul>
 *     <li>{@link UserDataPacket} - 从网络发送的数据</li>
 *     <li>{@link SimpleUserData} - 从磁盘加载的数据</li>
 * </ul>
 */
public final class UserDataUpdater {

    /**
     * 从给定的 {@link UserDataPacket} 更新特定 {@link SimpleUserData} 的状态.
     *
     * @param data   要更新的数据 - 这将是返回值
     * @param source 从网络发送的数据, 从中复制状态
     * @return 更新后的 {@code data} (引用保持不变)
     */
    public static @NonNull SimpleUserData update(final @NonNull SimpleUserData data, final @NonNull UserDataPacket source) {
        for (final LevelCategory category : LevelCategory.values()) {
            Level level = data.asMap().computeIfAbsent(category, LevelFactory::newLevel);
            level.setExperience(source.getExpByCategory(category));
        }

        return data; // 引用保持不变
    }

    /**
     * 从另一个 {@link SimpleUserData} 更新特定 {@link SimpleUserData} 的状态.
     *
     * @param data   要更新的数据 - 这将是返回值
     * @param source 从磁盘加载的数据, 从中复制状态
     * @return 更新后的 {@code data} (引用保持不变)
     */
    public static @NonNull SimpleUserData update(final @NonNull SimpleUserData data, final @NonNull SimpleUserData source) {
        for (final LevelCategory category : LevelCategory.values()) {
            Level level = data.asMap().computeIfAbsent(category, LevelFactory::newLevel);
            level.setExperience(source.getLevel(category).getExperience());
        }

        return data; // 引用保持不变
    }

    private UserDataUpdater() {
        throw new UnsupportedOperationException("this class cannot be instantiated");
    }

}