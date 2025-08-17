package com.apexguard.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class ConfigManager {
    private final Plugin plugin;
    private final Logger logger;
    private final ObjectMapper jsonMapper;
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    
    // Advanced configuration sections
    private final Map<String, DetectionProfile> detectionProfiles = new HashMap<>();
    private final Map<String, PhysicsProfile> physicsProfiles = new HashMap<>();
    private final Map<String, MLProfile> mlProfiles = new HashMap<>();
    
    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.jsonMapper = new ObjectMapper();
        loadConfiguration();
    }
    
    public void loadConfiguration() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        
        // Load default configuration
        loadDefaults(config);
        
        // Load advanced profiles
        loadDetectionProfiles(config);
        loadPhysicsProfiles(config);
        loadMLProfiles(config);
        
        // Load Redis configuration
        loadRedisConfig(config);
        
        // Load performance settings
        loadPerformanceConfig(config);
        
        logger.info("Configuration loaded successfully");
    }
    
    private void loadDefaults(FileConfiguration config) {
        config.addDefault("general.debug", false);
        config.addDefault("general.log-level", "INFO");
        config.addDefault("general.auto-save-interval", 300);
        
        // Detection settings
        config.addDefault("detection.enabled", true);
        config.addDefault("detection.max-violations", 50);
        config.addDefault("detection.violation-decay", 0.95);
        config.addDefault("detection.risk-threshold", 0.8);
        
        // Action settings
        config.addDefault("actions.kick.enabled", true);
        config.addDefault("actions.ban.enabled", true);
        config.addDefault("actions.warn.enabled", true);
        config.addDefault("actions.slow.enabled", true);
        
        // Physics settings
        config.addDefault("physics.enabled", true);
        config.addDefault("physics.max-reach", 6.0);
        config.addDefault("physics.max-speed", 0.8);
        config.addDefault("physics.gravity", 0.08);
        
        // ML settings
        config.addDefault("ml.enabled", true);
        config.addDefault("ml.learning-rate", 0.01);
        config.addDefault("ml.batch-size", 100);
        config.addDefault("ml.epochs", 1000);
        
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }
    
    private void loadDetectionProfiles(FileConfiguration config) {
        ConfigurationSection profiles = config.getConfigurationSection("detection-profiles");
        if (profiles == null) return;
        
        for (String key : profiles.getKeys(false)) {
            ConfigurationSection profile = profiles.getConfigurationSection(key);
            if (profile != null) {
                DetectionProfile dp = new DetectionProfile();
                dp.name = key;
                dp.enabled = profile.getBoolean("enabled", true);
                dp.aggressive = profile.getBoolean("aggressive", false);
                dp.threshold = profile.getDouble("threshold", 0.8);
                dp.cooldown = profile.getLong("cooldown", 5000);
                detectionProfiles.put(key, dp);
            }
        }
    }
    
    private void loadPhysicsProfiles(FileConfiguration config) {
        ConfigurationSection profiles = config.getConfigurationSection("physics-profiles");
        if (profiles == null) return;
        
        for (String key : profiles.getKeys(false)) {
            ConfigurationSection profile = profiles.getConfigurationSection(key);
            if (profile != null) {
                PhysicsProfile pp = new PhysicsProfile();
                pp.name = key;
                pp.friction = profile.getDouble("friction", 0.6);
                pp.traction = profile.getDouble("traction", 0.8);
                pp.maxSpeed = profile.getDouble("max-speed", 0.8);
                pp.maxReach = profile.getDouble("max-reach", 6.0);
                physicsProfiles.put(key, pp);
            }
        }
    }
    
    private void loadMLProfiles(FileConfiguration config) {
        ConfigurationSection profiles = config.getConfigurationSection("ml-profiles");
        if (profiles == null) return;
        
        for (String key : profiles.getKeys(false)) {
            ConfigurationSection profile = profiles.getConfigurationSection(key);
            if (profile != null) {
                MLProfile mp = new MLProfile();
                mp.name = key;
                mp.learningRate = profile.getDouble("learning-rate", 0.01);
                mp.batchSize = profile.getInt("batch-size", 100);
                mp.epochs = profile.getInt("epochs", 1000);
                mp.autoTune = profile.getBoolean("auto-tune", true);
                mlProfiles.put(key, mp);
            }
        }
    }
    
    private void loadRedisConfig(FileConfiguration config) {
        config.addDefault("redis.enabled", false);
        config.addDefault("redis.host", "localhost");
        config.addDefault("redis.port", 6379);
        config.addDefault("redis.password", "");
        config.addDefault("redis.database", 0);
        config.addDefault("redis.timeout", 2000);
        config.addDefault("redis.max-connections", 10);
    }
    
    private void loadPerformanceConfig(FileConfiguration config) {
        config.addDefault("performance.max-threads", Runtime.getRuntime().availableProcessors());
        config.addDefault("performance.batch-size", 50);
        config.addDefault("performance.queue-size", 1000);
        config.addDefault("performance.memory-limit", 512);
    }
    
    // Configuration getters with caching
    public boolean profileBool(String check, String key, boolean defaultValue) {
        String cacheKey = check + "." + key;
        return (Boolean) cache.computeIfAbsent(cacheKey, k -> {
            FileConfiguration config = plugin.getConfig();
            ConfigurationSection section = config.getConfigurationSection("checks." + check);
            return section != null ? section.getBoolean(key, defaultValue) : defaultValue;
        });
    }
    
    public double profileDouble(String check, String key, double defaultValue) {
        String cacheKey = check + "." + key;
        return (Double) cache.computeIfAbsent(cacheKey, k -> {
            FileConfiguration config = plugin.getConfig();
            ConfigurationSection section = config.getConfigurationSection("checks." + check);
            return section != null ? section.getDouble(key, defaultValue) : defaultValue;
        });
    }
    
    public int profileInt(String check, String key, int defaultValue) {
        String cacheKey = check + "." + key;
        return (Integer) cache.computeIfAbsent(cacheKey, k -> {
            FileConfiguration config = plugin.getConfig();
            ConfigurationSection section = config.getConfigurationSection("checks." + check);
            return section != null ? section.getInt(key, defaultValue) : defaultValue;
        });
    }
    
    public String profileString(String check, String key, String defaultValue) {
        String cacheKey = check + "." + key;
        return (String) cache.computeIfAbsent(cacheKey, k -> {
            FileConfiguration config = plugin.getConfig();
            ConfigurationSection section = config.getConfigurationSection("checks." + check);
            return section != null ? section.getString(key, defaultValue) : defaultValue;
        });
    }
    
    // Advanced configuration getters
    public DetectionProfile getDetectionProfile(String name) {
        return detectionProfiles.getOrDefault(name, detectionProfiles.get("default"));
    }
    
    public PhysicsProfile getPhysicsProfile(String name) {
        return physicsProfiles.getOrDefault(name, physicsProfiles.get("default"));
    }
    
    public MLProfile getMLProfile(String name) {
        return mlProfiles.getOrDefault(name, mlProfiles.get("default"));
    }
    
    public boolean isRedisEnabled() {
        return plugin.getConfig().getBoolean("redis.enabled", false);
    }
    
    public String getRedisHost() {
        return plugin.getConfig().getString("redis.host", "localhost");
    }
    
    public int getRedisPort() {
        return plugin.getConfig().getInt("redis.port", 6379);
    }
    
    public String getRedisPassword() {
        return plugin.getConfig().getString("redis.password", "");
    }
    
    public int getRedisDatabase() {
        return plugin.getConfig().getInt("redis.database", 0);
    }
    
    public int getRedisTimeout() {
        return plugin.getConfig().getInt("redis.timeout", 2000);
    }
    
    public int getRedisMaxConnections() {
        return plugin.getConfig().getInt("redis.max-connections", 10);
    }
    
    public int getMaxThreads() {
        return plugin.getConfig().getInt("performance.max-threads", Runtime.getRuntime().availableProcessors());
    }
    
    public int getBatchSize() {
        return plugin.getConfig().getInt("performance.batch-size", 50);
    }
    
    public int getQueueSize() {
        return plugin.getConfig().getInt("performance.queue-size", 1000);
    }
    
    public int getMemoryLimit() {
        return plugin.getConfig().getInt("performance.memory-limit", 512);
    }
    
    public void clearCache() {
        cache.clear();
    }
    
    // Configuration classes
    public static class DetectionProfile {
        public String name;
        public boolean enabled;
        public boolean aggressive;
        public double threshold;
        public long cooldown;
    }
    
    public static class PhysicsProfile {
        public String name;
        public double friction;
        public double traction;
        public double maxSpeed;
        public double maxReach;
    }
    
    public static class MLProfile {
        public String name;
        public double learningRate;
        public int batchSize;
        public int epochs;
        public boolean autoTune;
    }
}