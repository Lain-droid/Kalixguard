package com.apexguard.network;

import com.comphenix.protocol.events.PacketContainer;

public final class ProtocolLibAdapter implements PacketView {
    private final PacketContainer packet;
    private final String type;
    private final boolean client;

    public ProtocolLibAdapter(PacketContainer packet, boolean client) {
        this.packet = packet;
        this.type = packet.getType().name();
        this.client = client;
    }

    @Override
    public String type() { return type + (client ? "_CLIENT" : "_SERVER"); }

    @Override
    public long getLong(String key, long def) {
        try {
            if (packet.getLongs().size() > 0) return packet.getLongs().read(0);
        } catch (Throwable ignored) {}
        return def;
    }

    @Override
    public int getInt(String key, int def) {
        try {
            if (packet.getIntegers().size() > 0) return packet.getIntegers().read(0);
        } catch (Throwable ignored) {}
        return def;
    }
}