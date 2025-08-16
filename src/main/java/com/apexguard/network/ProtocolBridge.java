package com.apexguard.network;

import com.apexguard.checks.registry.CheckRegistry;
import com.apexguard.core.PlayerManager;
import com.apexguard.logging.JsonLogger;

public interface ProtocolBridge {
    void register(CheckRegistry registry, PlayerManager playerManager, JsonLogger logger);
    void shutdown();
}