package com.apexguard.core;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ViolationTracker {
    private final Map<UUID, Double> vl = new ConcurrentHashMap<>();
    private final double halfLifeSeconds;

    public ViolationTracker(double halfLifeSeconds) {
        this.halfLifeSeconds = Math.max(5.0, halfLifeSeconds);
    }

    public void add(UUID uuid, double amount) {
        vl.merge(uuid, amount, Double::sum);
    }

    public double get(UUID uuid) { return vl.getOrDefault(uuid, 0.0); }

    public void decayAll(double dtSeconds) {
        if (dtSeconds <= 0) return;
        double decay = Math.pow(0.5, dtSeconds / halfLifeSeconds);
        for (Map.Entry<UUID, Double> e : vl.entrySet()) {
            double v = e.getValue() * decay;
            if (v < 0.01) vl.remove(e.getKey()); else e.setValue(v);
        }
    }

    public void reset(UUID uuid) { vl.remove(uuid); }
}