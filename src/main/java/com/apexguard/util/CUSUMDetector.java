package com.apexguard.util;

import com.google.common.util.concurrent.AtomicDouble;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CUSUM (Cumulative Sum) detector for detecting changes in statistical processes
 * Implements both upper and lower CUSUM charts for detecting positive and negative shifts
 */
public final class CUSUMDetector {
    private final double k; // Reference value (half the shift to detect)
    private final double h; // Decision interval (threshold for alarm)
    
    private final AtomicDouble upperCUSUM; // Upper CUSUM statistic
    private final AtomicDouble lowerCUSUM; // Lower CUSUM statistic
    private final AtomicDouble mean; // Current mean estimate
    private final AtomicDouble variance; // Current variance estimate
    private final AtomicLong count; // Number of observations
    private final AtomicLong lastAlarmTime; // Time of last alarm
    private final AtomicLong alarmCount; // Total number of alarms
    
    /**
     * Creates a new CUSUM detector
     * @param k Reference value (half the shift to detect)
     * @param h Decision interval (threshold for alarm)
     */
    public CUSUMDetector(double k, double h) {
        this.k = k;
        this.h = h;
        this.upperCUSUM = new AtomicDouble(0.0);
        this.lowerCUSUM = new AtomicDouble(0.0);
        this.mean = new AtomicDouble(0.0);
        this.variance = new AtomicDouble(0.0);
        this.count = new AtomicLong(0);
        this.lastAlarmTime = new AtomicLong(0);
        this.alarmCount = new AtomicLong(0);
    }
    
    /**
     * Adds a new observation and updates the CUSUM statistics
     * @param value The new observation value
     * @return true if an alarm is triggered
     */
    public boolean add(double value) {
        long currentCount = count.get();
        
        if (currentCount == 0) {
            // First observation
            mean.set(value);
            variance.set(0.0);
            upperCUSUM.set(0.0);
            lowerCUSUM.set(0.0);
        } else {
            // Update mean and variance using Welford's online algorithm
            double oldMean = mean.get();
            double newMean = oldMean + (value - oldMean) / (currentCount + 1);
            mean.set(newMean);
            
            double oldVariance = variance.get();
            double delta = value - oldMean;
            double newVariance = oldVariance + delta * (value - newMean);
            variance.set(newVariance);
            
            // Update CUSUM statistics
            double standardizedValue = (value - newMean) / Math.sqrt(newVariance / currentCount);
            
            // Upper CUSUM (detects positive shifts)
            double upperShift = standardizedValue - k;
            double newUpperCUSUM = Math.max(0, upperCUSUM.get() + upperShift);
            upperCUSUM.set(newUpperCUSUM);
            
            // Lower CUSUM (detects negative shifts)
            double lowerShift = -standardizedValue - k;
            double newLowerCUSUM = Math.max(0, lowerCUSUM.get() + lowerShift);
            lowerCUSUM.set(newLowerCUSUM);
        }
        
        count.incrementAndGet();
        
        // Check for alarms
        boolean alarm = false;
        if (upperCUSUM.get() >= h || lowerCUSUM.get() >= h) {
            alarm = true;
            lastAlarmTime.set(System.currentTimeMillis());
            alarmCount.incrementAndGet();
        }
        
        return alarm;
    }
    
    /**
     * Adds a new observation with a known mean and standard deviation
     * @param value The new observation value
     * @param knownMean The known mean of the process
     * @param knownStdDev The known standard deviation of the process
     * @return true if an alarm is triggered
     */
    public boolean add(double value, double knownMean, double knownStdDev) {
        if (knownStdDev <= 0) {
            throw new IllegalArgumentException("Standard deviation must be positive");
        }
        
        // Standardize the value
        double standardizedValue = (value - knownMean) / knownStdDev;
        
        // Update CUSUM statistics
        double upperShift = standardizedValue - k;
        double newUpperCUSUM = Math.max(0, upperCUSUM.get() + upperShift);
        upperCUSUM.set(newUpperCUSUM);
        
        double lowerShift = -standardizedValue - k;
        double newLowerCUSUM = Math.max(0, lowerCUSUM.get() + lowerShift);
        lowerCUSUM.set(newLowerCUSUM);
        
        count.incrementAndGet();
        
        // Check for alarms
        boolean alarm = false;
        if (upperCUSUM.get() >= h || lowerCUSUM.get() >= h) {
            alarm = true;
            lastAlarmTime.set(System.currentTimeMillis());
            alarmCount.incrementAndGet();
        }
        
        return alarm;
    }
    
    /**
     * Gets the current upper CUSUM statistic
     * @return The upper CUSUM value
     */
    public double getUpperCUSUM() {
        return upperCUSUM.get();
    }
    
    /**
     * Gets the current lower CUSUM statistic
     * @return The lower CUSUM value
     */
    public double getLowerCUSUM() {
        return lowerCUSUM.get();
    }
    
    /**
     * Gets the maximum of the upper and lower CUSUM statistics
     * @return The maximum CUSUM value
     */
    public double getMaxCUSUM() {
        return Math.max(upperCUSUM.get(), lowerCUSUM.get());
    }
    
    /**
     * Gets the current mean estimate
     * @return The current mean
     */
    public double getMean() {
        return mean.get();
    }
    
    /**
     * Gets the current variance estimate
     * @return The current variance
     */
    public double getVariance() {
        return variance.get();
    }
    
    /**
     * Gets the current standard deviation estimate
     * @return The current standard deviation
     */
    public double getStandardDeviation() {
        return Math.sqrt(variance.get());
    }
    
    /**
     * Gets the number of observations
     * @return The count of observations
     */
    public long getCount() {
        return count.get();
    }
    
    /**
     * Gets the time of the last alarm
     * @return The timestamp of the last alarm, or 0 if no alarms
     */
    public long getLastAlarmTime() {
        return lastAlarmTime.get();
    }
    
    /**
     * Gets the total number of alarms
     * @return The count of alarms
     */
    public long getAlarmCount() {
        return alarmCount.get();
    }
    
    /**
     * Gets the reference value (k)
     * @return The reference value
     */
    public double getK() {
        return k;
    }
    
    /**
     * Gets the decision interval (h)
     * @return The decision interval
     */
    public double getH() {
        return h;
    }
    
    /**
     * Checks if an alarm is currently active
     * @return true if either CUSUM statistic exceeds the threshold
     */
    public boolean isAlarmActive() {
        return upperCUSUM.get() >= h || lowerCUSUM.get() >= h;
    }
    
    /**
     * Resets the CUSUM detector to its initial state
     */
    public void reset() {
        upperCUSUM.set(0.0);
        lowerCUSUM.set(0.0);
        mean.set(0.0);
        variance.set(0.0);
        count.set(0);
        lastAlarmTime.set(0);
        alarmCount.set(0);
    }
    
    /**
     * Gets the average run length (ARL) for a given shift size
     * @param shiftSize The size of the shift to detect
     * @return The average run length
     */
    public double getAverageRunLength(double shiftSize) {
        if (shiftSize == 0) {
            // ARL0 (false alarm rate)
            return Math.exp(2 * h * k) / (2 * k * k);
        } else {
            // ARL1 (detection rate)
            double delta = shiftSize / Math.sqrt(variance.get() / Math.max(1, count.get()));
            double adjustedK = k + delta;
            return Math.exp(2 * h * adjustedK) / (2 * adjustedK * adjustedK);
        }
    }
    
    /**
     * Gets the probability of detection for a given shift size
     * @param shiftSize The size of the shift to detect
     * @return The probability of detection
     */
    public double getDetectionProbability(double shiftSize) {
        double arl1 = getAverageRunLength(shiftSize);
        return 1.0 - Math.exp(-1.0 / arl1);
    }
    
    /**
     * Creates a copy of this CUSUM detector
     * @return A new CUSUM detector with the same state
     */
    public CUSUMDetector copy() {
        CUSUMDetector copy = new CUSUMDetector(k, h);
        copy.upperCUSUM.set(upperCUSUM.get());
        copy.lowerCUSUM.set(lowerCUSUM.get());
        copy.mean.set(mean.get());
        copy.variance.set(variance.get());
        copy.count.set(count.get());
        copy.lastAlarmTime.set(lastAlarmTime.get());
        copy.alarmCount.set(alarmCount.get());
        return copy;
    }
    
    @Override
    public String toString() {
        return String.format("CUSUM(k=%.2f, h=%.2f, upper=%.3f, lower=%.3f, mean=%.3f, n=%d, alarms=%d)", 
                           k, h, upperCUSUM.get(), lowerCUSUM.get(), mean.get(), count.get(), alarmCount.get());
    }
}