package cc.mewcraft.adventurelevel.file;

import cc.mewcraft.adventurelevel.data.PlayerData;
import me.lucko.helper.terminable.Terminable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

/**
 * This class provides methods to create/load/save data to/from datasource.
 * <p>
 * All methods of this interface are not thread-safe and may block the calling thread. Therefore, concurrency should be
 * used on the caller's end where possible.
 */
public interface DataStorage extends Terminable {

    /**
     * Creates the specific PlayerData synchronously.
     *
     * @param uuid the uuid of PlayerData
     * @return a PlayerData wrapped by Promise
     */
    @NonNull PlayerData create(UUID uuid);

    /**
     * Loads the specific PlayerData synchronously.
     *
     * @param uuid the uuid of PlayerData
     * @return a PlayerData wrapped by Promise
     */
    @NonNull PlayerData load(UUID uuid);

    /**
     * Saves the given PlayerData synchronously.
     *
     * @param playerData the playerData to save
     */
    void save(PlayerData playerData);

    /**
     * Initialise this data storage.
     */
    void init();

    /**
     * Close this data storage.
     * <p>
     * Implementation Notes: this should save all online player data before return.
     */
    void close();

}
