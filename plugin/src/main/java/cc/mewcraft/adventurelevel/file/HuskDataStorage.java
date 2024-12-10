package cc.mewcraft.adventurelevel.file;

import cc.mewcraft.adventurelevel.data.SimpleUserData;
import cc.mewcraft.adventurelevel.level.LevelFactory;
import cc.mewcraft.adventurelevel.level.category.Level;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import cc.mewcraft.adventurelevel.util.PlayerUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import net.william278.husksync.BukkitHuskSync;
import net.william278.husksync.HuskSync;
import net.william278.husksync.adapter.Adaptable;
import net.william278.husksync.api.HuskSyncAPI;
import net.william278.husksync.data.BukkitData;
import net.william278.husksync.data.Identifier;
import net.william278.husksync.data.Serializer;
import net.william278.husksync.user.BukkitUser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

// FIXME 未完全测试的 AbstractDataStorage 实现
//  用于试验使用 HuskSync 来作为数据储存/同步的方式

@Singleton
public class HuskDataStorage extends AbstractUserDataStorage {
    private static final Identifier HUSK_PLAYER_DATA_ID = Identifier.from("adventurelevel", "player_data");

    // HuskSyncAPI
    private final HuskSyncAPI huskSyncApi;

    // Logger
    private final Logger logger;

    @Inject
    public HuskDataStorage(
            final Logger logger
    ) {
        super();
        this.huskSyncApi = HuskSyncAPI.getInstance();
        this.logger = logger;
    }

    @Override
    public void init() {
        HuskSync huskSync = huskSyncApi.getPlugin();
        huskSync.registerSerializer(HUSK_PLAYER_DATA_ID, new HuskUserDataSerializer(huskSync));
    }

    @Override
    public @NonNull SimpleUserData create(@NonNull UUID uuid) {
        // Construct a map of empty levels
        final ConcurrentHashMap<LevelCategory, Level> levels = new ConcurrentHashMap<>() {{
            put(LevelCategory.PRIMARY, LevelFactory.newLevel(LevelCategory.PRIMARY));
            put(LevelCategory.PLAYER_DEATH, LevelFactory.newLevel(LevelCategory.PLAYER_DEATH));
            put(LevelCategory.ENTITY_DEATH, LevelFactory.newLevel(LevelCategory.ENTITY_DEATH));
            put(LevelCategory.FURNACE, LevelFactory.newLevel(LevelCategory.FURNACE));
            put(LevelCategory.BREED, LevelFactory.newLevel(LevelCategory.BREED));
            put(LevelCategory.VILLAGER_TRADE, LevelFactory.newLevel(LevelCategory.VILLAGER_TRADE));
            put(LevelCategory.FISHING, LevelFactory.newLevel(LevelCategory.FISHING));
            put(LevelCategory.BLOCK_BREAK, LevelFactory.newLevel(LevelCategory.BLOCK_BREAK));
            put(LevelCategory.EXP_BOTTLE, LevelFactory.newLevel(LevelCategory.EXP_BOTTLE));
            put(LevelCategory.GRINDSTONE, LevelFactory.newLevel(LevelCategory.GRINDSTONE));
        }};

        final SimpleUserData simpleUserData = new SimpleUserData(uuid, levels);
        final HuskUserData huskUserData = new HuskUserData(simpleUserData);
        final BukkitUser bukkitUser = (BukkitUser) huskSyncApi.getUser(uuid).join().orElseThrow(() ->
                new IllegalStateException("failed to create new userdata in HuskSync data storage for player " + PlayerUtils.getPrettyString(uuid))
        );

        bukkitUser.setData(HUSK_PLAYER_DATA_ID, huskUserData);
        logger.info("[{}] Created new userdata in HuskSync data storage: {}", PlayerUtils.getName(uuid), simpleUserData);

        return simpleUserData;
    }

    @Override
    public @Nullable SimpleUserData load(@NonNull UUID uuid) {
        BukkitUser bukkitUser = (BukkitUser) huskSyncApi.getUser(uuid).join().orElseThrow();
        HuskUserData huskUserData = (HuskUserData) bukkitUser.getData(HUSK_PLAYER_DATA_ID).orElseGet(() -> {
            logger.warn("[{}] Userdata not found in HuskSync data storage", PlayerUtils.getName(uuid));
            return null;
        });

        if (huskUserData == null) {
            return null;
        }

        logger.info("[{}] Loaded userdata from HuskSync data storage: {}", PlayerUtils.getName(uuid), huskUserData);
        return new SimpleUserData(uuid, huskUserData.getLevels());
    }

    @Override
    public void save(@NonNull SimpleUserData data) {
        if (!data.isPopulated()) {
            logger.warn("[{}] Skipped saving userdata to HuskSync data storage", PlayerUtils.getName(data.getUuid()));
            return;
        }

        HuskUserData huskUserData = new HuskUserData(data);
        BukkitUser bukkitUser = (BukkitUser) huskSyncApi.getUser(data.getUuid()).join().orElseThrow();
        bukkitUser.setData(HUSK_PLAYER_DATA_ID, huskUserData);
        logger.info("[{}] Saved userdata to HuskSync data storage: {}", PlayerUtils.getName(data.getUuid()), data);
    }

    @Override
    public void close() {
        // NOP
    }

    private static class HuskUserData extends BukkitData implements Adaptable, Examinable {
        public final long timestamp;
        public final int primaryXp;
        public final int blockBreakXp;
        public final int breedXp;
        public final int entityDeathXp;
        public final int expBottleXp;
        public final int fishingXp;
        public final int furnaceXp;
        public final int grindstoneXp;
        public final int playerDeathXp;
        public final int villagerTradeXp;

        public HuskUserData(SimpleUserData data) {
            this.timestamp = System.currentTimeMillis();
            this.primaryXp = data.getLevel(LevelCategory.PRIMARY).getExperience();
            this.blockBreakXp = data.getLevel(LevelCategory.BLOCK_BREAK).getExperience();
            this.breedXp = data.getLevel(LevelCategory.BREED).getExperience();
            this.entityDeathXp = data.getLevel(LevelCategory.ENTITY_DEATH).getExperience();
            this.expBottleXp = data.getLevel(LevelCategory.EXP_BOTTLE).getExperience();
            this.fishingXp = data.getLevel(LevelCategory.FISHING).getExperience();
            this.furnaceXp = data.getLevel(LevelCategory.FURNACE).getExperience();
            this.grindstoneXp = data.getLevel(LevelCategory.GRINDSTONE).getExperience();
            this.playerDeathXp = data.getLevel(LevelCategory.PLAYER_DEATH).getExperience();
            this.villagerTradeXp = data.getLevel(LevelCategory.VILLAGER_TRADE).getExperience();
        }

        @Override
        public void apply(@NotNull BukkitUser user, @NotNull BukkitHuskSync plugin) throws IllegalStateException {
            // NOP
        }

        public ConcurrentHashMap<LevelCategory, Level> getLevels() {
            return new ConcurrentHashMap<>() {{
                put(LevelCategory.PRIMARY, LevelFactory.newLevel(LevelCategory.PRIMARY).withExperience(primaryXp));
                put(LevelCategory.PLAYER_DEATH, LevelFactory.newLevel(LevelCategory.PLAYER_DEATH).withExperience(playerDeathXp));
                put(LevelCategory.ENTITY_DEATH, LevelFactory.newLevel(LevelCategory.ENTITY_DEATH).withExperience(entityDeathXp));
                put(LevelCategory.FURNACE, LevelFactory.newLevel(LevelCategory.FURNACE).withExperience(furnaceXp));
                put(LevelCategory.BREED, LevelFactory.newLevel(LevelCategory.BREED).withExperience(breedXp));
                put(LevelCategory.VILLAGER_TRADE, LevelFactory.newLevel(LevelCategory.VILLAGER_TRADE).withExperience(villagerTradeXp));
                put(LevelCategory.FISHING, LevelFactory.newLevel(LevelCategory.FISHING).withExperience(fishingXp));
                put(LevelCategory.BLOCK_BREAK, LevelFactory.newLevel(LevelCategory.BLOCK_BREAK).withExperience(blockBreakXp));
                put(LevelCategory.EXP_BOTTLE, LevelFactory.newLevel(LevelCategory.EXP_BOTTLE).withExperience(expBottleXp));
                put(LevelCategory.GRINDSTONE, LevelFactory.newLevel(LevelCategory.GRINDSTONE).withExperience(grindstoneXp));
            }};
        }

        @Override public @NonNull Stream<? extends ExaminableProperty> examinableProperties() {
            return Stream.of(
                    ExaminableProperty.of("timestamp", timestamp),
                    ExaminableProperty.of("primaryXp", primaryXp),
                    ExaminableProperty.of("blockBreakXp", blockBreakXp),
                    ExaminableProperty.of("breedXp", breedXp),
                    ExaminableProperty.of("entityDeathXp", entityDeathXp),
                    ExaminableProperty.of("expBottleXp", expBottleXp),
                    ExaminableProperty.of("fishingXp", fishingXp),
                    ExaminableProperty.of("furnaceXp", furnaceXp),
                    ExaminableProperty.of("grindstoneXp", grindstoneXp),
                    ExaminableProperty.of("playerDeathXp", playerDeathXp),
                    ExaminableProperty.of("villagerTradeXp", villagerTradeXp)
            );
        }

        public @NonNull String toString() {
            return "HuskUserData{timestamp=" + timestamp + ", primaryExp=" + primaryXp + "}";
        }
    }

    private static class HuskUserDataSerializer extends Serializer.Json<HuskUserData> implements Serializer<HuskUserData> {

        // We need to create a constructor that takes our instance of the API
        public HuskUserDataSerializer(@NotNull HuskSync huskSync) {
            // We pass the class type here so that Gson knows what class we're serializing
            super(huskSync, HuskUserData.class);
        }

    }
}