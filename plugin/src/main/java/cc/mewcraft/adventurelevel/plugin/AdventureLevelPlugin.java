package cc.mewcraft.adventurelevel.plugin;

import cc.mewcraft.adventurelevel.command.CommandManager;
import cc.mewcraft.adventurelevel.data.PlayerDataManager;
import cc.mewcraft.adventurelevel.data.PlayerDataManagerImpl;
import cc.mewcraft.adventurelevel.file.DataStorage;
import cc.mewcraft.adventurelevel.file.SQLDataStorage;
import cc.mewcraft.adventurelevel.hooks.luckperms.LevelContextCalculator;
import cc.mewcraft.adventurelevel.hooks.placeholder.MiniPlaceholderExpansion;
import cc.mewcraft.adventurelevel.hooks.placeholder.PAPIPlaceholderExpansion;
import cc.mewcraft.adventurelevel.listener.data.NetworkUserdataListener;
import cc.mewcraft.adventurelevel.listener.data.SimpleUserdataListener;
import cc.mewcraft.adventurelevel.listener.game.PickupExpListener;
import cc.mewcraft.adventurelevel.message.PlayerDataMessenger;
import cc.mewcraft.adventurelevel.util.Translations;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

import static net.kyori.adventure.text.Component.text;

public class AdventureLevelPlugin extends ExtendedJavaPlugin implements AdventureLevel {
    private static AdventureLevelPlugin INSTANCE;

    private Injector injector;
    private ComponentLogger logger;

    public static @NonNull AdventureLevelPlugin getInstance() {
        return INSTANCE;
    }

    @Override protected void load() {
        logger = getComponentLogger();
    }

    @Override protected void enable() {
        INSTANCE = this;

        injector = Guice.createInjector(new AbstractModule() {
            @Override protected void configure() {
                bind(AdventureLevelPlugin.class).toInstance(AdventureLevelPlugin.this);
                bind(Logger.class).toInstance(getSLF4JLogger());

                bind(DataStorage.class).to(SQLDataStorage.class).in(Scopes.SINGLETON);
                bind(PlayerDataManager.class).to(PlayerDataManagerImpl.class).in(Scopes.SINGLETON);
                bind(Translations.class).toProvider(() ->
                        new Translations(AdventureLevelPlugin.this, "lang")
                ).in(Scopes.SINGLETON);
            }
        });

        saveDefaultConfig();
        saveResourceRecursively("calc");

        bind(injector.getInstance(DataStorage.class)).init();
        bind(injector.getInstance(PlayerDataManager.class));
        bind(injector.getInstance(PlayerDataMessenger.class)).registerListeners();

        // Register listeners
        bind(registerTerminableListener(injector.getInstance(PickupExpListener.class)));
        if (isPluginPresent("HuskSync")) {
            logger.info(text("HuskSync found, using NetworkUserdataListener").color(NamedTextColor.AQUA));
            bind(registerTerminableListener(injector.getInstance(NetworkUserdataListener.class)));
        } else {
            logger.info(text("HuskSync not found, using SimpleUserdataListener").color(NamedTextColor.AQUA));
            bind(registerTerminableListener(injector.getInstance(SimpleUserdataListener.class)));
        }

        // Register placeholders
        if (isPluginPresent("MiniPlaceholders"))
            bind(injector.getInstance(MiniPlaceholderExpansion.class).register());
        if (isPluginPresent("PlaceholderAPI"))
            bind(injector.getInstance(PAPIPlaceholderExpansion.class).register());

        // Register LuckPerms contexts
        if (isPluginPresent("LuckPerms"))
            injector.getInstance(LevelContextCalculator.class).register();

        // Register commands
        new CommandManager(this);

        // Register API instance
        AdventureLevelProvider.register(this);
    }

    /**
     * 重载配置文件, 仅有部分数据支持重载.
     */
    public void reloadConfigPart() {
        reloadConfig();
        translations().reload();
        playerDataManager().cleanup();
    }

    @Override
    public @NonNull PlayerDataManager playerDataManager() {
        return injector.getInstance(PlayerDataManager.class);
    }

    public @NonNull PlayerDataMessenger playerDataMessenger() {
        return injector.getInstance(PlayerDataMessenger.class);
    }

    public @NonNull Translations translations() {
        return injector.getInstance(Translations.class);
    }
}
