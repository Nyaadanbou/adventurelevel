package cc.mewcraft.adventurelevel.file;

import cc.mewcraft.adventurelevel.data.SimpleUserData;
import me.lucko.helper.terminable.Terminable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

/**
 * This class provides methods to create/load/save data to/from datasource.
 * <p>
 * All methods of this interface are not thread-safe and may block the calling thread. Therefore, concurrency should be
 * used on the caller's end where possible.
 */
public interface UserDataStorage extends Terminable {

    /**
     * Creates the specific {@link SimpleUserData}.
     *
     * @param uuid the uuid of {@link SimpleUserData}
     * @return a new instance of {@link SimpleUserData}
     * @throws IllegalStateException if failed to create {@link SimpleUserData} in data storage
     */
    @NonNull SimpleUserData create(@NonNull UUID uuid);

    /**
     * Loads the specific {@link SimpleUserData}.
     *
     * @param uuid the uuid of {@link SimpleUserData}
     * @return a new instance of {@link SimpleUserData}
     */
    @Nullable SimpleUserData load(@NonNull UUID uuid);

    /**
     * Saves the given {@link SimpleUserData}.
     *
     * @param data the {@link SimpleUserData} to save
     */
    void save(@NonNull SimpleUserData data);

    /**
     * Initialise this data storage.
     */
    void init();

    /**
     * Close this data storage.
     */
    // Implementation Notes: this should save all online user data before return.
    void close();

}
