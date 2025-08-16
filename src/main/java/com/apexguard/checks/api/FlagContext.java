package com.apexguard.checks.api;

import java.util.HashMap;
import java.util.Map;

public final class FlagContext {
    public final String check;
    public final String category;
    public final double severity;
    public final Map<String, Object> meta = new HashMap<>();

    public FlagContext(String check, String category, double severity) {
        this.check = check;
        this.category = category;
        this.severity = severity;
    }

    public FlagContext with(String key, Object value) {
        meta.put(key, value);
        return this;
    }
}