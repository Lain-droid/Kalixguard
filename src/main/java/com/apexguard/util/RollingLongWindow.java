package com.apexguard.util;

import java.util.ArrayDeque;
import java.util.Deque;

public final class RollingLongWindow {
    private final int capacity;
    private final Deque<Long> deque = new ArrayDeque<>();
    private long sum;

    public RollingLongWindow(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    public void add(long value) {
        deque.addLast(value);
        sum += value;
        while (deque.size() > capacity) {
            sum -= deque.removeFirst();
        }
    }

    public int size() { return deque.size(); }
    public boolean isFull() { return deque.size() >= capacity; }
    public double mean() { return deque.isEmpty() ? 0.0 : (double) sum / deque.size(); }

    public long[] toArray() {
        long[] arr = new long[deque.size()];
        int i = 0;
        for (Long l : deque) arr[i++] = l;
        return arr;
    }

    public void clear() {
        deque.clear();
        sum = 0L;
    }
}