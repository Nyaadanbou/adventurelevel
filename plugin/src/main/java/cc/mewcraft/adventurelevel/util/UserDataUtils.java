package cc.mewcraft.adventurelevel.util;

import cc.mewcraft.adventurelevel.data.UserData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UserDataUtils {
    public static @Nullable Player getPlayer(UserData userData) {
        return Bukkit.getPlayer(userData.getUuid());
    }

    public static boolean isOnline(UserData userData) {
        return getPlayer(userData) != null;
    }

    public static boolean isConnected(UserData userData) {
        Player player = getPlayer(userData);
        return player != null && player.isConnected();
    }

    private UserDataUtils() {
        throw new UnsupportedOperationException("this class cannot be instantiated");
    }
}
