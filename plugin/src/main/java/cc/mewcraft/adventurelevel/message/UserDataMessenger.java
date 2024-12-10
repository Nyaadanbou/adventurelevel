package cc.mewcraft.adventurelevel.message;

import cc.mewcraft.adventurelevel.data.SimpleUserData;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import cc.mewcraft.adventurelevel.message.packet.UserDataPacket;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import cc.mewcraft.adventurelevel.util.PlayerUtils;
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
 * Why we have this class?
 * Because SQL database is too slow to save/load data when players switch servers.
 */
@Singleton
public class UserDataMessenger implements Terminable {
    private static final String SYNC_CHANNEL_NAME = "adventurelevel-messenger";

    private final Logger logger;

    /**
     * A channel to send messages.
     */
    private final Channel<UserDataPacket> channel;

    /**
     * An agent created from the channel, used to register listeners.
     */
    private final ChannelAgent<UserDataPacket> channelAgent;

    /**
     * A cache that stores the messages received from other servers.
     * <p>
     * The entries should be expired in a very short period of time.
     */
    private final Cache<UUID, UserDataPacket> messageStore;

    @Inject
    public UserDataMessenger(final Logger logger) {
        this.logger = logger;

        final AdventureLevelPlugin plugin = AdventureLevelPlugin.instance();
        final long messageExpireMilliseconds = Math.max(0, plugin.getConfig().getLong("synchronization.message_expire_milliseconds"));
        this.messageStore = CacheBuilder.newBuilder().expireAfterWrite(Duration.of(messageExpireMilliseconds, ChronoUnit.MILLIS)).build();

        // Get and define the channel.
        this.channel = plugin.getService(Messenger.class).getChannel(SYNC_CHANNEL_NAME, UserDataPacket.class);

        // Create an agent for the channel.
        this.channelAgent = this.channel.newAgent();
    }

    public void registerListeners() {
        channel.newAgent((agent, message) -> {
            if (Objects.equals(ServerInfo.SERVER_ID.get(), message.server())) {
                return; // ignore packets sent from the same server
            }

            UUID uuid = message.uuid();
            messageStore.put(uuid, message);
            logger.info("[{}] Stored userdata from channel: {}", PlayerUtils.getName(uuid), message);
        });
    }

    /**
     * This method should be called upon player quitting the server.
     * <p>
     * <b>Caveat:</b> The data should be published only if {@link SimpleUserData#isPopulated()} returns true.
     * Callers should check {@link SimpleUserData#isPopulated()} before calling this method.
     *
     * @param data the player data to be sent to the channel
     */
    public void publish(@NonNull SimpleUserData data) {
        channel.sendMessage(new UserDataPacket(
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
                logger.info("[{}] Published userdata to channel: {}", PlayerUtils.getName(data.getUuid()), data)
        );
    }

    /**
     * Gets the cached player data in the message store.
     *
     * @return the {@link UserDataPacket} cached in the message store, or null if it's not cached
     */
    public @Nullable UserDataPacket get(UUID uuid) {
        UserDataPacket packet = messageStore.getIfPresent(uuid);
        if (packet != null) {
            logger.info("[{}] Access userdata from messenger: {}", PlayerUtils.getName(uuid), packet);
        }
        return packet;
    }

    @Override public void close() {
        // clean up agent
        channelAgent.close();
        // clean up cache
        messageStore.cleanUp();
    }
}
