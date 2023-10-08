package cc.mewcraft.adventurelevel.message;

import cc.mewcraft.adventurelevel.data.PlayerData;
import cc.mewcraft.adventurelevel.data.PlayerDataManager;
import cc.mewcraft.adventurelevel.data.PlayerDataUpdater;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import cc.mewcraft.adventurelevel.message.packet.PlayerDataPacket;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import cc.mewcraft.adventurelevel.util.PlayerUtils;
import cc.mewcraft.spatula.network.ServerInfo;

import me.lucko.helper.messaging.Channel;
import me.lucko.helper.messaging.ChannelAgent;
import me.lucko.helper.messaging.Messenger;
import me.lucko.helper.terminable.Terminable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class provides methods to sync data between servers.
 * <p>
 * Why we have this class? Because SQL database is too slow to save/load data when players switch servers.
 */
@Singleton
public class PlayerDataMessenger implements Terminable {
    private static final String SYNC_CHANNEL = "advtrlvl-sync";

    private final AdventureLevelPlugin plugin;
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
            final PlayerDataManager playerDataManager
    ) {
        this.plugin = plugin;
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
                    plugin.getSLF4JLogger().info("Update userdata in cache: name={}, mainXp={}", PlayerUtils.getNameFromUUIDNullable(uuid), message.mainXp());
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
    public void publish(@NotNull PlayerData data) {
        channel.sendMessage(new PlayerDataPacket(
                data.getUuid(),
                ServerInfo.SERVER_ID.get(),
                System.currentTimeMillis(),
                data.getLevel(LevelCategory.MAIN).getExperience(),
                data.getLevel(LevelCategory.BLOCK_BREAK).getExperience(),
                data.getLevel(LevelCategory.BREED).getExperience(),
                data.getLevel(LevelCategory.ENTITY_DEATH).getExperience(),
                data.getLevel(LevelCategory.EXP_BOTTLE).getExperience(),
                data.getLevel(LevelCategory.FISHING).getExperience(),
                data.getLevel(LevelCategory.FURNACE).getExperience(),
                data.getLevel(LevelCategory.GRINDSTONE).getExperience(),
                data.getLevel(LevelCategory.PLAYER_DEATH).getExperience(),
                data.getLevel(LevelCategory.VILLAGER_TRADE).getExperience()
        )).thenAcceptAsync(n -> plugin.getSLF4JLogger().info("Published userdata to channel: name={}, mainXp={}", PlayerUtils.getNameFromUUID(data.getUuid()), data.getLevel(LevelCategory.MAIN).getExperience()));
    }

    /**
     * Gets the cached player data in the message store.
     *
     * @return the PlayerData cached in the message store, or null if it's not cached
     */
    public @Nullable PlayerDataPacket get(UUID uuid) {
        return messageStore.getIfPresent(uuid);
    }

    @Override public void close() throws Exception {
        agent.close();
    }
}
