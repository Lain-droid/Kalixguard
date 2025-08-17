package com.apexguard.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;

/**
 * Comprehensive statistical utility functions for anti-cheat analysis
 */
public final class Stats {
    
    private Stats() {
        // Utility class, prevent instantiation
    }
    
    /**
     * Calculates the mean (average) of an array of values
     * @param values The values to calculate the mean for
     * @return The mean value
     */
    public static double mean(double[] values) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        }
        return DoubleStream.of(values).average().orElse(Double.NaN);
    }
    
    /**
     * Calculates the mean (average) of a list of values
     * @param values The values to calculate the mean for
     * @return The mean value
     */
    public static double mean(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return Double.NaN;
        }
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }
    
    /**
     * Calculates the mean (average) of an array of long values
     * @param values The values to calculate the mean for
     * @return The mean value
     */
    public static double mean(long[] values) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        }
        return Arrays.stream(values).mapToDouble(v -> (double) v).average().orElse(Double.NaN);
    }
    
    /**
     * Calculates the median of an array of values
     * @param values The values to calculate the median for
     * @return The median value
     */
    public static double median(double[] values) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        }
        
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        
        int n = sorted.length;
        if (n % 2 == 0) {
            return (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0;
        } else {
            return sorted[n / 2];
        }
    }
    
    /**
     * Calculates the median of a list of values
     * @param values The values to calculate the median for
     * @return The median value
     */
    public static double median(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return Double.NaN;
        }
        
        List<Double> sorted = values.stream().sorted().toList();
        int n = sorted.size();
        
        if (n % 2 == 0) {
            return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
        } else {
            return sorted.get(n / 2);
        }
    }
    
    /**
     * Calculates the variance of an array of values
     * @param values The values to calculate the variance for
     * @return The variance
     */
    public static double variance(double[] values) {
        if (values == null || values.length < 2) {
            return Double.NaN;
        }
        
        double mean = mean(values);
        double sumSquaredDiff = 0.0;
        
        for (double value : values) {
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }
        
        return sumSquaredDiff / (values.length - 1); // Sample variance
    }
    
    /**
     * Calculates the variance of a list of values
     * @param values The values to calculate the variance for
     * @return The variance
     */
    public static double variance(List<Double> values) {
        if (values == null || values.size() < 2) {
            return Double.NaN;
        }
        
        double mean = mean(values);
        double sumSquaredDiff = values.stream()
            .mapToDouble(v -> {
                double diff = v - mean;
                return diff * diff;
            })
            .sum();
        
        return sumSquaredDiff / (values.size() - 1); // Sample variance
    }
    
    /**
     * Calculates the standard deviation of an array of values
     * @param values The values to calculate the standard deviation for
     * @return The standard deviation
     */
    public static double standardDeviation(double[] values) {
        double var = variance(values);
        return Double.isNaN(var) ? Double.NaN : Math.sqrt(var);
    }
    
    /**
     * Calculates the standard deviation of a list of values
     * @param values The values to calculate the standard deviation for
     * @return The standard deviation
     */
    public static double standardDeviation(List<Double> values) {
        double var = variance(values);
        return Double.isNaN(var) ? Double.NaN : Math.sqrt(var);
    }
    
    /**
     * Calculates the standard deviation of an array of long values
     * @param values The values to calculate the standard deviation for
     * @return The standard deviation
     */
    public static double standardDeviation(long[] values) {
        if (values == null || values.length < 2) {
            return Double.NaN;
        }
        
        double mean = mean(values);
        double sumSquaredDiff = 0.0;
        
        for (long value : values) {
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }
        
        double variance = sumSquaredDiff / (values.length - 1); // Sample variance
        return Math.sqrt(variance);
    }
    
    /**
     * Calculates the z-score of a value relative to a distribution
     * @param value The value to calculate the z-score for
     * @param mean The mean of the distribution
     * @param standardDeviation The standard deviation of the distribution
     * @return The z-score
     */
    public static double zScore(double value, double mean, double standardDeviation) {
        if (standardDeviation <= 0) {
            return Double.NaN;
        }
        return (value - mean) / standardDeviation;
    }
    
    /**
     * Calculates the z-score of a value relative to an array of values
     * @param value The value to calculate the z-score for
     * @param values The reference values
     * @return The z-score
     */
    public static double zScore(double value, double[] values) {
        double mean = mean(values);
        double stdDev = standardDeviation(values);
        return zScore(value, mean, stdDev);
    }
    
    /**
     * Calculates the z-score of a value relative to a list of values
     * @param value The value to calculate the z-score for
     * @param values The reference values
     * @return The z-score
     */
    public static double zScore(double value, List<Double> values) {
        double mean = mean(values);
        double stdDev = standardDeviation(values);
        return zScore(value, mean, stdDev);
    }
    
    /**
     * Calculates the median absolute deviation (MAD)
     * @param values The values to calculate the MAD for
     * @return The MAD value
     */
    public static double medianAbsoluteDeviation(double[] values) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        }
        
        double median = median(values);
        double[] absoluteDeviations = new double[values.length];
        
        for (int i = 0; i < values.length; i++) {
            absoluteDeviations[i] = Math.abs(values[i] - median);
        }
        
        return median(absoluteDeviations);
    }
    
    /**
     * Calculates the median absolute deviation (MAD)
     * @param values The values to calculate the MAD for
     * @return The MAD value
     */
    public static double medianAbsoluteDeviation(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return Double.NaN;
        }
        
        double median = median(values);
        List<Double> absoluteDeviations = values.stream()
            .map(v -> Math.abs(v - median))
            .toList();
        
        return median(absoluteDeviations);
    }
    
    /**
     * Calculates the robust z-score using MAD instead of standard deviation
     * @param value The value to calculate the robust z-score for
     * @param values The reference values
     * @return The robust z-score
     */
    public static double robustZScore(double value, double[] values) {
        double median = median(values);
        double mad = medianAbsoluteDeviation(values);
        
        if (mad <= 0) {
            return Double.NaN;
        }
        
        // MAD is approximately 0.6745 * standard deviation for normal distributions
        return (value - median) / (mad / 0.6745);
    }
    
    /**
     * Calculates the robust z-score using MAD instead of standard deviation
     * @param value The value to calculate the robust z-score for
     * @param values The reference values
     * @return The robust z-score
     */
    public static double robustZScore(double value, List<Double> values) {
        double median = median(values);
        double mad = medianAbsoluteDeviation(values);
        
        if (mad <= 0) {
            return Double.NaN;
        }
        
        // MAD is approximately 0.6745 * standard deviation for normal distributions
        return (value - median) / (mad / 0.6745);
    }
    
    /**
     * Calculates the coefficient of variation (CV)
     * @param values The values to calculate the CV for
     * @return The coefficient of variation
     */
    public static double coefficientOfVariation(double[] values) {
        double mean = mean(values);
        double stdDev = standardDeviation(values);
        
        if (mean == 0 || Double.isNaN(stdDev)) {
            return Double.NaN;
        }
        
        return stdDev / Math.abs(mean);
    }
    
    /**
     * Calculates the coefficient of variation (CV)
     * @param values The values to calculate the CV for
     * @return The coefficient of variation
     */
    public static double coefficientOfVariation(List<Double> values) {
        double mean = mean(values);
        double stdDev = standardDeviation(values);
        
        if (mean == 0 || Double.isNaN(stdDev)) {
            return Double.NaN;
        }
        
        return stdDev / Math.abs(mean);
    }
    
    /**
     * Calculates the coefficient of variation (CV) for long values
     * @param values The values to calculate the CV for
     * @return The coefficient of variation
     */
    public static double coefficientOfVariation(long[] values) {
        if (values == null || values.length < 2) {
            return Double.NaN;
        }
        
        double mean = mean(values);
        if (Math.abs(mean) < 1e-10) {
            return Double.NaN;
        }
        
        double stdDev = standardDeviation(values);
        return stdDev / Math.abs(mean);
    }
    
    /**
     * Calculates the skewness of a distribution
     * @param values The values to calculate the skewness for
     * @return The skewness
     */
    public static double skewness(double[] values) {
        if (values == null || values.length < 3) {
            return Double.NaN;
        }
        
        double mean = mean(values);
        double stdDev = standardDeviation(values);
        
        if (stdDev <= 0) {
            return Double.NaN;
        }
        
        double sum = 0.0;
        for (double value : values) {
            double z = (value - mean) / stdDev;
            sum += z * z * z;
        }
        
        return sum / values.length;
    }
    
    /**
     * Calculates the kurtosis of a distribution
     * @param values The values to calculate the kurtosis for
     * @return The kurtosis
     */
    public static double kurtosis(double[] values) {
        if (values == null || values.length < 4) {
            return Double.NaN;
        }
        
        double mean = mean(values);
        double stdDev = standardDeviation(values);
        
        if (stdDev <= 0) {
            return Double.NaN;
        }
        
        double sum = 0.0;
        for (double value : values) {
            double z = (value - mean) / stdDev;
            sum += z * z * z * z;
        }
        
        return (sum / values.length) - 3.0; // Excess kurtosis
    }
    
    /**
     * Calculates the range of values
     * @param values The values to calculate the range for
     * @return The range (max - min)
     */
    public static double range(double[] values) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        }
        
        double min = DoubleStream.of(values).min().orElse(Double.NaN);
        double max = DoubleStream.of(values).max().orElse(Double.NaN);
        
        return max - min;
    }
    
    /**
     * Calculates the interquartile range (IQR)
     * @param values The values to calculate the IQR for
     * @return The IQR (Q3 - Q1)
     */
    public static double interquartileRange(double[] values) {
        if (values == null || values.length < 4) {
            return Double.NaN;
        }
        
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        
        int n = sorted.length;
        double q1 = percentile(sorted, 25);
        double q3 = percentile(sorted, 75);
        
        return q3 - q1;
    }
    
    /**
     * Calculates the percentile of a value in a sorted array
     * @param sortedValues The sorted array of values
     * @param percentile The percentile to calculate (0-100)
     * @return The percentile value
     */
    public static double percentile(double[] sortedValues, double percentile) {
        if (sortedValues == null || sortedValues.length == 0 || percentile < 0 || percentile > 100) {
            return Double.NaN;
        }
        
        if (percentile == 0) return sortedValues[0];
        if (percentile == 100) return sortedValues[sortedValues.length - 1];
        
        double index = (percentile / 100.0) * (sortedValues.length - 1);
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);
        
        if (lowerIndex == upperIndex) {
            return sortedValues[lowerIndex];
        }
        
        double weight = index - lowerIndex;
        return sortedValues[lowerIndex] * (1 - weight) + sortedValues[upperIndex] * weight;
    }
    
    /**
     * Calculates the correlation coefficient between two arrays
     * @param x The first array of values
     * @param y The second array of values
     * @return The correlation coefficient
     */
    public static double correlation(double[] x, double[] y) {
        if (x == null || y == null || x.length != y.length || x.length < 2) {
            return Double.NaN;
        }
        
        double meanX = mean(x);
        double meanY = mean(y);
        
        double numerator = 0.0;
        double sumXSquared = 0.0;
        double sumYSquared = 0.0;
        
        for (int i = 0; i < x.length; i++) {
            double diffX = x[i] - meanX;
            double diffY = y[i] - meanY;
            
            numerator += diffX * diffY;
            sumXSquared += diffX * diffX;
            sumYSquared += diffY * diffY;
        }
        
        double denominator = Math.sqrt(sumXSquared * sumYSquared);
        if (denominator == 0) {
            return Double.NaN;
        }
        
        return numerator / denominator;
    }
    
    /**
     * Calculates the entropy of a discrete distribution
     * @param probabilities The probabilities of each outcome
     * @return The entropy in bits
     */
    public static double entropy(double[] probabilities) {
        if (probabilities == null || probabilities.length == 0) {
            return Double.NaN;
        }
        
        double entropy = 0.0;
        for (double p : probabilities) {
            if (p > 0) {
                entropy -= p * Math.log(p) / Math.log(2);
            }
        }
        
        return entropy;
    }
    
    /**
     * Normalizes values to a 0-1 range
     * @param values The values to normalize
     * @return The normalized values
     */
    public static double[] normalize(double[] values) {
        if (values == null || values.length == 0) {
            return new double[0];
        }
        
        double min = DoubleStream.of(values).min().orElse(0);
        double max = DoubleStream.of(values).max().orElse(1);
        
        if (max == min) {
            return new double[values.length]; // All values become 0
        }
        
        double[] normalized = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            normalized[i] = (values[i] - min) / (max - min);
        }
        
        return normalized;
    }
    
    /**
     * Standardizes values to have mean 0 and standard deviation 1
     * @param values The values to standardize
     * @return The standardized values
     */
    public static double[] standardize(double[] values) {
        if (values == null || values.length == 0) {
            return new double[0];
        }
        
        double mean = mean(values);
        double stdDev = standardDeviation(values);
        
        if (stdDev <= 0) {
            return new double[values.length]; // All values become 0
        }
        
        double[] standardized = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            standardized[i] = (values[i] - mean) / stdDev;
        }
        
        return standardized;
    }
    
    /**
     * Clamps a value between a minimum and maximum
     * @param value The value to clamp
     * @param min The minimum value
     * @param max The maximum value
     * @return The clamped value
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Calculates GCD consistency for aim assist detection
     * @param values The values to analyze
     * @return The consistency score (0-1, higher means more consistent)
     */
    public static double gcdConsistency(double[] values) {
        if (values == null || values.length < 3) {
            return 0.0;
        }
        
        // Calculate differences between consecutive values
        double[] diffs = new double[values.length - 1];
        for (int i = 0; i < diffs.length; i++) {
            diffs[i] = Math.abs(values[i + 1] - values[i]);
        }
        
        // Find the most common difference (approximate GCD)
        double mostCommonDiff = median(diffs);
        
        // Calculate consistency based on how many values are close to multiples of the common difference
        int consistentCount = 0;
        double tolerance = mostCommonDiff * 0.1; // 10% tolerance
        
        for (double diff : diffs) {
            double remainder = diff % mostCommonDiff;
            if (remainder <= tolerance || remainder >= (mostCommonDiff - tolerance)) {
                consistentCount++;
            }
        }
        
        return (double) consistentCount / diffs.length;
    }
}