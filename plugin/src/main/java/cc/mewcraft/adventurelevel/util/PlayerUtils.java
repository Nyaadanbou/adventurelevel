package cc.mewcraft.adventurelevel.util;

import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public final class PlayerUtils {

    public static @NonNull String getPrettyString(UUID uuid) {
        return getName(uuid) + "{uuid=" + uuid + "}";
    }

    public static @NonNull String getName(UUID uuid) {
        return Players.getOffline(uuid).map(OfflinePlayer::getName).orElse("null");
    }

    public static @Nullable String getNameOrNull(UUID uuid) {
        return Players.getOffline(uuid).map(OfflinePlayer::getName).orElse(null);
    }

    private PlayerUtils() {
        throw new UnsupportedOperationException("this class cannot be instantiated.");
    }

}
