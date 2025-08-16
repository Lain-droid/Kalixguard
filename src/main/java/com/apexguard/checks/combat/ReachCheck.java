package com.apexguard.checks.combat;

import com.apexguard.checks.api.Check;
import com.apexguard.checks.api.FlagContext;
import com.apexguard.core.ActionEngine;
import com.apexguard.core.ConfigManager;
import com.apexguard.logging.JsonLogger;
import com.apexguard.player.PlayerData;
import com.apexguard.util.Stats;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public final class ReachCheck implements Check {
    private final ConfigManager config;
    private final ActionEngine actions;
    private final JsonLogger logger;

    public ReachCheck(ConfigManager config, ActionEngine actions, JsonLogger logger) {
        this.config = config;
        this.actions = actions;
        this.logger = logger;
    }

    @Override public String name() { return "Reach"; }
    @Override public String category() { return "combat"; }

    @Override
    public void handleCombatEvent(PlayerData attackerData, EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity)) return;
        Player attacker = (Player) event.getDamager();
        LivingEntity victim = (LivingEntity) event.getEntity();

        if (!config.profileBool(name(), "enabled", true)) return;

        double baseReach = 3.0;
        // Speed effect slightly increases reach due to momentum/latency
        double speedBonus = 0.0;
        AttributeInstance speedAttr = attacker.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) speedBonus += Stats.clamp((speedAttr.getValue() - speedAttr.getBaseValue()) * 2.0, 0.0, 0.4);

        double pingMs = attackerData.getNetwork().isInitialized() ? attackerData.getNetwork().getRttMs() : 50.0;
        double latencyBonus = Stats.clamp(pingMs / 200.0, 0.0, 0.6); // up to +0.6 at 120ms

        double allowed = baseReach + speedBonus + latencyBonus + config.profileDouble(name(), "tolerance", 0.2);

        Location eye = attacker.getEyeLocation();
        BoundingBox box = victim.getBoundingBox();
        double dist = distanceToBox(eye.toVector(), box);

        if (dist > allowed) {
            double sev = (dist - allowed);
            actions.flag(attacker.getUniqueId(), new FlagContext(name(), category(), sev)
                    .with("dist", dist).with("allowed", allowed));
        }
    }

    private double distanceToBox(Vector point, BoundingBox box) {
        double dx = Math.max(Math.max(box.getMinX() - point.getX(), 0), point.getX() - box.getMaxX());
        double dy = Math.max(Math.max(box.getMinY() - point.getY(), 0), point.getY() - box.getMaxY());
        double dz = Math.max(Math.max(box.getMinZ() - point.getZ(), 0), point.getZ() - box.getMaxZ());
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    @Override
    public void handlePacketAsync(PlayerData data, com.apexguard.network.PacketView packet) {
        // not used
    }
}