package com.foliaCustomPrefix.manager;

import com.foliaCustomPrefix.util.MiniMessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class ConfigManager {

    private final Plugin plugin;
    private final Map<String, String> messages = new ConcurrentHashMap<>();
    private volatile long cooldownMillis;
    private volatile int maxLength;
    private volatile boolean debug;
    private volatile List<String> blacklist = List.of();

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        long cooldownDays = plugin.getConfig().getLong("cooldown-days", 3L);
        this.cooldownMillis = TimeUnit.DAYS.toMillis(Math.max(0L, cooldownDays));
        this.maxLength = Math.max(1, plugin.getConfig().getInt("max-length", 32));
        this.debug = plugin.getConfig().getBoolean("debug", false);
        this.blacklist = plugin.getConfig().getStringList("blacklist").stream()
            .map(word -> word.toLowerCase(Locale.ROOT))
            .toList();
        reloadMessages();
    }

    private void reloadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        messages.clear();
        ConfigurationSection section = yaml.getConfigurationSection("messages");
        if (section == null) {
            plugin.getLogger().warning("messages.yml is missing the 'messages' section.");
            return;
        }
        for (String key : section.getKeys(false)) {
            String value = section.getString(key);
            if (value != null) {
                messages.put(key, value);
            }
        }
    }

    public long getCooldownMillis() {
        return cooldownMillis;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isBlacklisted(String plainText) {
        String lower = plainText.toLowerCase(Locale.ROOT);
        for (String word : blacklist) {
            if (lower.contains(word)) {
                return true;
            }
        }
        return false;
    }

    public Component message(String key) {
        return MiniMessageUtil.parse(raw(key));
    }

    public Component message(String key, String placeholder, String replacement) {
        return MiniMessageUtil.parse(raw(key).replace(placeholder, replacement));
    }

    private String raw(String key) {
        return messages.getOrDefault(key, "<red>Missing message: " + key);
    }
}
