package com.apexguard.network;

public interface PacketView {
    String type();
    default long getLong(String key, long def) { return def; }
    default int getInt(String key, int def) { return def; }
}