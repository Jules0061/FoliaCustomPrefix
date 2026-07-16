package com.foliaCustomPrefix;

import com.foliaCustomPrefix.command.PrefixCommand;
import com.foliaCustomPrefix.data.DataStorage;
import com.foliaCustomPrefix.hooks.LuckPermsHook;
import com.foliaCustomPrefix.hooks.PlaceholderHook;
import com.foliaCustomPrefix.listeners.ChatListener;
import com.foliaCustomPrefix.listeners.JoinListener;
import com.foliaCustomPrefix.manager.ConfigManager;
import com.foliaCustomPrefix.manager.CooldownManager;
import com.foliaCustomPrefix.manager.DisplayManager;
import com.foliaCustomPrefix.manager.PrefixManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class FoliaCustomPrefix extends JavaPlugin {

    private PrefixManager prefixManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        ConfigManager configManager = new ConfigManager(this);
        DataStorage dataStorage = new DataStorage(this);
        this.prefixManager = new PrefixManager(this, dataStorage);
        this.prefixManager.loadAll();
        CooldownManager cooldownManager = new CooldownManager(configManager, prefixManager);
        LuckPermsHook luckPermsHook = new LuckPermsHook();
        DisplayManager displayManager = new DisplayManager(this, prefixManager, luckPermsHook);

        PluginCommand command = getCommand("prefix");
        if (command == null) {
            getLogger().severe("Command 'prefix' is missing from plugin.yml, disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        PrefixCommand executor = new PrefixCommand(configManager, prefixManager, cooldownManager, displayManager, luckPermsHook);
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        getServer().getPluginManager().registerEvents(new ChatListener(this, configManager, displayManager), this);
        getServer().getPluginManager().registerEvents(new JoinListener(prefixManager, displayManager), this);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this, prefixManager, luckPermsHook).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        }

        getLogger().info("FoliaCustomPrefix enabled.");
    }

    @Override
    public void onDisable() {
        if (prefixManager != null) {
            prefixManager.saveSync();
        }
        getLogger().info("FoliaCustomPrefix disabled.");
    }
}
