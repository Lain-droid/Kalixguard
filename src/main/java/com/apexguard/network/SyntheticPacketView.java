package com.apexguard.network;

import java.util.Map;

public final class SyntheticPacketView implements PacketView {
    private final String type;
    private final Map<String, Object> attrs;

    public SyntheticPacketView(String type, Map<String, Object> attrs) {
        this.type = type;
        this.attrs = attrs;
    }

    @Override
    public String type() { return type; }

    @Override
    public long getLong(String key, long def) {
        Object v = attrs.get(key);
        if (v instanceof Number) return ((Number) v).longValue();
        return def;
    }

    @Override
    public int getInt(String key, int def) {
        Object v = attrs.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        return def;
    }
}