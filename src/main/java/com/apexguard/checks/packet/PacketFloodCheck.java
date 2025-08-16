package com.apexguard.checks.packet;

import com.apexguard.checks.api.Check;
import com.apexguard.checks.api.FlagContext;
import com.apexguard.core.ActionEngine;
import com.apexguard.core.ConfigManager;
import com.apexguard.logging.JsonLogger;
import com.apexguard.player.PlayerData;
import com.apexguard.network.PacketView;

import java.util.concurrent.TimeUnit;

public final class PacketFloodCheck implements Check {
    private final ConfigManager config;
    private final ActionEngine actions;
    private final JsonLogger logger;

    public PacketFloodCheck(ConfigManager config, ActionEngine actions, JsonLogger logger) {
        this.config = config;
        this.actions = actions;
        this.logger = logger;
    }

    private static final long WINDOW_NANOS = TimeUnit.SECONDS.toNanos(5);

    @Override
    public String name() { return "PacketFlood"; }

    @Override
    public String category() { return "packet"; }

    @Override
    public void handlePacketAsync(PlayerData data, PacketView packet) {
        // Placeholder: utilize replay metrics outside checks
    }
}