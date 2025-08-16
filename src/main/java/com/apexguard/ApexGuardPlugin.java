package com.apexguard;

import com.apexguard.commands.ApexGuardCommand;
import com.apexguard.core.ApexGuard;
import com.apexguard.core.ConfigManager;
import com.apexguard.core.PlayerManager;
import com.apexguard.core.TaskEngine;
import com.apexguard.network.ProtocolBridge;
// import com.apexguard.network.ProtocolLibBridge;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class ApexGuardPlugin extends JavaPlugin {
    private ApexGuard apexGuard;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        final ConfigManager configManager = new ConfigManager(this);
        final TaskEngine taskEngine = new TaskEngine(configManager);
        final PlayerManager playerManager = new PlayerManager(this, taskEngine, configManager);

        ProtocolBridge protocolBridge = null;
        final Plugin protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        if (protocolLib != null && protocolLib.isEnabled()) {
            try {
                Class<?> bridgeClass = Class.forName("com.apexguard.network.ProtocolLibBridge");
                protocolBridge = (ProtocolBridge) bridgeClass
                        .getConstructor(JavaPlugin.class, TaskEngine.class, PlayerManager.class, ConfigManager.class)
                        .newInstance(this, taskEngine, playerManager, configManager);
                getLogger().info("ProtocolLib detected, packet-level features enabled.");
            } catch (Throwable t) {
                getLogger().warning("Failed to initialize ProtocolLib bridge reflectively. Running without packet capture. " + t.getMessage());
            }
        } else {
            getLogger().info("ProtocolLib not present; running in API-only mode with reduced detection.");
        }

        this.apexGuard = new ApexGuard(this, configManager, taskEngine, playerManager, protocolBridge);
        this.apexGuard.start();

        final ApexGuardCommand command = new ApexGuardCommand(apexGuard);
        getCommand("apexguard").setExecutor(command);
        getCommand("apexguard").setTabCompleter(command);
        getCommand("agstats").setExecutor(new com.apexguard.commands.StatsCommand(apexGuard));
    }

    @Override
    public void onDisable() {
        if (apexGuard != null) {
            apexGuard.stop();
        }
    }
}