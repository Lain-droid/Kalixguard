package com.apexguard.physics;

import java.util.Objects;

/**
 * Represents network-related data including ping, jitter, and packet loss
 */
public final class NetworkVector {
    private final long ping;
    private final long jitter;
    private final long packetLoss;
    private final long keepAlive;
    private final long rtt;
    private final long timestamp;
    private final String address;
    private final int protocolVersion;
    private final boolean proxy;
    
    public NetworkVector(long ping, long jitter, long packetLoss, long keepAlive, 
                        long rtt, long timestamp, String address, int protocolVersion, boolean proxy) {
        this.ping = ping;
        this.jitter = jitter;
        this.packetLoss = packetLoss;
        this.keepAlive = keepAlive;
        this.rtt = rtt;
        this.timestamp = timestamp;
        this.address = address;
        this.protocolVersion = protocolVersion;
        this.proxy = proxy;
    }
    
    // Getters
    public long getPing() { return ping; }
    public long getJitter() { return jitter; }
    public long getPacketLoss() { return packetLoss; }
    public long getKeepAlive() { return keepAlive; }
    public long getRtt() { return rtt; }
    public long getTimestamp() { return timestamp; }
    public String getAddress() { return address; }
    public int getProtocolVersion() { return protocolVersion; }
    public boolean isProxy() { return proxy; }
    
    /**
     * Calculates the ping stability (lower values indicate more stable connection)
     * @return The ping stability metric
     */
    public double getPingStability() {
        if (ping <= 0) return Double.NaN;
        return (double) jitter / ping;
    }
    
    /**
     * Calculates the connection quality score (0-1, higher is better)
     * @return The connection quality score
     */
    public double getConnectionQuality() {
        if (ping <= 0) return 0.0;
        
        // Normalize ping (0-100ms = 1.0, 1000ms+ = 0.0)
        double pingScore = Math.max(0.0, 1.0 - (ping / 1000.0));
        
        // Normalize jitter (0-10ms = 1.0, 100ms+ = 0.0)
        double jitterScore = Math.max(0.0, 1.0 - (jitter / 100.0));
        
        // Normalize packet loss (0% = 1.0, 10%+ = 0.0)
        double lossScore = Math.max(0.0, 1.0 - (packetLoss / 10.0));
        
        // Weighted average
        return (pingScore * 0.5 + jitterScore * 0.3 + lossScore * 0.2);
    }
    
    /**
     * Checks if the ping is suspiciously low (potential lag spoofing)
     * @param minPing The minimum realistic ping
     * @return true if the ping is suspiciously low
     */
    public boolean isPingSuspiciouslyLow(long minPing) {
        return ping < minPing;
    }
    
    /**
     * Checks if the jitter is suspiciously low (potential lag spoofing)
     * @param minJitter The minimum realistic jitter
     * @return true if the jitter is suspiciously low
     */
    public boolean isJitterSuspiciouslyLow(long minJitter) {
        return jitter < minJitter;
    }
    
    /**
     * Checks if the packet loss is suspiciously low (potential lag spoofing)
     * @param minLoss The minimum realistic packet loss
     * @return true if the packet loss is suspiciously low
     */
    public boolean isPacketLossSuspiciouslyLow(long minLoss) {
        return packetLoss < minLoss;
    }
    
    /**
     * Checks if the RTT is consistent with ping
     * @param tolerance The tolerance for RTT vs ping difference
     * @return true if RTT is consistent with ping
     */
    public boolean isRTTConsistent(long tolerance) {
        return Math.abs(rtt - ping) <= tolerance;
    }
    
    /**
     * Calculates the network anomaly score
     * @return A score indicating how anomalous the network behavior is
     */
    public double getAnomalyScore() {
        double score = 0.0;
        
        // Ping anomalies
        if (ping < 5) score += 0.3; // Suspiciously low ping
        if (ping > 2000) score += 0.2; // Extremely high ping
        
        // Jitter anomalies
        if (jitter < 1) score += 0.3; // Suspiciously low jitter
        if (jitter > 500) score += 0.2; // Extremely high jitter
        
        // Packet loss anomalies
        if (packetLoss < 0.1) score += 0.2; // Suspiciously low packet loss
        if (packetLoss > 50) score += 0.3; // Extremely high packet loss
        
        // RTT consistency
        if (!isRTTConsistent(50)) score += 0.2; // RTT inconsistent with ping
        
        return Math.min(1.0, score);
    }
    
    /**
     * Creates a new network vector with updated ping
     * @param newPing The new ping value
     * @return A new network vector with the updated ping
     */
    public NetworkVector withPing(long newPing) {
        return new NetworkVector(newPing, jitter, packetLoss, keepAlive, 
                               rtt, timestamp, address, protocolVersion, proxy);
    }
    
    /**
     * Creates a new network vector with updated jitter
     * @param newJitter The new jitter value
     * @return A new network vector with the updated jitter
     */
    public NetworkVector withJitter(long newJitter) {
        return new NetworkVector(ping, newJitter, packetLoss, keepAlive, 
                               rtt, timestamp, address, protocolVersion, proxy);
    }
    
    /**
     * Creates a new network vector with updated packet loss
     * @param newPacketLoss The new packet loss value
     * @return A new network vector with the updated packet loss
     */
    public NetworkVector withPacketLoss(long newPacketLoss) {
        return new NetworkVector(ping, jitter, newPacketLoss, keepAlive, 
                               rtt, timestamp, address, protocolVersion, proxy);
    }
    
    /**
     * Creates a new network vector with updated RTT
     * @param newRtt The new RTT value
     * @return A new network vector with the updated RTT
     */
    public NetworkVector withRtt(long newRtt) {
        return new NetworkVector(ping, jitter, packetLoss, keepAlive, 
                               newRtt, timestamp, address, protocolVersion, proxy);
    }
    
    /**
     * Creates a new network vector with updated timestamp
     * @param newTimestamp The new timestamp
     * @return A new network vector with the updated timestamp
     */
    public NetworkVector withTimestamp(long newTimestamp) {
        return new NetworkVector(ping, jitter, packetLoss, keepAlive, 
                               rtt, newTimestamp, address, protocolVersion, proxy);
    }
    
    /**
     * Creates a new network vector with updated address
     * @param newAddress The new address
     * @return A new network vector with the updated address
     */
    public NetworkVector withAddress(String newAddress) {
        return new NetworkVector(ping, jitter, packetLoss, keepAlive, 
                               rtt, timestamp, newAddress, protocolVersion, proxy);
    }
    
    /**
     * Creates a new network vector with updated protocol version
     * @param newProtocolVersion The new protocol version
     * @return A new network vector with the updated protocol version
     */
    public NetworkVector withProtocolVersion(int newProtocolVersion) {
        return new NetworkVector(ping, jitter, packetLoss, keepAlive, 
                               rtt, timestamp, address, newProtocolVersion, proxy);
    }
    
    /**
     * Creates a new network vector with updated proxy flag
     * @param newProxy The new proxy flag
     * @return A new network vector with the updated proxy flag
     */
    public NetworkVector withProxy(boolean newProxy) {
        return new NetworkVector(ping, jitter, packetLoss, keepAlive, 
                               rtt, timestamp, address, protocolVersion, newProxy);
    }
    
    /**
     * Creates a copy of this network vector
     * @return A new network vector with the same values
     */
    public NetworkVector copy() {
        return new NetworkVector(ping, jitter, packetLoss, keepAlive, 
                               rtt, timestamp, address, protocolVersion, proxy);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        NetworkVector that = (NetworkVector) obj;
        return ping == that.ping &&
               jitter == that.jitter &&
               packetLoss == that.packetLoss &&
               keepAlive == that.keepAlive &&
               rtt == that.rtt &&
               timestamp == that.timestamp &&
               protocolVersion == that.protocolVersion &&
               proxy == that.proxy &&
               Objects.equals(address, that.address);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(ping, jitter, packetLoss, keepAlive, rtt, 
                          timestamp, address, protocolVersion, proxy);
    }
    
    @Override
    public String toString() {
        return String.format("NetworkVector{ping=%d, jitter=%d, packetLoss=%d, " +
                           "keepAlive=%d, rtt=%d, timestamp=%d, address='%s', " +
                           "protocolVersion=%d, proxy=%s}",
                           ping, jitter, packetLoss, keepAlive, rtt, 
                           timestamp, address, protocolVersion, proxy);
    }
    
    /**
     * Builder class for creating NetworkVector instances
     */
    public static class Builder {
        private long ping, jitter, packetLoss, keepAlive, rtt, timestamp;
        private String address;
        private int protocolVersion;
        private boolean proxy;
        
        public Builder() {
            this.timestamp = System.currentTimeMillis();
            this.address = "unknown";
            this.protocolVersion = 0;
        }
        
        public Builder ping(long ping) {
            this.ping = ping;
            return this;
        }
        
        public Builder jitter(long jitter) {
            this.jitter = jitter;
            return this;
        }
        
        public Builder packetLoss(long packetLoss) {
            this.packetLoss = packetLoss;
            return this;
        }
        
        public Builder keepAlive(long keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }
        
        public Builder rtt(long rtt) {
            this.rtt = rtt;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder address(String address) {
            this.address = address;
            return this;
        }
        
        public Builder protocolVersion(int protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }
        
        public Builder proxy(boolean proxy) {
            this.proxy = proxy;
            return this;
        }
        
        public NetworkVector build() {
            return new NetworkVector(ping, jitter, packetLoss, keepAlive, 
                                   rtt, timestamp, address, protocolVersion, proxy);
        }
    }
}