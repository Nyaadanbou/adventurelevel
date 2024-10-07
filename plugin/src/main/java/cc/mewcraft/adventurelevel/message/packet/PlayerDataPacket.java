package cc.mewcraft.adventurelevel.message.packet;

import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import me.lucko.helper.messaging.codec.Message;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import net.kyori.examination.string.StringExaminer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;
import java.util.stream.Stream;

@Message(codec = PlayerDataCodec.class) // use specific Codec instead of GsonCodec to encode/decode this class
public record PlayerDataPacket(
        UUID uuid,
        String server,
        long timestamp,
        int primaryXp,
        int blockBreakXp,
        int breedXp,
        int entityDeathXp,
        int expBottleXp,
        int fishingXp,
        int furnaceXp,
        int grindstoneXp,
        int playerDeathXp,
        int villagerTradeXp
) implements Examinable {
    public int getExpByCategory(LevelCategory category) {
        return switch (category) {
            case PRIMARY -> primaryXp;
            case BLOCK_BREAK -> blockBreakXp;
            case BREED -> breedXp;
            case ENTITY_DEATH -> entityDeathXp;
            case EXP_BOTTLE -> expBottleXp;
            case FISHING -> fishingXp;
            case FURNACE -> furnaceXp;
            case GRINDSTONE -> grindstoneXp;
            case PLAYER_DEATH -> playerDeathXp;
            case VILLAGER_TRADE -> villagerTradeXp;
        };
    }

    public @NonNull String toSimpleString() {
        return "PlayerDataPacket{uuid=" + uuid + ", server='" + server + "', timestamp=" + timestamp + ", primaryExp=" + primaryXp + "}";
    }

    @Override public @NonNull Stream<? extends ExaminableProperty> examinableProperties() {
        return Stream.of(
                ExaminableProperty.of("uuid", uuid),
                ExaminableProperty.of("server", server),
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

    @Override public @NonNull String toString() {
        return StringExaminer.simpleEscaping().examine(this);
    }
}
