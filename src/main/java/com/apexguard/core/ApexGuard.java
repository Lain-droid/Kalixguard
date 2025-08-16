package com.apexguard.core;

import com.apexguard.network.ProtocolBridge;
import com.apexguard.player.PlayerData;
import com.apexguard.checks.registry.CheckRegistry;
import com.apexguard.logging.JsonLogger;
import com.apexguard.selfprotect.AntiDebug;
import com.apexguard.selfprotect.IntegrityScanner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public final class ApexGuard {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private final TaskEngine taskEngine;
    private final PlayerManager playerManager;
    private final ProtocolBridge protocolBridge;

    private final CheckRegistry checkRegistry;
    private final ActionEngine actionEngine;
    private final JsonLogger jsonLogger;
    private final IntegrityScanner integrityScanner;
    private final AntiDebug antiDebug;

    public ApexGuard(final Plugin plugin,
                     final ConfigManager configManager,
                     final TaskEngine taskEngine,
                     final PlayerManager playerManager,
                     final ProtocolBridge protocolBridge) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.taskEngine = taskEngine;
        this.playerManager = playerManager;
        this.protocolBridge = protocolBridge;

        this.jsonLogger = new JsonLogger(plugin, configManager);
        this.actionEngine = new ActionEngine(plugin, configManager, jsonLogger);
        this.checkRegistry = new CheckRegistry(configManager, actionEngine, jsonLogger);
        this.integrityScanner = new IntegrityScanner(plugin, configManager);
        this.antiDebug = new AntiDebug(plugin, configManager);
    }

    public void start() {
        if (protocolBridge != null) {
            protocolBridge.register(checkRegistry, playerManager, jsonLogger);
        }
        playerManager.hookEvents(checkRegistry);
        integrityScanner.start(taskEngine);
        antiDebug.scanAtStartup();

        // Prime online players (e.g., on /reload)
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerManager.initializePlayer(player.getUniqueId());
        }
    }

    public void stop() {
        if (protocolBridge != null) {
            protocolBridge.shutdown();
        }
        integrityScanner.stop();
        playerManager.shutdown();
        taskEngine.shutdown();
        jsonLogger.shutdown();
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerManager.getPlayerData(uuid);
    }

    public ConfigManager getConfigManager() { return configManager; }
    public TaskEngine getTaskEngine() { return taskEngine; }
    public ActionEngine getActionEngine() { return actionEngine; }
    public CheckRegistry getCheckRegistry() { return checkRegistry; }
    public JsonLogger getJsonLogger() { return jsonLogger; }
}