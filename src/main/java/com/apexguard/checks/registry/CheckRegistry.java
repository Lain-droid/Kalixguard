package com.apexguard.checks.registry;

import com.apexguard.checks.api.Check;
import com.apexguard.core.ActionEngine;
import com.apexguard.core.ConfigManager;
import com.apexguard.core.PlayerManager;
import com.apexguard.logging.JsonLogger;
import com.apexguard.network.PacketView;
import com.apexguard.player.PlayerData;

import java.util.ArrayList;
import java.util.List;

public final class CheckRegistry {
    private final ConfigManager configManager;
    private final ActionEngine actionEngine;
    private final JsonLogger jsonLogger;
    private PlayerManager playerManager;

    private final List<Check> checks = new ArrayList<>();

    public CheckRegistry(ConfigManager configManager, ActionEngine actionEngine, JsonLogger jsonLogger) {
        this.configManager = configManager;
        this.actionEngine = actionEngine;
        this.jsonLogger = jsonLogger;
        registerDefaults();
    }

    public void attachPlayerManager(PlayerManager pm) { this.playerManager = pm; }

    private void registerDefaults() {
        checks.add(new com.apexguard.checks.combat.AutoClickerCheck(configManager, actionEngine, jsonLogger));
        checks.add(new com.apexguard.checks.combat.AimAssistCheck(configManager, actionEngine, jsonLogger));
        checks.add(new com.apexguard.checks.movement.SpeedCheck(configManager, actionEngine, jsonLogger));
        checks.add(new com.apexguard.checks.packet.PacketFloodCheck(configManager, actionEngine, jsonLogger));
        checks.add(new com.apexguard.checks.packet.KeepAliveSpoofCheck(configManager, actionEngine, jsonLogger));
    }

    public void onPacketAsync(PlayerData data, PacketView packet) {
        for (Check check : checks) {
            try {
                check.handlePacketAsync(data, packet);
            } catch (Throwable t) {
                // swallow and continue
            }
        }
    }

    public void onTickSync(PlayerData data) {
        for (Check check : checks) {
            try {
                check.handleTickSync(data);
            } catch (Throwable t) {
                // swallow
            }
        }
    }
}