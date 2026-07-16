package com.foliaCustomPrefix.listeners;

import com.foliaCustomPrefix.manager.DisplayManager;
import com.foliaCustomPrefix.manager.PrefixManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class JoinListener implements Listener {

    private final PrefixManager prefixManager;
    private final DisplayManager displayManager;

    public JoinListener(PrefixManager prefixManager, DisplayManager displayManager) {
        this.prefixManager = prefixManager;
        this.displayManager = displayManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        displayManager.updateDisplay(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        prefixManager.saveAsync();
    }
}
