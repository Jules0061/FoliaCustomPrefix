package com.foliaCustomPrefix.manager;

import com.foliaCustomPrefix.data.DataStorage;
import com.foliaCustomPrefix.data.PrefixData;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PrefixManager {

    private final Plugin plugin;
    private final DataStorage storage;
    private final ConcurrentHashMap<UUID, PrefixData> cache = new ConcurrentHashMap<>();

    public PrefixManager(Plugin plugin, DataStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void loadAll() {
        cache.clear();
        cache.putAll(storage.load());
        plugin.getLogger().info("Loaded " + cache.size() + " prefix entries.");
    }

    public String getPrefix(UUID uuid) {
        PrefixData data = cache.get(uuid);
        return data != null && data.hasPrefix() ? data.prefix() : null;
    }

    public long getLastChange(UUID uuid) {
        PrefixData data = cache.get(uuid);
        return data != null ? data.lastChange() : 0L;
    }

    public void setPrefix(UUID uuid, String prefix) {
        cache.put(uuid, new PrefixData(prefix, System.currentTimeMillis()));
        saveAsync();
    }

    public void resetPrefix(UUID uuid) {
        cache.put(uuid, new PrefixData(null, System.currentTimeMillis()));
        saveAsync();
    }

    public void removeEntry(UUID uuid) {
        cache.remove(uuid);
        saveAsync();
    }

    public void saveAsync() {
        Map<UUID, PrefixData> snapshot = Map.copyOf(cache);
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> storage.save(snapshot));
    }

    public void saveSync() {
        storage.save(Map.copyOf(cache));
    }
}
