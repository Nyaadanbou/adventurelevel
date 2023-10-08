package cc.mewcraft.adventurelevel.util;

import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerUtils {

    public static @NotNull String getNameFromUUID(UUID uuid) {
        return Players.getOffline(uuid).map(OfflinePlayer::getName).orElse("null");
    }

    public static @Nullable String getNameFromUUIDNullable(UUID uuid) {
        return Players.getOffline(uuid).map(OfflinePlayer::getName).orElse(null);
    }

    private PlayerUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

}
