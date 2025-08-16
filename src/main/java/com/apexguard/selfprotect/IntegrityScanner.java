package com.apexguard.selfprotect;

import com.apexguard.core.ConfigManager;
import com.apexguard.core.TaskEngine;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class IntegrityScanner {
    private final Plugin plugin;
    private final ConfigManager config;
    private volatile String baselineSha;

    public IntegrityScanner(Plugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void start(TaskEngine taskEngine) {
        if (!plugin.getConfig().getBoolean("self_protection.integrity-scan", true)) return;
        taskEngine.submit(() -> baselineSha = hashJar());
    }

    public void stop() {
        if (!plugin.getConfig().getBoolean("self_protection.integrity-scan", true)) return;
        String current = hashJar();
        if (baselineSha != null && !baselineSha.equals(current)) {
            plugin.getLogger().warning("Plugin jar hash changed during runtime. Possible tampering.");
        }
    }

    private String hashJar() {
        File file = new File(plugin.getDataFolder().getParentFile(), plugin.getName() + ".jar");
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int r;
            while ((r = fis.read(buf)) != -1) md.update(buf, 0, r);
            return HexFormat.of().formatHex(md.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            plugin.getLogger().warning("Failed to hash jar: " + e.getMessage());
            return "";
        }
    }
}