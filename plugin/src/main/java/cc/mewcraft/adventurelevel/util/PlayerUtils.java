package cc.mewcraft.adventurelevel.util;

import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public final class PlayerUtils {

    public static @NonNull String getReadableString(UUID uuid) {
        return getNameFromUUID(uuid) + "{uuid=" + uuid + "}";
    }

    public static @NonNull String getNameFromUUID(UUID uuid) {
        return Players.getOffline(uuid).map(OfflinePlayer::getName).orElse("null");
    }

    public static @Nullable String getNameFromUUIDNullable(UUID uuid) {
        return Players.getOffline(uuid).map(OfflinePlayer::getName).orElse(null);
    }

    private PlayerUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

}
