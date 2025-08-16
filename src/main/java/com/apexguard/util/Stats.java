package com.apexguard.util;

import java.util.Arrays;

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

    public static double median(double[] arr) {
        if (arr.length == 0) return 0.0;
        double[] copy = Arrays.copyOf(arr, arr.length);
        Arrays.sort(copy);
        int mid = copy.length / 2;
        if ((copy.length & 1) == 0) return 0.5 * (copy[mid - 1] + copy[mid]);
        return copy[mid];
    }

    public static double mad(double[] arr) {
        if (arr.length == 0) return 0.0;
        double med = median(arr);
        double[] dev = new double[arr.length];
        for (int i = 0; i < arr.length; i++) dev[i] = Math.abs(arr[i] - med);
        return median(dev) * 1.4826; // consistency constant
    }

    public static double percentile(double[] arr, double p) {
        if (arr.length == 0) return 0.0;
        double[] copy = Arrays.copyOf(arr, arr.length);
        Arrays.sort(copy);
        double rank = Math.max(0, Math.min(1, p)) * (copy.length - 1);
        int lo = (int) Math.floor(rank);
        int hi = (int) Math.ceil(rank);
        if (lo == hi) return copy[lo];
        double w = rank - lo;
        return copy[lo] * (1 - w) + copy[hi] * w;
    }

    public static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}