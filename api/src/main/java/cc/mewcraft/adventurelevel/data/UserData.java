package cc.mewcraft.adventurelevel.data;

import cc.mewcraft.adventurelevel.level.category.Level;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public interface UserData {

    /**
     * 返回对应的玩家的 UUID.
     */
    @NonNull UUID getUuid();

    /**
     * 返回指定类别的等级.
     *
     * @param category 等级的类别
     * @return 指定类别的等级
     * @throws IllegalArgumentException 如果该类别的等级不存在, 或该数据未加载完成
     */
    @NonNull Level getLevel(LevelCategory category);

}
