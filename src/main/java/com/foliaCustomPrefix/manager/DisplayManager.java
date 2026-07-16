package com.foliaCustomPrefix.manager;

import com.foliaCustomPrefix.hooks.LuckPermsHook;
import com.foliaCustomPrefix.util.MiniMessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class DisplayManager {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .build();

    private final Plugin plugin;
    private final PrefixManager prefixManager;
    private final LuckPermsHook luckPermsHook;

    public DisplayManager(Plugin plugin, PrefixManager prefixManager, LuckPermsHook luckPermsHook) {
        this.plugin = plugin;
        this.prefixManager = prefixManager;
        this.luckPermsHook = luckPermsHook;
    }

    public Component resolvePrefix(Player player) {
        String custom = prefixManager.getPrefix(player.getUniqueId());
        if (custom != null) {
            return MiniMessageUtil.parse(custom);
        }
        String luckPermsPrefix = luckPermsHook.getPrefix(player);
        if (luckPermsPrefix == null || luckPermsPrefix.isEmpty()) {
            return Component.empty();
        }
        return LEGACY.deserialize(luckPermsPrefix.replace('§', '&'));
    }

    public void updateDisplay(Player player) {
        player.getScheduler().run(plugin, task -> {
            if (!player.isOnline()) {
                return;
            }
            Component prefix = resolvePrefix(player);
            Component name = Component.text(player.getName(), NamedTextColor.GRAY);
            Component display = prefix.equals(Component.empty())
                ? name
                : Component.empty().append(prefix).append(Component.space()).append(name);
            player.displayName(display);
            player.playerListName(display);
        }, null);
    }
}
