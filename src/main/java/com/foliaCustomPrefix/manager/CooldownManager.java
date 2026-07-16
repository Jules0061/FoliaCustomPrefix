package com.foliaCustomPrefix.manager;

import java.util.UUID;

public final class CooldownManager {

    private final ConfigManager configManager;
    private final PrefixManager prefixManager;

    public CooldownManager(ConfigManager configManager, PrefixManager prefixManager) {
        this.configManager = configManager;
        this.prefixManager = prefixManager;
    }

    public long getRemainingMillis(UUID uuid) {
        long lastChange = prefixManager.getLastChange(uuid);
        if (lastChange <= 0L) {
            return 0L;
        }
        long elapsed = System.currentTimeMillis() - lastChange;
        return Math.max(0L, configManager.getCooldownMillis() - elapsed);
    }
}
