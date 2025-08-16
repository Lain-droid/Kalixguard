package com.apexguard.player;

import com.apexguard.player.events.PacketRecord;

import java.util.ArrayDeque;
import java.util.Deque;

public final class ReplayBuffer {
    private final Deque<PacketRecord> deque = new ArrayDeque<>();
    private long approxBytes;

    public synchronized void append(PacketRecord record, long maxBytes) {
        deque.addLast(record);
        approxBytes += Math.max(32, record.approxSize);
        while (approxBytes > maxBytes && !deque.isEmpty()) {
            PacketRecord removed = deque.removeFirst();
            approxBytes -= Math.max(32, removed.approxSize);
        }
    }

    public synchronized PacketRecord[] snapshot() {
        return deque.toArray(new PacketRecord[0]);
    }

    public synchronized void clear() {
        deque.clear();
        approxBytes = 0L;
    }
}