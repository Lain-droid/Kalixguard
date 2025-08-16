package com.apexguard.listeners;

import com.apexguard.checks.registry.CheckRegistry;
import com.apexguard.core.PlayerManager;
import com.apexguard.player.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class MovementListener implements Listener {
    private final PlayerManager playerManager;
    private final CheckRegistry registry;

    public MovementListener(PlayerManager playerManager, CheckRegistry registry) {
        this.playerManager = playerManager;
        this.registry = registry;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom() == null || e.getTo() == null) return;
        if (!e.getFrom().getWorld().equals(e.getTo().getWorld())) return;
        PlayerData data = playerManager.getPlayerData(e.getPlayer().getUniqueId());

        float fromYaw = e.getFrom().getYaw();
        float toYaw = e.getTo().getYaw();
        float fromPitch = e.getFrom().getPitch();
        float toPitch = e.getTo().getPitch();
        double dx = e.getTo().getX() - e.getFrom().getX();
        double dz = e.getTo().getZ() - e.getFrom().getZ();
        double horizontal = Math.hypot(dx, dz);

        double dyaw = wrapDegrees(toYaw - fromYaw);
        double dpitch = wrapDegrees(toPitch - fromPitch);

        data.getYawDeltas().add(Math.abs(dyaw));
        data.getPitchDeltas().add(Math.abs(dpitch));
        data.getHorizontalSpeed().add(horizontal);

        registry.onTickSync(data);
    }

    private double wrapDegrees(double d) {
        while (d <= -180.0) d += 360.0;
        while (d > 180.0) d -= 360.0;
        return d;
    }
}