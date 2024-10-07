package cc.mewcraft.adventurelevel.plugin;

import cc.mewcraft.adventurelevel.command.CommandManager;
import cc.mewcraft.adventurelevel.data.PlayerDataManager;
import cc.mewcraft.adventurelevel.data.PlayerDataManagerImpl;
import cc.mewcraft.adventurelevel.file.DataStorage;
import cc.mewcraft.adventurelevel.file.SQLDataStorage;
import cc.mewcraft.adventurelevel.hooks.luckperms.LevelContextCalculator;
import cc.mewcraft.adventurelevel.hooks.placeholder.MiniPlaceholderExpansion;
import cc.mewcraft.adventurelevel.hooks.placeholder.PAPIPlaceholderExpansion;
import cc.mewcraft.adventurelevel.listener.PickupExpListener;
import cc.mewcraft.adventurelevel.listener.UserdataListener;
import cc.mewcraft.adventurelevel.message.PlayerDataMessenger;
import cc.mewcraft.adventurelevel.util.Translations;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

public class AdventureLevelPlugin extends ExtendedJavaPlugin implements AdventureLevel {
    private static AdventureLevelPlugin INSTANCE;

    private Logger logger;
    private Injector injector;
    private DataStorage dataStorage;
    private PlayerDataMessenger playerDataMessenger;
    private PlayerDataManager playerDataManager;
    private Translations translations;

    public static @NonNull AdventureLevelPlugin getInstance() {
        return INSTANCE;
    }

    @Override protected void enable() {
        INSTANCE = this;

        logger = getSLF4JLogger();
        injector = Guice.createInjector(new AbstractModule() {
            @Override protected void configure() {
                bind(AdventureLevelPlugin.class).toInstance(AdventureLevelPlugin.this);
                bind(Logger.class).toInstance(getSLF4JLogger());
                bind(DataStorage.class).to(SQLDataStorage.class);
                bind(PlayerDataManager.class).to(PlayerDataManagerImpl.class);
            }
        });

        saveDefaultConfig();
        saveResourceRecursively("calc");

        translations = new Translations(this, "lang");

        dataStorage = bind(injector.getInstance(DataStorage.class));
        dataStorage.init();

        playerDataManager = bind(injector.getInstance(PlayerDataManager.class));
        playerDataMessenger = bind(injector.getInstance(PlayerDataMessenger.class));
        playerDataMessenger.registerListeners();

        // Register listeners
        registerTerminableListener(injector.getInstance(PickupExpListener.class)).bindWith(this);
        registerTerminableListener(injector.getInstance(UserdataListener.class)).bindWith(this);

        // Register placeholders
        if (isPluginPresent("MiniPlaceholders"))
            injector.getInstance(MiniPlaceholderExpansion.class).register().bindWith(this);
        if (isPluginPresent("PlaceholderAPI"))
            injector.getInstance(PAPIPlaceholderExpansion.class).register().bindWith(this);

        // Register LuckPerms contexts
        if (isPluginPresent("LuckPerms"))
            injector.getInstance(LevelContextCalculator.class).register();

        // Register commands
        new CommandManager(this);

        // Register API instance
        AdventureLevelProvider.register(this);
    }

    public @NonNull DataStorage getDataStorage() {
        return dataStorage;
    }

    public @NonNull PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public @NonNull PlayerDataMessenger getPlayerDataMessenger() {
        return playerDataMessenger;
    }

    public @NonNull Translations getLang() {
        return translations;
    }
}
