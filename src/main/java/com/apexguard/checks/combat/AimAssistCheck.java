package com.apexguard.checks.combat;

import com.apexguard.checks.api.Check;
import com.apexguard.checks.api.FlagContext;
import com.apexguard.core.ActionEngine;
import com.apexguard.core.ConfigManager;
import com.apexguard.logging.JsonLogger;
import com.apexguard.player.PlayerData;
import com.apexguard.util.Stats;
import com.apexguard.network.PacketView;

public final class AimAssistCheck implements Check {
    private final ConfigManager config;
    private final ActionEngine actions;
    private final JsonLogger logger;

    public AimAssistCheck(ConfigManager config, ActionEngine actions, JsonLogger logger) {
        this.config = config;
        this.actions = actions;
        this.logger = logger;
    }

    @Override
    public String name() { return "AimAssist"; }

    @Override
    public String category() { return "combat"; }

    @Override
    public void handlePacketAsync(PlayerData data, PacketView packet) {
        String t = packet.type();
        if (!(t.endsWith("LOOK") || t.endsWith("POSITION_LOOK") || t.endsWith("FLYING"))) return;
        // Packet decoding omitted in scaffold
    }

    @Override
    public void handleTickSync(PlayerData data) {
        evaluate(data);
    }

    private void evaluate(PlayerData data) {
        if (!config.profileBool(name(), "enabled", true)) return;
        double[] yaw = data.getYawDeltas().stream().mapToDouble(Double::doubleValue).toArray();
        if (yaw.length < 40) return;
        double mean = 0.0; for (double v : yaw) mean += v; mean /= yaw.length;
        double var = 0.0; for (double v : yaw) var += (v - mean) * (v - mean); var /= yaw.length;
        double std = Math.sqrt(Math.max(0.0, var));

        double consistency = Stats.gcdConsistency(yaw);
        double jitterZ = Math.abs(Stats.zScore(std, 2.5, 1.0));

        double minRot = config.profileDouble(name(), "min-rotations", 60);
        double gcdCons = config.profileDouble(name(), "gcd-consistency", 0.985);
        double jitterZTh = config.profileDouble(name(), "jitter-zscore", 3.5);

        if (yaw.length >= minRot && consistency >= gcdCons && jitterZ <= jitterZTh) {
            double sev = (consistency - gcdCons) * 40.0 + (jitterZTh - jitterZ) * 3.0;
            actions.flag(data.getUuid(), new FlagContext(name(), category(), sev)
                    .with("consistency", consistency).with("jitterZ", jitterZ));
        }
    }
}