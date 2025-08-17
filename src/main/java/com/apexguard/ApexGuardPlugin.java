package com.apexguard;

import com.apexguard.commands.ApexGuardCommand;
import com.apexguard.core.ApexGuard;
import com.apexguard.core.ConfigManager;
import com.apexguard.core.PlayerManager;
import com.apexguard.core.TaskEngine;
import com.apexguard.logging.Console;
import com.apexguard.network.ProtocolBridge;
// import com.apexguard.network.ProtocolLibBridge;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class ApexGuardPlugin extends JavaPlugin {
    private ApexGuard apexGuard;

    @Override
    public void onEnable() {
        Console.info(getServer().getConsoleSender(), "&d[ApexGuard] &7Starting...");
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
                        .getDeclaredConstructor(Plugin.class, TaskEngine.class, PlayerManager.class, ConfigManager.class)
                        .newInstance(this, taskEngine, playerManager, configManager);
                Console.success(getServer().getConsoleSender(), "&aProtocolLib detected, packet-level features enabled.");
            } catch (ClassNotFoundException cnf) {
                Console.warn(getServer().getConsoleSender(), "&eProtocolLib detected but bridge not bundled; running without packet capture.");
            } catch (Throwable t) {
                Console.error(getServer().getConsoleSender(), "&cProtocolLib present but bridge failed to initialize: " + t.getClass().getSimpleName());
            }
        } else {
            Console.warn(getServer().getConsoleSender(), "&eProtocolLib not present; running in API-only mode with reduced detection.");
        }

        this.apexGuard = new ApexGuard(this, configManager, taskEngine, playerManager, protocolBridge);
        try {
            this.apexGuard.start();
            Console.success(getServer().getConsoleSender(), "&d[ApexGuard] &aApex başarıyla çalıştı");
        } catch (Throwable t) {
            Console.error(getServer().getConsoleSender(), "&c[ApexGuard] Başlatma hatası: " + t.getClass().getSimpleName());
            throw t;
        }

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