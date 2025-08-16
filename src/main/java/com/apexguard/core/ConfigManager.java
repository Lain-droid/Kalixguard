package com.apexguard.core;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ConfigManager {
    private final Plugin plugin;
    private final FileConfiguration config;

    private final Map<String, Map<String, Object>> mergedProfiles = new HashMap<>();

    public ConfigManager(final Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        mergedProfiles.clear();
        loadProfiles();
    }

    private void loadProfiles() {
        final ConfigurationSection profiles = config.getConfigurationSection("profiles");
        if (profiles == null) return;
        for (String key : profiles.getKeys(false)) {
            mergeProfile(key, profiles);
        }
    }

    private Map<String, Object> mergeProfile(final String name, final ConfigurationSection profiles) {
        if (mergedProfiles.containsKey(name)) return mergedProfiles.get(name);
        final ConfigurationSection section = profiles.getConfigurationSection(name);
        if (section == null) return Map.of();
        Map<String, Object> base = new HashMap<>();
        if (section.contains("inherit")) {
            String parent = Objects.toString(section.get("inherit"));
            base.putAll(mergeProfile(parent, profiles));
        }
        ConfigurationSection checks = section.getConfigurationSection("checks");
        if (checks != null) {
            for (String checkName : checks.getKeys(false)) {
                ConfigurationSection c = checks.getConfigurationSection(checkName);
                if (c == null) continue;
                Map<String, Object> map = new HashMap<>();
                for (String k : c.getKeys(false)) {
                    map.put(k, c.get(k));
                }
                base.put(checkName, map);
            }
        }
        mergedProfiles.put(name, base);
        return base;
    }

    public String getActiveProfileName() {
        return config.getString("general.profile", "safe_default");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getActiveProfile() {
        return mergedProfiles.getOrDefault(getActiveProfileName(), Map.of());
    }

    public boolean isJsonLogging() { return config.getBoolean("general.json-logging", true); }

    public boolean isVerboseDefault() { return config.getBoolean("general.verbose-default", false); }

    public int getReplaySeconds() { return config.getInt("general.replay-seconds", 10); }

    public long getMaxReplayBytesPerPlayer() { return config.getLong("general.max-replay-bytes-per-player", 1_048_576L); }

    public int getWorkerThreads() { return config.getInt("performance.worker-threads", -1); }

    public int getMaxQueue() { return config.getInt("performance.max-queue", 32768); }

    public double getTpsLow() { return config.getDouble("performance.adaptive.tps-low", 18.5); }

    public double getRelaxFactor() { return config.getDouble("performance.adaptive.relax-factor", 1.25); }

    public boolean requireProtocolLib() { return config.getBoolean("compatibility.require-protocollib", false); }

    public boolean isGeyserAware() { return config.getBoolean("compatibility.geyser-floodgate-aware", true); }

    public double profileDouble(String check, String key, double def) {
        Object v = asMap(getActiveProfile().get(check)).get(key);
        return v instanceof Number ? ((Number) v).doubleValue() : def;
    }

    public int profileInt(String check, String key, int def) {
        Object v = asMap(getActiveProfile().get(check)).get(key);
        return v instanceof Number ? ((Number) v).intValue() : def;
    }

    public boolean profileBool(String check, String key, boolean def) {
        Object v = asMap(getActiveProfile().get(check)).get(key);
        return v instanceof Boolean ? (Boolean) v : def;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : Map.of();
    }

    public int actionFlag() { return plugin.getConfig().getInt("general.actions.flag-threshold", 5); }
    public int actionWarn() { return plugin.getConfig().getInt("general.actions.warn-threshold", 15); }
    public int actionSlow() { return plugin.getConfig().getInt("general.actions.slow-threshold", 25); }
    public int actionKick() { return plugin.getConfig().getInt("general.actions.kick-threshold", 45); }
    public boolean isBanEnabled() { return plugin.getConfig().getBoolean("general.actions.ban-enabled", false); }
}