package com.foliaCustomPrefix.hooks;

import com.foliaCustomPrefix.manager.PrefixManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlaceholderHook extends PlaceholderExpansion {

    private final Plugin plugin;
    private final PrefixManager prefixManager;
    private final LuckPermsHook luckPermsHook;

    public PlaceholderHook(Plugin plugin, PrefixManager prefixManager, LuckPermsHook luckPermsHook) {
        this.plugin = plugin;
        this.prefixManager = prefixManager;
        this.luckPermsHook = luckPermsHook;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "foliacustomprefix";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return null;
        }
        if (params.equalsIgnoreCase("prefix")) {
            String custom = prefixManager.getPrefix(player.getUniqueId());
            if (custom != null) {
                return custom;
            }
            String luckPermsPrefix = luckPermsHook.getPrefix(player.getUniqueId());
            return luckPermsPrefix != null ? luckPermsPrefix : "";
        }
        if (params.equalsIgnoreCase("has_prefix")) {
            return Boolean.toString(prefixManager.getPrefix(player.getUniqueId()) != null);
        }
        return null;
    }
}
