package com.apexguard.util;

import com.google.common.util.concurrent.AtomicDouble;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe exponential moving average implementation
 */
public final class ExponentialMovingAverage {
    private final double alpha;
    private final AtomicDouble currentValue;
    private final AtomicLong count;
    private final AtomicDouble variance;
    
    /**
     * Creates a new exponential moving average with the specified decay factor
     * @param alpha The decay factor (0 < alpha < 1). Smaller values give more weight to older data.
     */
    public ExponentialMovingAverage(double alpha) {
        if (alpha <= 0 || alpha >= 1) {
            throw new IllegalArgumentException("Alpha must be between 0 and 1, got: " + alpha);
        }
        this.alpha = alpha;
        this.currentValue = new AtomicDouble(Double.NaN);
        this.count = new AtomicLong(0);
        this.variance = new AtomicDouble(0.0);
    }
    
    /**
     * Adds a new value to the moving average
     * @param value The new value to add
     */
    public void add(double value) {
        long currentCount = count.get();
        
        if (currentCount == 0) {
            // First value
            currentValue.set(value);
            variance.set(0.0);
        } else {
            // Update moving average
            double oldValue = currentValue.get();
            double newValue = oldValue + alpha * (value - oldValue);
            currentValue.set(newValue);
            
            // Update variance (using Welford's online algorithm)
            double oldVariance = variance.get();
            double delta = value - oldValue;
            double newVariance = oldVariance + (1.0 - alpha) * delta * delta;
            variance.set(newVariance);
        }
        
        count.incrementAndGet();
    }
    
    /**
     * Gets the current moving average value
     * @return The current moving average, or NaN if no values have been added
     */
    public double getAverage() {
        return currentValue.get();
    }
    
    /**
     * Gets the current variance
     * @return The current variance
     */
    public double getVariance() {
        return variance.get();
    }
    
    /**
     * Gets the current standard deviation
     * @return The current standard deviation
     */
    public double getStandardDeviation() {
        return Math.sqrt(variance.get());
    }
    
    /**
     * Gets the number of values that have been added
     * @return The count of values
     */
    public long getCount() {
        return count.get();
    }
    
    /**
     * Gets the decay factor (alpha)
     * @return The decay factor
     */
    public double getAlpha() {
        return alpha;
    }
    
    /**
     * Checks if the EMA has any values
     * @return true if at least one value has been added
     */
    public boolean hasValues() {
        return count.get() > 0;
    }
    
    /**
     * Resets the EMA to its initial state
     */
    public void reset() {
        currentValue.set(Double.NaN);
        count.set(0);
        variance.set(0.0);
    }
    
    /**
     * Gets the effective sample size (number of samples that contribute significantly)
     * @return The effective sample size
     */
    public double getEffectiveSampleSize() {
        if (alpha >= 1.0) return 1.0;
        return 2.0 / alpha - 1.0;
    }
    
    /**
     * Gets the half-life of the EMA (number of samples for weight to decay to 0.5)
     * @return The half-life in samples
     */
    public double getHalfLife() {
        return Math.log(0.5) / Math.log(1.0 - alpha);
    }
    
    /**
     * Creates a copy of this EMA
     * @return A new EMA with the same state
     */
    public ExponentialMovingAverage copy() {
        ExponentialMovingAverage copy = new ExponentialMovingAverage(alpha);
        copy.currentValue.set(currentValue.get());
        copy.count.set(count.get());
        copy.variance.set(variance.get());
        return copy;
    }
    
    /**
     * Merges this EMA with another EMA
     * @param other The other EMA to merge with
     * @return A new EMA representing the merged result
     */
    public ExponentialMovingAverage merge(ExponentialMovingAverage other) {
        if (Math.abs(this.alpha - other.alpha) > 1e-10) {
            throw new IllegalArgumentException("Cannot merge EMAs with different alpha values");
        }
        
        ExponentialMovingAverage merged = new ExponentialMovingAverage(alpha);
        
        if (this.count.get() == 0) {
            merged.currentValue.set(other.currentValue.get());
            merged.count.set(other.count.get());
            merged.variance.set(other.variance.get());
        } else if (other.count.get() == 0) {
            merged.currentValue.set(this.currentValue.get());
            merged.count.set(this.count.get());
            merged.variance.set(this.variance.get());
        } else {
            // Weighted average based on effective sample sizes
            double thisWeight = this.getEffectiveSampleSize();
            double otherWeight = other.getEffectiveSampleSize();
            double totalWeight = thisWeight + otherWeight;
            
            double mergedValue = (this.currentValue.get() * thisWeight + 
                                other.currentValue.get() * otherWeight) / totalWeight;
            double mergedVariance = (this.variance.get() * thisWeight + 
                                   other.variance.get() * otherWeight) / totalWeight;
            
            merged.currentValue.set(mergedValue);
            merged.count.set(this.count.get() + other.count.get());
            merged.variance.set(mergedVariance);
        }
        
        return merged;
    }
    
    @Override
    public String toString() {
        return String.format("EMA(α=%.3f, avg=%.3f, var=%.3f, n=%d)", 
                           alpha, getAverage(), getVariance(), getCount());
    }
}