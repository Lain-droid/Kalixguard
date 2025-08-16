package com.apexguard.checks.inventory;

import com.apexguard.checks.api.Check;
import com.apexguard.checks.api.FlagContext;
import com.apexguard.core.ActionEngine;
import com.apexguard.core.ConfigManager;
import com.apexguard.logging.JsonLogger;
import com.apexguard.network.PacketView;
import com.apexguard.player.PlayerData;

public final class FastUsePlaceCheck implements Check {
    private final ConfigManager config;
    private final ActionEngine actions;
    private final JsonLogger logger;

    public FastUsePlaceCheck(ConfigManager config, ActionEngine actions, JsonLogger logger) {
        this.config = config;
        this.actions = actions;
        this.logger = logger;
    }

    @Override public String name() { return "FastUsePlace"; }
    @Override public String category() { return "inventory"; }

    @Override
    public void handlePacketAsync(PlayerData data, PacketView packet) {
        // not used; driven by sync events
    }

    @Override
    public void handleTickSync(PlayerData data) {
        if (!config.profileBool(name(), "enabled", true)) return;
        long[] use = data.getUseIntervalsMs().toArray();
        long[] place = data.getPlaceIntervalsMs().toArray();
        evaluate(data, use, "use");
        evaluate(data, place, "place");
    }

    private void evaluate(PlayerData data, long[] intervals, String kind) {
        if (intervals.length < 10) return;
        double mean = 0.0; for (long v : intervals) mean += v; mean /= intervals.length;
        double std = 0.0; for (long v : intervals) std += (v - mean) * (v - mean); std = Math.sqrt(std / intervals.length);
        double minInterval = mean - 2.0 * std;
        double limit = Math.max(60.0, minInterval);
        double latest = intervals[intervals.length - 1];
        if (latest < limit) {
            double sev = (limit - latest) / Math.max(1.0, limit);
            actions.flag(data.getUuid(), new FlagContext(name(), category(), sev).with("kind", kind).with("interval", latest));
        }
    }
}