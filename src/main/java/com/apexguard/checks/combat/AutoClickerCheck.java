package com.apexguard.checks.combat;

import com.apexguard.checks.api.Check;
import com.apexguard.checks.api.FlagContext;
import com.apexguard.core.ActionEngine;
import com.apexguard.core.ConfigManager;
import com.apexguard.logging.JsonLogger;
import com.apexguard.player.PlayerData;
import com.apexguard.util.Stats;
import com.apexguard.network.PacketView;

public final class AutoClickerCheck implements Check {
    private final ConfigManager config;
    private final ActionEngine actions;
    private final JsonLogger logger;

    public AutoClickerCheck(ConfigManager config, ActionEngine actions, JsonLogger logger) {
        this.config = config;
        this.actions = actions;
        this.logger = logger;
    }

    @Override
    public String name() { return "AutoClicker"; }

    @Override
    public String category() { return "combat"; }

    @Override
    public void handlePacketAsync(PlayerData data, PacketView packet) {
        String t = packet.type();
        if (!(t.endsWith("ARM_ANIMATION") || t.endsWith("USE_ENTITY"))) return;

        long nowMs = System.currentTimeMillis();
        long last = data.getLastClickMs();
        if (last > 0) {
            long interval = Math.max(1L, nowMs - last);
            data.getClickIntervalsMs().add(interval);
        }
        data.setLastClickMs(nowMs);
        evaluate(data);
    }

    private void evaluate(PlayerData data) {
        if (!config.profileBool(name(), "enabled", true)) return;
        long[] intervals = data.getClickIntervalsMs().toArray();
        if (intervals.length < 20) return;

        double mean = 0.0;
        for (long v : intervals) mean += v;
        mean /= intervals.length;
        double variance = 0.0;
        for (long v : intervals) variance += (v - mean) * (v - mean);
        variance /= intervals.length;
        double std = Math.sqrt(variance);

        double cps = 1000.0 / Math.max(1.0, mean);
        double cv = Stats.coefficientOfVariation(mean, std);

        double minCps = config.profileDouble(name(), "min-cps", 10.5);
        double cvTh = config.profileDouble(name(), "cv-threshold", 0.055);

        if (cps >= minCps && cv <= cvTh) {
            double severity = (cps - minCps) * 0.5 + (cvTh - cv) * 20.0;
            actions.flag(data.getUuid(), new FlagContext(name(), category(), severity)
                    .with("cps", cps).with("cv", cv));
        }
    }
}