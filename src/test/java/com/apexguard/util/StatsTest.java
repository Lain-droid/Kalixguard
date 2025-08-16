package com.apexguard.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StatsTest {
    @Test
    void testZScore() {
        double z = Stats.zScore(15, 10, 2);
        assertEquals(2.5, z, 1e-6);
    }

    @Test
    void testRollingDoubleWindow() {
        RollingDoubleWindow w = new RollingDoubleWindow(3);
        w.add(1); w.add(2); w.add(3);
        assertEquals(3, w.size());
        assertEquals(2.0, w.mean(), 1e-6);
        w.add(4);
        assertEquals(3, w.size());
        assertEquals(3.0, w.mean(), 1e-6);
    }

    @Test
    void testGcdConsistency() {
        double[] arr = {1.5, 3.0, 4.5, 6.0};
        double c = Stats.gcdConsistency(arr);
        assertTrue(c > 0.5);
    }
}