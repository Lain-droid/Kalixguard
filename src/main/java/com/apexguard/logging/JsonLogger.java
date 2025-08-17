package com.apexguard.logging;

import com.apexguard.core.ConfigManager;
import org.bukkit.plugin.Plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JsonLogger implements AutoCloseable {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private final File file;

    private BufferedWriter writer;

    private final Set<UUID> verbosePlayers = ConcurrentHashMap.newKeySet();

    public JsonLogger(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.file = new File(plugin.getDataFolder(), "apexguard.log.jsonl");
        try {
            plugin.getDataFolder().mkdirs();
            this.writer = Files.newBufferedWriter(
                    file.toPath(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to open log file: " + e.getMessage());
        }
    }

    public void setVerbose(UUID uuid, boolean value) {
        if (value) verbosePlayers.add(uuid); else verbosePlayers.remove(uuid);
    }

    public boolean isVerbose(UUID uuid) {
        return verbosePlayers.contains(uuid) || plugin.getConfig().getBoolean("general.debug", false);
    }

    public synchronized void log(Map<String, Object> event) {
        if (writer == null) return;
        String level = plugin.getConfig().getString("general.log-level", "INFO");
        if (level != null && level.equalsIgnoreCase("OFF")) return;
        try {
            writer.write(toJson(event));
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to write log: " + e.getMessage());
        }
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder(256);
        sb.append('{');
        sb.append("\"ts\":").append(Instant.now().toEpochMilli());
        for (Map.Entry<String, Object> e : map.entrySet()) {
            sb.append(',');
            sb.append('"').append(escape(e.getKey())).append('"').append(':');
            Object v = e.getValue();
            if (v == null) {
                sb.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                sb.append(v.toString());
            } else {
                sb.append('"').append(escape(String.valueOf(v))).append('"');
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public void close() {
        shutdown();
    }

    public synchronized void shutdown() {
        if (writer != null) {
            try { writer.close(); } catch (IOException ignored) {}
            writer = null;
        }
    }
}