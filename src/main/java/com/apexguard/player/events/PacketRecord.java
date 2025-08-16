package com.apexguard.player.events;

public final class PacketRecord {
    public final long nanoTime;
    public final PacketDirection direction;
    public final String type;
    public final int approxSize;

    public PacketRecord(long nanoTime, PacketDirection direction, String type, int approxSize) {
        this.nanoTime = nanoTime;
        this.direction = direction;
        this.type = type;
        this.approxSize = approxSize;
    }
}