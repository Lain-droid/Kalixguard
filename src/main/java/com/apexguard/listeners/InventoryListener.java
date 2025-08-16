package com.apexguard.listeners;

import com.apexguard.checks.registry.CheckRegistry;
import com.apexguard.core.PlayerManager;
import com.apexguard.player.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public final class InventoryListener implements Listener {
    private final PlayerManager playerManager;
    private final CheckRegistry registry;

    public InventoryListener(PlayerManager playerManager, CheckRegistry registry) {
        this.playerManager = playerManager;
        this.registry = registry;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            PlayerData data = playerManager.getPlayerData(e.getPlayer().getUniqueId());
            long now = System.currentTimeMillis();
            long last = data.getLastUseMs();
            if (last > 0) data.getUseIntervalsMs().add(Math.max(1L, now - last));
            data.setLastUseMs(now);
            registry.onTickSync(data);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        PlayerData data = playerManager.getPlayerData(e.getPlayer().getUniqueId());
        long now = System.currentTimeMillis();
        long last = data.getLastPlaceMs();
        if (last > 0) data.getPlaceIntervalsMs().add(Math.max(1L, now - last));
        data.setLastPlaceMs(now);
        registry.onTickSync(data);
    }
}