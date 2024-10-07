package cc.mewcraft.adventurelevel.message;

import cc.mewcraft.adventurelevel.data.PlayerData;
import cc.mewcraft.adventurelevel.data.PlayerDataManager;
import cc.mewcraft.adventurelevel.data.PlayerDataUpdater;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import cc.mewcraft.adventurelevel.message.packet.PlayerDataPacket;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import cc.mewcraft.nettowaku.ServerInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.lucko.helper.messaging.Channel;
import me.lucko.helper.messaging.ChannelAgent;
import me.lucko.helper.messaging.Messenger;
import me.lucko.helper.terminable.Terminable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * This class provides methods to sync data between servers.
 * <p>
 * Why we have this class? Because SQL database is too slow to save/load data when players switch servers.
 */
@Singleton
public class PlayerDataMessenger implements Terminable {
    private static final String SYNC_CHANNEL = "advtrlvl-sync";

    private final Logger logger;
    private final PlayerDataManager playerDataManager;

    /**
     * A channel to send messages.
     */
    private final Channel<PlayerDataPacket> channel;
    /**
     * An agent created from the channel, used to register listeners.
     */
    private final ChannelAgent<PlayerDataPacket> agent;
    /**
     * A cache that stores the messages received from other servers.
     * <p>
     * The entries should be expired in a very short period of time.
     */
    private final Cache<UUID, PlayerDataPacket> messageStore;

    @Inject
    public PlayerDataMessenger(
            final AdventureLevelPlugin plugin,
            final Logger logger,
            final PlayerDataManager playerDataManager
    ) {
        this.logger = logger;
        this.playerDataManager = playerDataManager;

        long networkLatencyMilliseconds = Math.max(0, plugin.getConfig().getLong("synchronization.network_latency_milliseconds"));
        this.messageStore = CacheBuilder.newBuilder().expireAfterWrite(Duration.of(networkLatencyMilliseconds * 2L, ChronoUnit.MILLIS)).build();

        // Get and define the channel.
        this.channel = plugin.getService(Messenger.class).getChannel(SYNC_CHANNEL, PlayerDataPacket.class);

        // Create an agent for the channel.
        this.agent = this.channel.newAgent();
    }

    public void registerListeners() {
        channel.newAgent((agent, message) -> {
            if (Objects.equals(ServerInfo.SERVER_ID.get(), message.server())) {
                return; // Ignore packets sent from the same server
            }

            UUID uuid = message.uuid();

            // Save data in the message store
            messageStore.put(uuid, message);

            if (playerDataManager.asMap().containsKey(uuid)) {
                PlayerData data = playerDataManager.asMap().get(uuid);
                if (data.complete()) {
                    // Here we only need to update 'complete' entry.

                    // Incomplete means that the entry is newly created (or re-added),
                    // in which case the CacheLoader will handle the data loading.

                    PlayerDataUpdater.update(data, message);
                    logger.info("Update userdata in cache: {}", message.toSimpleString());
                }
            }
        });
    }

    /**
     * This method should be called upon player quitting the server.
     * <p>
     * <b>Caveat:</b> The data should be published only if {@link PlayerData#complete()} returns true.
     * Callers should check {@link PlayerData#complete()} before calling this method.
     *
     * @param data the player data to be sent to the channel
     */
    public void publish(@NonNull PlayerData data) {
        channel.sendMessage(new PlayerDataPacket(
                data.getUuid(),
                ServerInfo.SERVER_ID.get(),
                System.currentTimeMillis(),
                data.getLevel(LevelCategory.PRIMARY).getExperience(),
                data.getLevel(LevelCategory.BLOCK_BREAK).getExperience(),
                data.getLevel(LevelCategory.BREED).getExperience(),
                data.getLevel(LevelCategory.ENTITY_DEATH).getExperience(),
                data.getLevel(LevelCategory.EXP_BOTTLE).getExperience(),
                data.getLevel(LevelCategory.FISHING).getExperience(),
                data.getLevel(LevelCategory.FURNACE).getExperience(),
                data.getLevel(LevelCategory.GRINDSTONE).getExperience(),
                data.getLevel(LevelCategory.PLAYER_DEATH).getExperience(),
                data.getLevel(LevelCategory.VILLAGER_TRADE).getExperience()
        )).thenAcceptAsync(n ->
                logger.info("Published userdata to channel: {}", data.toSimpleString())
        );
    }

    /**
     * Gets the cached player data in the message store.
     *
     * @return the PlayerData cached in the message store, or null if it's not cached
     */
    public @Nullable PlayerDataPacket get(UUID uuid) {
        PlayerDataPacket packet = messageStore.getIfPresent(uuid);
        if (packet != null) {
            logger.info("Access userdata from messenger: {}", packet.toSimpleString());
        }
        return packet;
    }

    @Override public void close() {
        agent.close();
    }
}
