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
    void testMean() {
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        double mean = Stats.mean(values);
        assertEquals(3.0, mean, 1e-6);
    }

    @Test
    void testMedian() {
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        double median = Stats.median(values);
        assertEquals(3.0, median, 1e-6);
    }

    @Test
    void testVariance() {
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        double variance = Stats.variance(values);
        assertEquals(2.5, variance, 1e-6); // Sample variance: sum((x-mean)²)/(n-1)
    }

    @Test
    void testStandardDeviation() {
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        double stdDev = Stats.standardDeviation(values);
        assertEquals(Math.sqrt(2.5), stdDev, 1e-6); // Sample std dev: √(sample variance)
    }

    @Test
    void testRobustZScore() {
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 100.0}; // outlier
        double robustZ = Stats.robustZScore(100.0, values);
        assertTrue(robustZ > 2.0); // Should detect outlier
    }
}