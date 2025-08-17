package com.apexguard.player;

/**
 * Compatibility interface for network statistics
 */
public interface NetworkStats {
    /**
     * Check if the network stats are initialized
     * @return true if initialized
     */
    boolean isInitialized();
    
    /**
     * Get the round-trip time in milliseconds
     * @return RTT in ms
     */
    double getRttMs();
    
    /**
     * Observe a new RTT measurement
     * @param rttMs The RTT in milliseconds
     */
    default void observeRtt(long rttMs) {
        // Default implementation does nothing
        // This is for compatibility with existing code
    }
}