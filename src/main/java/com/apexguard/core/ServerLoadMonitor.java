package com.apexguard.core;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.Plugin;

public final class ServerLoadMonitor {
    private final Plugin plugin;

    private volatile double tickMsEwma = 50.0;
    private volatile double lagFactor = 1.0;
    private BukkitTask task;

    public ServerLoadMonitor(Plugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        final double alpha = 0.1;
        task = new BukkitRunnable() {
            long last = System.nanoTime();
            @Override public void run() {
                long now = System.nanoTime();
                long dt = now - last; last = now;
                double ms = dt / 1_000_000.0;
                tickMsEwma = alpha * ms + (1 - alpha) * tickMsEwma;
                lagFactor = Math.max(1.0, tickMsEwma / 50.0);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    public double getTickMs() { return tickMsEwma; }
    public double getLagFactor() { return lagFactor; }
}