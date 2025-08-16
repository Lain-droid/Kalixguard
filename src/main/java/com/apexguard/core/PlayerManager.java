package com.apexguard.core;

import com.apexguard.checks.registry.CheckRegistry;
import com.apexguard.listeners.CombatListener;
import com.apexguard.listeners.MovementListener;
import com.apexguard.listeners.InventoryListener;
import com.apexguard.player.PlayerData;
import com.apexguard.player.ReplayBuffer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerManager implements Listener {
    private final Plugin plugin;
    private final TaskEngine taskEngine;
    private final ConfigManager configManager;

    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();

    public PlayerManager(final Plugin plugin, final TaskEngine taskEngine, final ConfigManager configManager) {
        this.plugin = plugin;
        this.taskEngine = taskEngine;
        this.configManager = configManager;
    }

    public void hookEvents(final CheckRegistry checkRegistry) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getPluginManager().registerEvents(new MovementListener(this, checkRegistry), plugin);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this, checkRegistry), plugin);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this, checkRegistry), plugin);
        checkRegistry.attachPlayerManager(this);
    }

    public void initializePlayer(UUID uuid) {
        players.computeIfAbsent(uuid, id -> new PlayerData(id, configManager));
    }

    public PlayerData getPlayerData(UUID uuid) {
        return players.computeIfAbsent(uuid, id -> new PlayerData(id, configManager));
    }

    public void removePlayer(UUID uuid) {
        PlayerData data = players.remove(uuid);
        if (data != null) {
            data.close();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        initializePlayer(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        removePlayer(e.getPlayer().getUniqueId());
    }

    public void shutdown() {
        for (PlayerData data : players.values()) {
            data.close();
        }
        players.clear();
    }

    public ReplayBuffer getReplay(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        return data.getReplayBuffer();
    }

    public Player getOnline(UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }
}