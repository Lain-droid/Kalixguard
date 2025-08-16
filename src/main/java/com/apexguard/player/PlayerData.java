package com.apexguard.player;

import com.apexguard.core.ConfigManager;
import com.apexguard.util.RollingDoubleWindow;
import com.apexguard.util.RollingLongWindow;

import java.util.UUID;

public final class PlayerData implements AutoCloseable {
    private final UUID uuid;
    private final ReplayBuffer replayBuffer = new ReplayBuffer();
    private final NetworkStats networkStats = new NetworkStats();

    // Click intervals, rotation deltas, movement speeds
    private final RollingLongWindow clickIntervalsMs;
    private final RollingDoubleWindow yawDeltas;
    private final RollingDoubleWindow pitchDeltas;
    private final RollingDoubleWindow horizontalSpeed;

    private volatile long lastClickMs;

    public PlayerData(UUID uuid, ConfigManager config) {
        this.uuid = uuid;
        int window = Math.max(100, config.profileInt("AutoClicker", "window-ms", 6000) / 50);
        this.clickIntervalsMs = new RollingLongWindow(window);
        this.yawDeltas = new RollingDoubleWindow(120);
        this.pitchDeltas = new RollingDoubleWindow(120);
        this.horizontalSpeed = new RollingDoubleWindow(80);
    }

    public UUID getUuid() { return uuid; }
    public ReplayBuffer getReplayBuffer() { return replayBuffer; }
    public NetworkStats getNetwork() { return networkStats; }

    public RollingLongWindow getClickIntervalsMs() { return clickIntervalsMs; }
    public RollingDoubleWindow getYawDeltas() { return yawDeltas; }
    public RollingDoubleWindow getPitchDeltas() { return pitchDeltas; }
    public RollingDoubleWindow getHorizontalSpeed() { return horizontalSpeed; }

    public long getLastClickMs() { return lastClickMs; }
    public void setLastClickMs(long ts) { this.lastClickMs = ts; }

    @Override
    public void close() {
        replayBuffer.clear();
    }
}