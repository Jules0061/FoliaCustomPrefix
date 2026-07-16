package com.foliaCustomPrefix.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public final class DataStorage {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final Plugin plugin;
    private final File file;
    private final ReentrantLock ioLock = new ReentrantLock();

    public DataStorage(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    public Map<UUID, PrefixData> load() {
        Map<UUID, PrefixData> result = new HashMap<>();
        ioLock.lock();
        try {
            if (!file.exists()) {
                plugin.saveResource("data.yml", false);
            }
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection players = yaml.getConfigurationSection("players");
            if (players == null) {
                return result;
            }
            for (String key : players.getKeys(false)) {
                UUID uuid = parseUuid(key);
                if (uuid == null) {
                    plugin.getLogger().warning("Skipping invalid UUID in data.yml: " + key);
                    continue;
                }
                ConfigurationSection entry = players.getConfigurationSection(key);
                if (entry == null) {
                    continue;
                }
                String prefix = entry.getString("prefix");
                result.put(uuid, new PrefixData(prefix, readLastChange(entry)));
            }
        } finally {
            ioLock.unlock();
        }
        return result;
    }

    public void save(Map<UUID, PrefixData> snapshot) {
        ioLock.lock();
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            ConfigurationSection players = yaml.createSection("players");
            for (Map.Entry<UUID, PrefixData> entry : snapshot.entrySet()) {
                PrefixData data = entry.getValue();
                ConfigurationSection section = players.createSection(entry.getKey().toString());
                if (data.hasPrefix()) {
                    section.set("prefix", data.prefix());
                }
                section.set("last-change", formatMillis(data.lastChange()));
            }
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save data.yml", e);
        } finally {
            ioLock.unlock();
        }
    }

    private long readLastChange(ConfigurationSection entry) {
        if (entry.isLong("last-change") || entry.isInt("last-change")) {
            return entry.getLong("last-change", 0L);
        }
        String value = entry.getString("last-change");
        if (value == null || value.isEmpty()) {
            return 0L;
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(value, DATE_FORMAT);
            return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            plugin.getLogger().warning("Invalid last-change date in data.yml: " + value);
            return 0L;
        }
    }

    private String formatMillis(long millis) {
        return DATE_FORMAT.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()));
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
