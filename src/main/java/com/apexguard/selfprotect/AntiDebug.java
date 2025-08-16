package com.apexguard.selfprotect;

import com.apexguard.core.ConfigManager;
import org.bukkit.plugin.Plugin;

public final class AntiDebug {
    private final Plugin plugin;
    private final ConfigManager config;

    public AntiDebug(Plugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void scanAtStartup() {
        if (!plugin.getConfig().getBoolean("self_protection.anti-debug", true)) return;
        String inputArgs = System.getProperty("sun.jvm.args", "");
        String vmInfo = System.getProperty("java.vm.info", "");
        boolean jdwp = inputArgs.contains("-agentlib:jdwp") || vmInfo.toLowerCase().contains("debug");
        if (jdwp) {
            plugin.getLogger().warning("Debugger indicators detected (JDWP). Running in hardened mode.");
        }
    }
}