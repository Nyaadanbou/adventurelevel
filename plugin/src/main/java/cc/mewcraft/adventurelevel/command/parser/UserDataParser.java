package cc.mewcraft.adventurelevel.command.parser;

import cc.mewcraft.adventurelevel.data.SimpleUserData;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
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

public class UserDataParser implements ArgumentParser<CommandSender, SimpleUserData>, BlockingSuggestionProvider.Strings<CommandSender> {

    public static ParserDescriptor<CommandSender, SimpleUserData> userDataParser() {
        return ParserDescriptor.of(new UserDataParser(), new TypeToken<>() {});
    }

    public static CommandComponent.Builder<CommandSender, SimpleUserData> userDataComponent() {
        return CommandComponent.<CommandSender, SimpleUserData>builder().parser(new UserDataParser());
    }

    @Override public @NonNull ArgumentParseResult<@NonNull SimpleUserData> parse(
            @NonNull final CommandContext<@NonNull CommandSender> commandContext,
            @NonNull final CommandInput commandInput
    ) {
        String input = commandInput.peekString();
        if (input.isBlank()) {
            return ArgumentParseResult.failure(new IllegalArgumentException());
        }

        CommandSender sender = commandContext.sender();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(input);
        if (offlinePlayer == null) {
            return ArgumentParseResult.failure(
                    new IllegalArgumentException(AdventureLevelPlugin.instance().getTranslations().of("msg_player_is_null").locale(sender).plain())
            );
        }

        SimpleUserData userData = AdventureLevelPlugin.instance().getUserDataManager().getCached0(offlinePlayer.getUniqueId());
        if (userData != null) {
            commandInput.readString();
            return ArgumentParseResult.success(userData);
        }

        return ArgumentParseResult.failure(
                new IllegalArgumentException(AdventureLevelPlugin.instance().getTranslations().of("msg_player_is_null").plain())
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
