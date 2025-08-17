package com.apexguard.ml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a feature vector for machine learning anomaly detection
 */
public final class FeatureVector {
    private final double[] numericalFeatures;
    private final String[] categoricalFeatures;
    private final Map<String, Double> namedFeatures;
    private final long timestamp;
    private final String playerId;
    
    public FeatureVector(double[] numericalFeatures, String[] categoricalFeatures, 
                        Map<String, Double> namedFeatures, long timestamp, String playerId) {
        this.numericalFeatures = numericalFeatures != null ? numericalFeatures.clone() : new double[0];
        this.categoricalFeatures = categoricalFeatures != null ? categoricalFeatures.clone() : new String[0];
        this.namedFeatures = namedFeatures != null ? new HashMap<>(namedFeatures) : new HashMap<>();
        this.timestamp = timestamp;
        this.playerId = playerId;
    }
    
    // Getters
    public double[] getNumericalFeatures() { return numericalFeatures.clone(); }
    public String[] getCategoricalFeatures() { return categoricalFeatures.clone(); }
    public Map<String, Double> getNamedFeatures() { return new HashMap<>(namedFeatures); }
    public long getTimestamp() { return timestamp; }
    public String getPlayerId() { return playerId; }
    
    /**
     * Gets a numerical feature at the specified index
     * @param index The index of the feature
     * @return The feature value, or NaN if index is out of bounds
     */
    public double getNumericalFeature(int index) {
        if (index >= 0 && index < numericalFeatures.length) {
            return numericalFeatures[index];
        }
        return Double.NaN;
    }
    
    /**
     * Gets a categorical feature at the specified index
     * @param index The index of the feature
     * @return The feature value, or null if index is out of bounds
     */
    public String getCategoricalFeature(int index) {
        if (index >= 0 && index < categoricalFeatures.length) {
            return categoricalFeatures[index];
        }
        return null;
    }
    
    /**
     * Gets a named feature by its name
     * @param name The name of the feature
     * @return The feature value, or null if not found
     */
    public Double getNamedFeature(String name) {
        return namedFeatures.get(name);
    }
    
    /**
     * Gets the number of numerical features
     * @return The count of numerical features
     */
    public int getNumericalFeatureCount() {
        return numericalFeatures.length;
    }
    
    /**
     * Gets the number of categorical features
     * @return The count of categorical features
     */
    public int getCategoricalFeatureCount() {
        return categoricalFeatures.length;
    }
    
    /**
     * Gets the number of named features
     * @return The count of named features
     */
    public int getNamedFeatureCount() {
        return namedFeatures.size();
    }
    
    /**
     * Gets the total number of features
     * @return The total count of all features
     */
    public int getTotalFeatureCount() {
        return numericalFeatures.length + categoricalFeatures.length + namedFeatures.size();
    }
    
    /**
     * Checks if the feature vector is empty
     * @return true if there are no features
     */
    public boolean isEmpty() {
        return getTotalFeatureCount() == 0;
    }
    
    /**
     * Normalizes numerical features to 0-1 range
     * @return A new feature vector with normalized numerical features
     */
    public FeatureVector normalize() {
        if (numericalFeatures.length == 0) {
            return this;
        }
        
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        for (double feature : numericalFeatures) {
            if (!Double.isNaN(feature)) {
                min = Math.min(min, feature);
                max = Math.max(max, feature);
            }
        }
        
        if (max == min) {
            return this; // No normalization needed
        }
        
        double[] normalized = new double[numericalFeatures.length];
        for (int i = 0; i < numericalFeatures.length; i++) {
            if (Double.isNaN(numericalFeatures[i])) {
                normalized[i] = Double.NaN;
            } else {
                normalized[i] = (numericalFeatures[i] - min) / (max - min);
            }
        }
        
        return new FeatureVector(normalized, categoricalFeatures, namedFeatures, timestamp, playerId);
    }
    
    /**
     * Standardizes numerical features to have mean 0 and standard deviation 1
     * @return A new feature vector with standardized numerical features
     */
    public FeatureVector standardize() {
        if (numericalFeatures.length == 0) {
            return this;
        }
        
        // Calculate mean
        double sum = 0.0;
        int count = 0;
        for (double feature : numericalFeatures) {
            if (!Double.isNaN(feature)) {
                sum += feature;
                count++;
            }
        }
        
        if (count == 0) {
            return this;
        }
        
        double mean = sum / count;
        
        // Calculate standard deviation
        double sumSquaredDiff = 0.0;
        for (double feature : numericalFeatures) {
            if (!Double.isNaN(feature)) {
                double diff = feature - mean;
                sumSquaredDiff += diff * diff;
            }
        }
        
        double stdDev = Math.sqrt(sumSquaredDiff / count);
        
        if (stdDev <= 0) {
            return this; // No standardization needed
        }
        
        double[] standardized = new double[numericalFeatures.length];
        for (int i = 0; i < numericalFeatures.length; i++) {
            if (Double.isNaN(numericalFeatures[i])) {
                standardized[i] = Double.NaN;
            } else {
                standardized[i] = (numericalFeatures[i] - mean) / stdDev;
            }
        }
        
        return new FeatureVector(standardized, categoricalFeatures, namedFeatures, timestamp, playerId);
    }
    
    /**
     * Adds a numerical feature
     * @param value The feature value to add
     * @return A new feature vector with the added feature
     */
    public FeatureVector addNumericalFeature(double value) {
        double[] newFeatures = Arrays.copyOf(numericalFeatures, numericalFeatures.length + 1);
        newFeatures[numericalFeatures.length] = value;
        return new FeatureVector(newFeatures, categoricalFeatures, namedFeatures, timestamp, playerId);
    }
    
    /**
     * Adds a categorical feature
     * @param value The feature value to add
     * @return A new feature vector with the added feature
     */
    public FeatureVector addCategoricalFeature(String value) {
        String[] newFeatures = Arrays.copyOf(categoricalFeatures, categoricalFeatures.length + 1);
        newFeatures[categoricalFeatures.length] = value;
        return new FeatureVector(numericalFeatures, newFeatures, namedFeatures, timestamp, playerId);
    }
    
    /**
     * Adds a named feature
     * @param name The name of the feature
     * @param value The feature value
     * @return A new feature vector with the added feature
     */
    public FeatureVector addNamedFeature(String name, double value) {
        Map<String, Double> newFeatures = new HashMap<>(namedFeatures);
        newFeatures.put(name, value);
        return new FeatureVector(numericalFeatures, categoricalFeatures, newFeatures, timestamp, playerId);
    }
    
    /**
     * Merges this feature vector with another
     * @param other The other feature vector to merge with
     * @return A new feature vector containing all features from both
     */
    public FeatureVector merge(FeatureVector other) {
        if (other == null) {
            return this;
        }
        
        // Merge numerical features
        double[] mergedNumerical = Arrays.copyOf(numericalFeatures, 
                                               numericalFeatures.length + other.numericalFeatures.length);
        System.arraycopy(other.numericalFeatures, 0, mergedNumerical, 
                        numericalFeatures.length, other.numericalFeatures.length);
        
        // Merge categorical features
        String[] mergedCategorical = Arrays.copyOf(categoricalFeatures, 
                                                 categoricalFeatures.length + other.categoricalFeatures.length);
        System.arraycopy(other.categoricalFeatures, 0, mergedCategorical, 
                        categoricalFeatures.length, other.categoricalFeatures.length);
        
        // Merge named features
        Map<String, Double> mergedNamed = new HashMap<>(namedFeatures);
        mergedNamed.putAll(other.namedFeatures);
        
        return new FeatureVector(mergedNumerical, mergedCategorical, mergedNamed, timestamp, playerId);
    }
    
    /**
     * Creates a copy of this feature vector
     * @return A new feature vector with the same values
     */
    public FeatureVector copy() {
        return new FeatureVector(numericalFeatures, categoricalFeatures, namedFeatures, timestamp, playerId);
    }
    
    /**
     * Creates a feature vector with updated timestamp
     * @param newTimestamp The new timestamp
     * @return A new feature vector with the updated timestamp
     */
    public FeatureVector withTimestamp(long newTimestamp) {
        return new FeatureVector(numericalFeatures, categoricalFeatures, namedFeatures, newTimestamp, playerId);
    }
    
    /**
     * Creates a feature vector with updated player ID
     * @param newPlayerId The new player ID
     * @return A new feature vector with the updated player ID
     */
    public FeatureVector withPlayerId(String newPlayerId) {
        return new FeatureVector(numericalFeatures, categoricalFeatures, namedFeatures, timestamp, newPlayerId);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FeatureVector that = (FeatureVector) obj;
        return timestamp == that.timestamp &&
               Arrays.equals(numericalFeatures, that.numericalFeatures) &&
               Arrays.equals(categoricalFeatures, that.categoricalFeatures) &&
               Objects.equals(namedFeatures, that.namedFeatures) &&
               Objects.equals(playerId, that.playerId);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(namedFeatures, timestamp, playerId);
        result = 31 * result + Arrays.hashCode(numericalFeatures);
        result = 31 * result + Arrays.hashCode(categoricalFeatures);
        return result;
    }
    
    @Override
    public String toString() {
        return String.format("FeatureVector{numerical=%d, categorical=%d, named=%d, " +
                           "timestamp=%d, playerId='%s'}", 
                           numericalFeatures.length, categoricalFeatures.length, 
                           namedFeatures.size(), timestamp, playerId);
    }
    
    /**
     * Builder class for creating FeatureVector instances
     */
    public static class Builder {
        private double[] numericalFeatures = new double[0];
        private String[] categoricalFeatures = new String[0];
        private Map<String, Double> namedFeatures = new HashMap<>();
        private long timestamp = System.currentTimeMillis();
        private String playerId = "unknown";
        
        public Builder numericalFeatures(double... features) {
            this.numericalFeatures = features.clone();
            return this;
        }
        
        public Builder categoricalFeatures(String... features) {
            this.categoricalFeatures = features.clone();
            return this;
        }
        
        public Builder namedFeature(String name, double value) {
            this.namedFeatures.put(name, value);
            return this;
        }
        
        public Builder namedFeatures(Map<String, Double> features) {
            this.namedFeatures.putAll(features);
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder playerId(String playerId) {
            this.playerId = playerId;
            return this;
        }
        
        public FeatureVector build() {
            return new FeatureVector(numericalFeatures, categoricalFeatures, 
                                   namedFeatures, timestamp, playerId);
        }
    }
}