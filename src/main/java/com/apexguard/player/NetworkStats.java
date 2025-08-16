package com.apexguard.player;

public final class NetworkStats {
    private double rttEwmaMs;
    private double jitterEwmaMs;
    private boolean initialized;

    public synchronized void observeRtt(long rttMs) {
        double alpha = 0.25;
        if (!initialized) {
            rttEwmaMs = rttMs;
            jitterEwmaMs = 0;
            initialized = true;
            return;
        }
        double prev = rttEwmaMs;
        rttEwmaMs = alpha * rttMs + (1 - alpha) * rttEwmaMs;
        jitterEwmaMs = alpha * Math.abs(rttEwmaMs - prev) + (1 - alpha) * jitterEwmaMs;
    }

    public synchronized double getRttMs() { return rttEwmaMs; }
    public synchronized double getJitterMs() { return jitterEwmaMs; }
    public synchronized boolean isInitialized() { return initialized; }
}