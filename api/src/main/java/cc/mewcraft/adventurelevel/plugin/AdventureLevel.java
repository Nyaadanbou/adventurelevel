package cc.mewcraft.adventurelevel.plugin;

import cc.mewcraft.adventurelevel.data.UserDataRepository;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface AdventureLevel {

    @NonNull UserDataRepository getUserDataRepository();

}
