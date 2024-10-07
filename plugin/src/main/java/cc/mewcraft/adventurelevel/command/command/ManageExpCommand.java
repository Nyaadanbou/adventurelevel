package cc.mewcraft.adventurelevel.command.command;

import cc.mewcraft.adventurelevel.command.AbstractCommand;
import cc.mewcraft.adventurelevel.command.CommandManager;
import cc.mewcraft.adventurelevel.command.parser.PlayerDataParser;
import cc.mewcraft.adventurelevel.data.PlayerData;
import cc.mewcraft.adventurelevel.level.category.Level;
import cc.mewcraft.adventurelevel.level.category.LevelCategory;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import cc.mewcraft.adventurelevel.util.PlayerUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.parser.standard.IntegerParser;

import java.util.List;
import java.util.function.Function;

public class ManageExpCommand extends AbstractCommand {
    public ManageExpCommand(
            final AdventureLevelPlugin plugin,
            final CommandManager manager
    ) {
        super(plugin, manager);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override public void register() {
        Command<CommandSender> setExpCommand = manager.get().commandBuilder("adventurelevel")
                .literal("set")
                .required("userdata", PlayerDataParser.playerDataParser())
                .required("category", EnumParser.enumComponent(LevelOption.class))
                .required("amount", IntegerParser.integerParser(0))
                .flag(CommandFlag.builder("level"))
                .permission("adventurelevel.command.admin")
                .handler(context -> {
                    CommandSender sender = context.sender();

                    PlayerData userdata = context.get("userdata");
                    LevelOption category = context.get("category");
                    int amount = context.get("amount");

                    boolean useLevel = context.flags().isPresent("level");
                    TagResolver[] resolvers = {
                            Placeholder.unparsed("player", PlayerUtils.getNameFromUUID(userdata.getUuid())),
                            Placeholder.unparsed("category", category.name()),
                            Placeholder.unparsed("amount", String.valueOf(amount))
                    };

                    if (useLevel) {
                        category.mapping.apply(userdata).setLevel(amount);
                        plugin.getLang().of("msg_player_level_is_set").resolver(resolvers).send(sender);
                    } else {
                        category.mapping.apply(userdata).setExperience(amount);
                        plugin.getLang().of("msg_player_xp_is_set").resolver(resolvers).send(sender);
                    }
                })
                .build();

        Command<CommandSender> addExpCommand = manager.get().commandBuilder("adventurelevel")
                .literal("add")
                .required("userdata", PlayerDataParser.playerDataParser())
                .required("category", EnumParser.enumParser(LevelOption.class))
                .required("amount", IntegerParser.integerParser(0))
                .flag(CommandFlag.builder("level"))
                .permission("adventurelevel.command.admin")
                .handler(context -> {
                    CommandSender sender = context.sender();

                    PlayerData userdata = context.get("userdata");
                    LevelOption category = context.get("category");
                    int amount = context.get("amount");

                    boolean useLevel = context.flags().isPresent("level");
                    TagResolver[] resolvers = {
                            Placeholder.unparsed("player", PlayerUtils.getNameFromUUID(userdata.getUuid())),
                            Placeholder.unparsed("category", category.name()),
                            Placeholder.unparsed("amount", String.valueOf(amount))
                    };

                    if (useLevel) {
                        category.mapping.apply(userdata).addLevel(amount);
                        plugin.getLang().of("msg_player_level_is_added").resolver(resolvers).send(sender);
                    } else {
                        category.mapping.apply(userdata).addExperience(amount);
                        plugin.getLang().of("msg_player_xp_is_added").resolver(resolvers).send(sender);
                    }
                })
                .build();

        manager.register(List.of(
                setExpCommand,
                addExpCommand
        ));
    }

    private enum LevelOption {
        primary(playerData -> playerData.getLevel(LevelCategory.PRIMARY)),
        player_death(playerData -> playerData.getLevel(LevelCategory.PLAYER_DEATH)),
        entity_death(playerData -> playerData.getLevel(LevelCategory.ENTITY_DEATH)),
        furnace(playerData -> playerData.getLevel(LevelCategory.FURNACE)),
        breed(playerData -> playerData.getLevel(LevelCategory.BREED)),
        villager_trade(playerData -> playerData.getLevel(LevelCategory.VILLAGER_TRADE)),
        fishing(playerData -> playerData.getLevel(LevelCategory.FISHING)),
        block_break(playerData -> playerData.getLevel(LevelCategory.BLOCK_BREAK)),
        exp_bottle(playerData -> playerData.getLevel(LevelCategory.EXP_BOTTLE)),
        grindstone(playerData -> playerData.getLevel(LevelCategory.GRINDSTONE));

        public final Function<PlayerData, Level> mapping;

        LevelOption(Function<PlayerData, Level> mapping) {
            this.mapping = mapping;
        }
    }
}
