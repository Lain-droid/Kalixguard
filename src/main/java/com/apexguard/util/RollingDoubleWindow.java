package com.apexguard.util;

import java.util.ArrayDeque;
import java.util.Deque;

public final class RollingDoubleWindow {
    private final int capacity;
    private final Deque<Double> deque = new ArrayDeque<>();
    private double sum;
    private double sumSq;

    public RollingDoubleWindow(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    public void add(double value) {
        deque.addLast(value);
        sum += value;
        sumSq += value * value;
        while (deque.size() > capacity) {
            double removed = deque.removeFirst();
            sum -= removed;
            sumSq -= removed * removed;
        }
    }

    public int size() { return deque.size(); }
    public boolean isFull() { return deque.size() >= capacity; }
    public double mean() { return deque.isEmpty() ? 0.0 : sum / deque.size(); }
    public double variance() {
        if (deque.isEmpty()) return 0.0;
        double mu = mean();
        return Math.max(0.0, sumSq / deque.size() - mu * mu);
    }
    public double stddev() { return Math.sqrt(variance()); }

    public double[] toArray() {
        double[] arr = new double[deque.size()];
        int i = 0;
        for (Double d : deque) arr[i++] = d;
        return arr;
    }

    public void clear() {
        deque.clear();
        sum = 0.0;
        sumSq = 0.0;
    }
}