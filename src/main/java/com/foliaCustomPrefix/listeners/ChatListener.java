package com.foliaCustomPrefix.listeners;

import com.foliaCustomPrefix.manager.ConfigManager;
import com.foliaCustomPrefix.manager.DisplayManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public final class ChatListener implements Listener {

    private final Plugin plugin;
    private final ConfigManager configManager;
    private final DisplayManager displayManager;

    public ChatListener(Plugin plugin, ConfigManager configManager, DisplayManager displayManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.displayManager = displayManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (configManager.isDebug()) {
            Component prefix = displayManager.resolvePrefix(event.getPlayer());
            plugin.getLogger().info("[Debug] Rendering chat for " + event.getPlayer().getName()
                + " with prefix: '" + PlainTextComponentSerializer.plainText().serialize(prefix) + "'");
        }
        event.renderer((source, sourceDisplayName, message, viewer) -> render(source, message));
    }

    private Component render(Player source, Component message) {
        Component prefix = displayManager.resolvePrefix(source);
        Component name = Component.text(source.getName(), NamedTextColor.GRAY);
        Component body = name
            .append(Component.text(": ", NamedTextColor.GRAY))
            .append(message.colorIfAbsent(NamedTextColor.WHITE));
        if (prefix.equals(Component.empty())) {
            return body;
        }
        return Component.empty()
            .append(prefix)
            .append(Component.space())
            .append(body);
    }
}
