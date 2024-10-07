package cc.mewcraft.adventurelevel.command.command;

import cc.mewcraft.adventurelevel.command.AbstractCommand;
import cc.mewcraft.adventurelevel.command.CommandManager;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;

import java.util.List;

public class ReloadPluginCommand extends AbstractCommand {
    public ReloadPluginCommand(
            final AdventureLevelPlugin plugin,
            final CommandManager manager
    ) {
        super(plugin, manager);
    }

    @Override public void register() {
        Command<CommandSender> reloadCommand = manager.get().commandBuilder("adventurelevel")
                .literal("reload")
                .permission("adventurelevel.command.admin")
                .handler(context -> {
                    plugin.reloadConfigPart();
                    plugin.translations().of("msg_config_reloaded").resolver(
                            Placeholder.unparsed("plugin", plugin.getName()),
                            Placeholder.unparsed("version", plugin.getPluginMeta().getVersion()),
                            Placeholder.unparsed("author", plugin.getPluginMeta().getAuthors().getFirst())
                    ).send(context.sender());
                })
                .build();

        manager.register(List.of(reloadCommand));
    }
}
