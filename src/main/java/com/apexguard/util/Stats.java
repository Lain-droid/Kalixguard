package com.apexguard.util;

public final class Stats {
    private Stats() {}

    public static double zScore(double value, double mean, double stddev) {
        if (stddev <= 1e-9) return 0.0;
        return (value - mean) / stddev;
    }

    public static double coefficientOfVariation(double mean, double stddev) {
        if (mean <= 1e-9) return 0.0;
        return stddev / mean;
    }

    public static double gcdDegrees(double a, double b) {
        // Work in thousandths of a degree to avoid FP artifacts
        long x = Math.round(Math.abs(a) * 1000.0);
        long y = Math.round(Math.abs(b) * 1000.0);
        if (x == 0) return y / 1000.0;
        if (y == 0) return x / 1000.0;
        while (y != 0) {
            long t = x % y;
            x = y;
            y = t;
        }
        return x / 1000.0;
    }

    public static double gcdConsistency(double[] deltas) {
        if (deltas.length < 3) return 0.0;
        double base = Math.abs(deltas[0]);
        double consistent = 0;
        for (int i = 1; i < deltas.length; i++) {
            double g = gcdDegrees(base, Math.abs(deltas[i]));
            if (g > 0.0 && (Math.abs(deltas[i]) / g) % 1.0 < 1e-3) consistent++;
            base = g;
        }
        return consistent / (deltas.length - 1);
    }
}