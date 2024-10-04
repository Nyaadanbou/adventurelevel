package cc.mewcraft.adventurelevel.command.parser;

import cc.mewcraft.adventurelevel.data.PlayerData;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.bukkit.BukkitCommandContextKeys;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.util.ArrayList;
import java.util.List;

@DefaultQualifier(NonNull.class)
public class PlayerDataParser implements ArgumentParser<CommandSender, PlayerData>, BlockingSuggestionProvider.Strings<CommandSender> {

    public static ParserDescriptor<CommandSender, PlayerData> playerDataParser() {
        return ParserDescriptor.of(new PlayerDataParser(), new TypeToken<>() {});
    }

    public static CommandComponent.Builder<CommandSender, PlayerData> playerDataComponent() {
        return CommandComponent.<CommandSender, PlayerData>builder().parser(new PlayerDataParser());
    }

    @Override public @NonNull ArgumentParseResult<@NonNull PlayerData> parse(
            @NonNull final CommandContext<@NonNull CommandSender> commandContext,
            @NonNull final CommandInput commandInput
    ) {
        String input = commandInput.peekString();
        if (input.isBlank()) {
            return ArgumentParseResult.failure(new IllegalArgumentException());
        }

        CommandSender sender = commandContext.sender();
        @Nullable OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(input);
        if (offlinePlayer == null) {
            return ArgumentParseResult.failure(
                    new IllegalArgumentException(AdventureLevelPlugin.getInstance().getLang().of("msg_player_is_null").locale(sender).plain())
            );
        }

        PlayerData playerData = AdventureLevelPlugin.getInstance().getPlayerDataManager().load(offlinePlayer);
        if (!playerData.equals(PlayerData.DUMMY)) {
            commandInput.readString();
            return ArgumentParseResult.success(playerData);
        }

        return ArgumentParseResult.failure(
                new IllegalArgumentException(AdventureLevelPlugin.getInstance().getLang().of("msg_player_is_null").plain())
        );
    }

    @Override public @NonNull Iterable<@NonNull String> stringSuggestions(
            @NonNull final CommandContext<CommandSender> context,
            @NonNull final CommandInput input
    ) {
        List<String> suggestions = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            final CommandSender bukkit = context.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);
            if (bukkit instanceof Player && !((Player) bukkit).canSee(player)) {
                continue;
            }
            suggestions.add(player.getName());
        }

        return suggestions;
    }
}
