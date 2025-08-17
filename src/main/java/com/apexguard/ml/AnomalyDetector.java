package com.apexguard.ml;

import com.apexguard.util.Stats;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Machine learning-based anomaly detector for anti-cheat systems
 * Implements multiple detection algorithms including isolation forest, LOF, and statistical methods
 */
public final class AnomalyDetector {
    private final Map<String, PlayerModel> playerModels;
    private final Map<String, GlobalModel> globalModels;
    private final AtomicInteger totalDetections;
    private final AtomicLong lastUpdateTime;
    
    // Configuration
    private final int minSamplesForTraining;
    private final double anomalyThreshold;
    private final boolean enableOnlineLearning;
    private final int maxModelAge;
    
    public AnomalyDetector() {
        this.playerModels = new ConcurrentHashMap<>();
        this.globalModels = new ConcurrentHashMap<>();
        this.totalDetections = new AtomicInteger(0);
        this.lastUpdateTime = new AtomicLong(System.currentTimeMillis());
        
        // Default configuration
        this.minSamplesForTraining = 50;
        this.anomalyThreshold = 0.8;
        this.enableOnlineLearning = true;
        this.maxModelAge = 24 * 60 * 60 * 1000; // 24 hours
    }
    
    /**
     * Creates an anomaly detector with custom configuration
     * @param minSamplesForTraining Minimum samples required for training
     * @param anomalyThreshold Threshold for anomaly detection (0-1)
     * @param enableOnlineLearning Whether to enable online learning
     * @param maxModelAge Maximum age of models in milliseconds
     */
    public AnomalyDetector(int minSamplesForTraining, double anomalyThreshold, 
                          boolean enableOnlineLearning, int maxModelAge) {
        this.playerModels = new ConcurrentHashMap<>();
        this.globalModels = new ConcurrentHashMap<>();
        this.totalDetections = new AtomicInteger(0);
        this.lastUpdateTime = new AtomicLong(System.currentTimeMillis());
        
        this.minSamplesForTraining = minSamplesForTraining;
        this.anomalyThreshold = anomalyThreshold;
        this.enableOnlineLearning = enableOnlineLearning;
        this.maxModelAge = maxModelAge;
    }
    
    /**
     * Detects anomalies in a feature vector
     * @param playerId The player ID
     * @param features The feature vector to analyze
     * @return Anomaly detection result
     */
    public AnomalyResult detectAnomaly(String playerId, FeatureVector features) {
        if (features == null || features.isEmpty()) {
            return new AnomalyResult(false, 0.0, "No features provided");
        }
        
        long currentTime = System.currentTimeMillis();
        lastUpdateTime.set(currentTime);
        
        // Get or create player model
        PlayerModel playerModel = playerModels.computeIfAbsent(playerId, 
            k -> new PlayerModel(playerId, minSamplesForTraining));
        
        // Get or create global model
        GlobalModel globalModel = globalModels.computeIfAbsent("global", 
            k -> new GlobalModel(minSamplesForTraining));
        
        // Update models with new data
        if (enableOnlineLearning) {
            playerModel.update(features);
            globalModel.update(features);
        }
        
        // Perform anomaly detection using multiple methods
        List<DetectionMethod> results = new ArrayList<>();
        
        // Statistical anomaly detection
        double statisticalScore = detectStatisticalAnomaly(playerModel, features);
        results.add(new DetectionMethod("statistical", statisticalScore));
        
        // Isolation Forest anomaly detection
        double isolationScore = detectIsolationForestAnomaly(playerModel, features);
        results.add(new DetectionMethod("isolation_forest", isolationScore));
        
        // Local Outlier Factor anomaly detection
        double lofScore = detectLOFAnomaly(playerModel, features);
        results.add(new DetectionMethod("local_outlier_factor", lofScore));
        
        // Global model comparison
        double globalScore = detectGlobalAnomaly(globalModel, features);
        results.add(new DetectionMethod("global_comparison", globalScore));
        
        // Combine scores using weighted average
        double combinedScore = combineScores(results);
        
        // Determine if anomaly is detected
        boolean isAnomaly = combinedScore >= anomalyThreshold;
        
        if (isAnomaly) {
            totalDetections.incrementAndGet();
        }
        
        // Clean up old models
        cleanupOldModels(currentTime);
        
        return new AnomalyResult(isAnomaly, combinedScore, 
            String.format("Combined score: %.3f, Methods: %s", 
                combinedScore, results.toString()));
    }
    
    /**
     * Detects statistical anomalies using z-scores and robust statistics
     */
    private double detectStatisticalAnomaly(PlayerModel model, FeatureVector features) {
        if (!model.isReady()) return 0.0;
        
        double[] numericalFeatures = features.getNumericalFeatures();
        if (numericalFeatures.length == 0) return 0.0;
        
        double totalScore = 0.0;
        int validFeatures = 0;
        
        for (int i = 0; i < numericalFeatures.length; i++) {
            if (Double.isNaN(numericalFeatures[i])) continue;
            
            double value = numericalFeatures[i];
            double mean = model.getFeatureMean(i);
            double stdDev = model.getFeatureStdDev(i);
            
            if (stdDev > 0) {
                double zScore = Math.abs((value - mean) / stdDev);
                double robustZScore = model.getRobustZScore(i, value);
                
                // Use the more conservative score
                double score = Math.min(zScore, robustZScore);
                totalScore += score;
                validFeatures++;
            }
        }
        
        if (validFeatures == 0) return 0.0;
        
        // Normalize to 0-1 range
        return Math.min(1.0, totalScore / (validFeatures * 3.0)); // 3.0 is typical threshold
    }
    
    /**
     * Detects anomalies using Isolation Forest algorithm
     */
    private double detectIsolationForestAnomaly(PlayerModel model, FeatureVector features) {
        if (!model.isReady()) return 0.0;
        
        // Simplified Isolation Forest implementation
        double[] numericalFeatures = features.getNumericalFeatures();
        if (numericalFeatures.length == 0) return 0.0;
        
        double totalScore = 0.0;
        int validFeatures = 0;
        
        for (int i = 0; i < numericalFeatures.length; i++) {
            if (Double.isNaN(numericalFeatures[i])) continue;
            
            double value = numericalFeatures[i];
            double min = model.getFeatureMin(i);
            double max = model.getFeatureMax(i);
            double q25 = model.getFeaturePercentile(i, 25);
            double q75 = model.getFeaturePercentile(i, 75);
            double iqr = q75 - q25;
            
            if (max > min && iqr > 0) {
                // Check if value is outside normal range
                double lowerBound = q25 - 1.5 * iqr;
                double upperBound = q75 + 1.5 * iqr;
                
                if (value < lowerBound || value > upperBound) {
                    // Calculate how far outside the bounds
                    double distance = Math.max(lowerBound - value, value - upperBound);
                    double normalizedDistance = distance / iqr;
                    totalScore += Math.min(1.0, normalizedDistance);
                }
                validFeatures++;
            }
        }
        
        if (validFeatures == 0) return 0.0;
        
        return totalScore / validFeatures;
    }
    
    /**
     * Detects anomalies using Local Outlier Factor algorithm
     */
    private double detectLOFAnomaly(PlayerModel model, FeatureVector features) {
        if (!model.isReady()) return 0.0;
        
        // Simplified LOF implementation
        double[] numericalFeatures = features.getNumericalFeatures();
        if (numericalFeatures.length == 0) return 0.0;
        
        // Calculate distance to nearest neighbors
        double totalScore = 0.0;
        int validFeatures = 0;
        
        for (int i = 0; i < numericalFeatures.length; i++) {
            if (Double.isNaN(numericalFeatures[i])) continue;
            
            double value = numericalFeatures[i];
            double[] historicalValues = model.getFeatureHistory(i);
            
            if (historicalValues.length > 0) {
                // Find k nearest neighbors
                int k = Math.min(5, historicalValues.length);
                double[] distances = new double[historicalValues.length];
                
                for (int j = 0; j < historicalValues.length; j++) {
                    distances[j] = Math.abs(value - historicalValues[j]);
                }
                
                // Sort distances and take k nearest
                Arrays.sort(distances);
                double avgDistance = 0.0;
                for (int j = 0; j < k; j++) {
                    avgDistance += distances[j];
                }
                avgDistance /= k;
                
                // Normalize by feature range
                double range = model.getFeatureMax(i) - model.getFeatureMin(i);
                if (range > 0) {
                    double normalizedDistance = avgDistance / range;
                    totalScore += Math.min(1.0, normalizedDistance);
                }
                validFeatures++;
            }
        }
        
        if (validFeatures == 0) return 0.0;
        
        return totalScore / validFeatures;
    }
    
    /**
     * Detects anomalies by comparing to global model
     */
    private double detectGlobalAnomaly(GlobalModel model, FeatureVector features) {
        if (!model.isReady()) return 0.0;
        
        double[] numericalFeatures = features.getNumericalFeatures();
        if (numericalFeatures.length == 0) return 0.0;
        
        double totalScore = 0.0;
        int validFeatures = 0;
        
        for (int i = 0; i < numericalFeatures.length; i++) {
            if (Double.isNaN(numericalFeatures[i])) continue;
            
            double value = numericalFeatures[i];
            double globalMean = model.getFeatureMean(i);
            double globalStdDev = model.getFeatureStdDev(i);
            
            if (globalStdDev > 0) {
                double zScore = Math.abs((value - globalMean) / globalStdDev);
                totalScore += Math.min(1.0, zScore / 3.0); // Normalize to 0-1
                validFeatures++;
            }
        }
        
        if (validFeatures == 0) return 0.0;
        
        return totalScore / validFeatures;
    }
    
    /**
     * Combines scores from multiple detection methods
     */
    private double combineScores(List<DetectionMethod> results) {
        if (results.isEmpty()) return 0.0;
        
        // Weighted average with statistical methods having higher weight
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;
        
        for (DetectionMethod result : results) {
            double weight = getMethodWeight(result.method);
            totalWeightedScore += result.score * weight;
            totalWeight += weight;
        }
        
        if (totalWeight == 0) return 0.0;
        
        return totalWeightedScore / totalWeight;
    }
    
    /**
     * Gets the weight for a detection method
     */
    private double getMethodWeight(String method) {
        return switch (method) {
            case "statistical" -> 0.4;
            case "isolation_forest" -> 0.3;
            case "local_outlier_factor" -> 0.2;
            case "global_comparison" -> 0.1;
            default -> 0.25;
        };
    }
    
    /**
     * Cleans up old models to prevent memory leaks
     */
    private void cleanupOldModels(long currentTime) {
        playerModels.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getLastUpdateTime() > maxModelAge);
        
        globalModels.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getLastUpdateTime() > maxModelAge);
    }
    
    /**
     * Gets statistics about the anomaly detector
     */
    public DetectorStats getStats() {
        return new DetectorStats(
            playerModels.size(),
            globalModels.size(),
            totalDetections.get(),
            lastUpdateTime.get()
        );
    }
    
    /**
     * Resets the anomaly detector
     */
    public void reset() {
        playerModels.clear();
        globalModels.clear();
        totalDetections.set(0);
        lastUpdateTime.set(System.currentTimeMillis());
    }
    
    // Inner classes
    public static class AnomalyResult {
        public final boolean isAnomaly;
        public final double score;
        public final String details;
        
        public AnomalyResult(boolean isAnomaly, double score, String details) {
            this.isAnomaly = isAnomaly;
            this.score = score;
            this.details = details;
        }
    }
    
    public static class DetectionMethod {
        public final String method;
        public final double score;
        
        public DetectionMethod(String method, double score) {
            this.method = method;
            this.score = score;
        }
        
        @Override
        public String toString() {
            return String.format("%s=%.3f", method, score);
        }
    }
    
    public static class DetectorStats {
        public final int playerModels;
        public final int globalModels;
        public final int totalDetections;
        public final long lastUpdateTime;
        
        public DetectorStats(int playerModels, int globalModels, int totalDetections, long lastUpdateTime) {
            this.playerModels = playerModels;
            this.globalModels = globalModels;
            this.totalDetections = totalDetections;
            this.lastUpdateTime = lastUpdateTime;
        }
    }
    
    /**
     * Player-specific model for anomaly detection
     */
    private static class PlayerModel {
        private final String playerId;
        private final int minSamples;
        private final Map<Integer, FeatureStats> featureStats;
        private final long lastUpdateTime;
        
        public PlayerModel(String playerId, int minSamples) {
            this.playerId = playerId;
            this.minSamples = minSamples;
            this.featureStats = new HashMap<>();
            this.lastUpdateTime = System.currentTimeMillis();
        }
        
        public void update(FeatureVector features) {
            double[] numericalFeatures = features.getNumericalFeatures();
            
            for (int i = 0; i < numericalFeatures.length; i++) {
                if (Double.isNaN(numericalFeatures[i])) continue;
                
                FeatureStats stats = featureStats.computeIfAbsent(i, k -> new FeatureStats());
                stats.addValue(numericalFeatures[i]);
            }
        }
        
        public boolean isReady() {
            return featureStats.values().stream()
                .anyMatch(stats -> stats.getCount() >= minSamples);
        }
        
        public double getFeatureMean(int featureIndex) {
            FeatureStats stats = featureStats.get(featureIndex);
            return stats != null ? stats.getMean() : 0.0;
        }
        
        public double getFeatureStdDev(int featureIndex) {
            FeatureStats stats = featureStats.get(featureIndex);
            return stats != null ? stats.getStandardDeviation() : 1.0;
        }
        
        public double getFeatureMin(int featureIndex) {
            FeatureStats stats = featureStats.get(featureIndex);
            return stats != null ? stats.getMin() : 0.0;
        }
        
        public double getFeatureMax(int featureIndex) {
            FeatureStats stats = featureStats.get(featureIndex);
            return stats != null ? stats.getMax() : 0.0;
        }
        
        public double getFeaturePercentile(int featureIndex, double percentile) {
            FeatureStats stats = featureStats.get(featureIndex);
            return stats != null ? stats.getPercentile(percentile) : 0.0;
        }
        
        public double getRobustZScore(int featureIndex, double value) {
            FeatureStats stats = featureStats.get(featureIndex);
            if (stats == null) return 0.0;
            
            double median = stats.getMedian();
            double mad = stats.getMedianAbsoluteDeviation();
            
            if (mad <= 0) return 0.0;
            
            return Math.abs(value - median) / (mad / 0.6745);
        }
        
        public double[] getFeatureHistory(int featureIndex) {
            FeatureStats stats = featureStats.get(featureIndex);
            return stats != null ? stats.getValues() : new double[0];
        }
        
        public long getLastUpdateTime() {
            return lastUpdateTime;
        }
    }
    
    /**
     * Global model for anomaly detection
     */
    private static class GlobalModel {
        private final int minSamples;
        private final Map<Integer, FeatureStats> featureStats;
        private final long lastUpdateTime;
        
        public GlobalModel(int minSamples) {
            this.minSamples = minSamples;
            this.featureStats = new HashMap<>();
            this.lastUpdateTime = System.currentTimeMillis();
        }
        
        public void update(FeatureVector features) {
            double[] numericalFeatures = features.getNumericalFeatures();
            
            for (int i = 0; i < numericalFeatures.length; i++) {
                if (Double.isNaN(numericalFeatures[i])) continue;
                
                FeatureStats stats = featureStats.computeIfAbsent(i, k -> new FeatureStats());
                stats.addValue(numericalFeatures[i]);
            }
        }
        
        public boolean isReady() {
            return featureStats.values().stream()
                .anyMatch(stats -> stats.getCount() >= minSamples);
        }
        
        public double getFeatureMean(int featureIndex) {
            FeatureStats stats = featureStats.get(featureIndex);
            return stats != null ? stats.getMean() : 0.0;
        }
        
        public double getFeatureStdDev(int featureIndex) {
            FeatureStats stats = featureStats.get(featureIndex);
            return stats != null ? stats.getStandardDeviation() : 1.0;
        }
        
        public long getLastUpdateTime() {
            return lastUpdateTime;
        }
    }
    
    /**
     * Statistics for a single feature
     */
    private static class FeatureStats {
        private final List<Double> values;
        private double sum;
        private double sumSquared;
        private double min;
        private double max;
        
        public FeatureStats() {
            this.values = new ArrayList<>();
            this.sum = 0.0;
            this.sumSquared = 0.0;
            this.min = Double.MAX_VALUE;
            this.max = Double.MIN_VALUE;
        }
        
        public void addValue(double value) {
            values.add(value);
            sum += value;
            sumSquared += value * value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        
        public int getCount() {
            return values.size();
        }
        
        public double getMean() {
            return values.isEmpty() ? 0.0 : sum / values.size();
        }
        
        public double getVariance() {
            if (values.size() < 2) return 0.0;
            double mean = getMean();
            return (sumSquared / values.size()) - (mean * mean);
        }
        
        public double getStandardDeviation() {
            return Math.sqrt(getVariance());
        }
        
        public double getMin() {
            return min == Double.MAX_VALUE ? 0.0 : min;
        }
        
        public double getMax() {
            return max == Double.MIN_VALUE ? 0.0 : max;
        }
        
        public double getMedian() {
            if (values.isEmpty()) return 0.0;
            
            List<Double> sorted = new ArrayList<>(values);
            Collections.sort(sorted);
            
            int size = sorted.size();
            if (size % 2 == 0) {
                return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
            } else {
                return sorted.get(size / 2);
            }
        }
        
        public double getMedianAbsoluteDeviation() {
            if (values.isEmpty()) return 0.0;
            
            double median = getMedian();
            List<Double> deviations = new ArrayList<>();
            
            for (double value : values) {
                deviations.add(Math.abs(value - median));
            }
            
            Collections.sort(deviations);
            int size = deviations.size();
            
            if (size % 2 == 0) {
                return (deviations.get(size / 2 - 1) + deviations.get(size / 2)) / 2.0;
            } else {
                return deviations.get(size / 2);
            }
        }
        
        public double getPercentile(double percentile) {
            if (values.isEmpty()) return 0.0;
            
            List<Double> sorted = new ArrayList<>(values);
            Collections.sort(sorted);
            
            double index = (percentile / 100.0) * (sorted.size() - 1);
            int lowerIndex = (int) Math.floor(index);
            int upperIndex = (int) Math.ceil(index);
            
            if (lowerIndex == upperIndex) {
                return sorted.get(lowerIndex);
            }
            
            double weight = index - lowerIndex;
            return sorted.get(lowerIndex) * (1 - weight) + sorted.get(upperIndex) * weight;
        }
        
        public double[] getValues() {
            return values.stream().mapToDouble(Double::doubleValue).toArray();
        }
    }
}