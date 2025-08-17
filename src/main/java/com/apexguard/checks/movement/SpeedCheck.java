package com.apexguard.checks.movement;

import com.apexguard.checks.api.Check;
import com.apexguard.checks.api.FlagContext;
import com.apexguard.core.ActionEngine;
import com.apexguard.core.ConfigManager;
import com.apexguard.logging.JsonLogger;
import com.apexguard.player.PlayerData;
import com.apexguard.util.Stats;
import com.apexguard.network.PacketView;

public final class SpeedCheck implements Check {
    private final ConfigManager config;
    private final ActionEngine actions;
    private final JsonLogger logger;

    public SpeedCheck(ConfigManager config, ActionEngine actions, JsonLogger logger) {
        this.config = config;
        this.actions = actions;
        this.logger = logger;
    }

    @Override
    public String name() { return "Speed"; }

    @Override
    public String category() { return "movement"; }

    @Override
    public void handlePacketAsync(PlayerData data, PacketView packet) {
        String t = packet.type();
        if (!(t.endsWith("FLYING") || t.endsWith("POSITION") || t.endsWith("POSITION_LOOK"))) return;
        // Without decoding positions, we can't compute speed here. In real impl, parse positions.
    }

    @Override
    public void handleTickSync(PlayerData data) {
        evaluate(data);
    }

    private void evaluate(PlayerData data) {
        if (!config.profileBool(name(), "enabled", true)) return;
        double[] speeds = data.getHorizontalSpeed().stream().mapToDouble(Double::doubleValue).toArray();
        if (speeds.length < 20) return;
        double mean = 0.0; for (double v : speeds) mean += v; mean /= speeds.length;
        double var = 0.0; for (double v : speeds) var += (v - mean) * (v - mean); var /= speeds.length;
        double std = Math.sqrt(Math.max(1e-9, var));
        double latest = speeds[speeds.length - 1];
        double z = Stats.zScore(latest, mean, std);
        double zTh = config.profileDouble(name(), "z-threshold", 4.0);
        // Environment-aware relaxation
        double relax = 1.0;
        try {
            org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(data.getUuid());
            if (p != null) {
                relax = new com.apexguard.physics.PhysicsEngine().computeRelaxMultiplier(p);
            }
        } catch (Throwable ignored) {}
        zTh *= relax;
        if (z >= zTh) {
            double sev = (z - zTh) / Math.max(1.0, relax);
            actions.flag(data.getUuid(), new FlagContext(name(), category(), sev).with("z", z).with("relax", relax));
        }
    }
}