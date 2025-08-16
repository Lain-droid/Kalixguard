package com.apexguard.listeners;

import com.apexguard.checks.registry.CheckRegistry;
import com.apexguard.core.PlayerManager;
import com.apexguard.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;

public final class CombatListener implements Listener {
    private final PlayerManager playerManager;
    private final CheckRegistry registry;

    public CombatListener(PlayerManager playerManager, CheckRegistry registry) {
        this.playerManager = playerManager;
        this.registry = registry;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwing(PlayerAnimationEvent e) {
        Player player = e.getPlayer();
        PlayerData data = playerManager.getPlayerData(player.getUniqueId());
        long nowMs = System.currentTimeMillis();
        long last = data.getLastClickMs();
        if (last > 0) {
            long interval = Math.max(1L, nowMs - last);
            data.getClickIntervalsMs().add(interval);
        }
        data.setLastClickMs(nowMs);
        registry.onTickSync(data);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        PlayerData data = playerManager.getPlayerData(p.getUniqueId());
        registry.onTickSync(data);
    }
}