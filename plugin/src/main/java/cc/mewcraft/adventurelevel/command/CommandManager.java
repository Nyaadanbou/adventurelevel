package cc.mewcraft.adventurelevel.command;

import cc.mewcraft.adventurelevel.command.command.ManageExpCommand;
import cc.mewcraft.adventurelevel.command.command.ReloadPluginCommand;
import cc.mewcraft.adventurelevel.plugin.AdventureLevelPlugin;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import java.util.List;

public class CommandManager {

    private final AdventureLevelPlugin plugin;
    private final LegacyPaperCommandManager<CommandSender> manager;

    public CommandManager(AdventureLevelPlugin plugin) {
        this.plugin = plugin;
        this.manager = new LegacyPaperCommandManager<>(plugin, ExecutionCoordinator.simpleCoordinator(), SenderMapper.identity());
        this.manager.registerLegacyPaperBrigadier();

        this.registerCommands();
    }

    public LegacyPaperCommandManager<CommandSender> get() {
        return manager;
    }

    public void register(final List<Command<CommandSender>> commands) {
        for (final Command<CommandSender> command : commands) {
            manager.command(command);
        }
    }

    public void registerCommands() {
        List.of(
                new ManageExpCommand(plugin, this),
                new ReloadPluginCommand(plugin, this)
        ).forEach(AbstractCommand::register);
    }
}
