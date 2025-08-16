package com.apexguard.checks.packet;

import com.apexguard.checks.api.Check;
import com.apexguard.checks.api.FlagContext;
import com.apexguard.core.ActionEngine;
import com.apexguard.core.ConfigManager;
import com.apexguard.logging.JsonLogger;
import com.apexguard.player.PlayerData;
import com.apexguard.player.NetworkStats;
import com.apexguard.network.PacketView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class KeepAliveSpoofCheck implements Check {
    private final ConfigManager config;
    private final ActionEngine actions;
    private final JsonLogger logger;

    private final Map<Long, Long> sentTimes = new ConcurrentHashMap<>();

    public KeepAliveSpoofCheck(ConfigManager config, ActionEngine actions, JsonLogger logger) {
        this.config = config;
        this.actions = actions;
        this.logger = logger;
    }

    @Override
    public String name() { return "KeepAliveSpoof"; }

    @Override
    public String category() { return "network"; }

    @Override
    public void handlePacketAsync(PlayerData data, PacketView packet) {
        String t = packet.type();
        if (t.endsWith("KEEP_ALIVE") && t.contains("SERVER")) {
            long id = packet.getLong("id", packet.getInt("id", 0));
            sentTimes.put(id, System.nanoTime());
        } else if (t.endsWith("KEEP_ALIVE") && t.contains("CLIENT")) {
            long id = packet.getLong("id", packet.getInt("id", 0));
            Long sent = sentTimes.remove(id);
            if (sent == null) return;
            long rttMs = Math.max(0L, (System.nanoTime() - sent) / 1_000_000L);
            NetworkStats ns = data.getNetwork();
            ns.observeRtt(rttMs);
            long minRtt = (long) config.profileDouble(name(), "min-rtt-ms", 25);
            long negAllowance = (long) config.profileDouble(name(), "negative-rtt-allowance-ms", 2);
            if (rttMs + negAllowance < 0 || (ns.isInitialized() && ns.getRttMs() > 0 && rttMs < ns.getRttMs() * 0.2 && ns.getRttMs() > minRtt)) {
                double sev = (ns.getRttMs() - rttMs) / Math.max(1.0, ns.getRttMs());
                actions.flag(data.getUuid(), new FlagContext(name(), category(), sev).with("rtt", rttMs).with("baseline", ns.getRttMs()));
            }
        }
    }
}