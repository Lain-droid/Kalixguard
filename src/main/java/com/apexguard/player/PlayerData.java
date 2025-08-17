package com.apexguard.player;

import com.apexguard.util.Stats;
import com.apexguard.util.RingBuffer;

import com.apexguard.util.ExponentialMovingAverage;
import com.apexguard.util.CUSUMDetector;
import com.apexguard.physics.MovementVector;
import com.apexguard.physics.CombatVector;
import com.apexguard.physics.NetworkVector;
import com.apexguard.ml.AnomalyDetector;
import com.apexguard.ml.FeatureVector;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.util.concurrent.AtomicDouble;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public final class PlayerData {
    private final UUID uuid;
    private final long joinTime;
    private final AtomicLong lastMoveTime;
    private final AtomicLong lastCombatTime;
    private final AtomicLong lastPacketTime;
    
    // Movement tracking
    private final RingBuffer<MovementVector> movementHistory;
    private final RingBuffer<Double> speedHistory;
    private final RingBuffer<Double> accelerationHistory;
    private final RingBuffer<Double> yawHistory;
    private final RingBuffer<Double> pitchHistory;
    private final RingBuffer<Boolean> onGroundHistory;
    private final RingBuffer<Boolean> inVehicleHistory;
    private final RingBuffer<Boolean> flyingHistory;
    private final RingBuffer<Boolean> sprintingHistory;
    private final RingBuffer<Boolean> sneakingHistory;
    
    // Combat tracking
    private final RingBuffer<CombatVector> combatHistory;
    private final RingBuffer<Double> cpsHistory;
    private final RingBuffer<Double> reachHistory;
    private final RingBuffer<Double> angleHistory;
    private final RingBuffer<Long> hitTimingHistory;
    private final RingBuffer<Boolean> blockingHistory;
    private final RingBuffer<Boolean> criticalHistory;
    
    // Network tracking
    private final RingBuffer<NetworkVector> networkHistory;
    private final RingBuffer<Long> pingHistory;
    private final RingBuffer<Long> jitterHistory;
    private final RingBuffer<Long> packetLossHistory;
    private final RingBuffer<Long> keepAliveHistory;
    private final RingBuffer<Long> rttHistory;
    
    // Inventory tracking
    private final RingBuffer<String> itemUseHistory;
    private final RingBuffer<Long> interactionTimingHistory;
    private final RingBuffer<Boolean> offhandHistory;
    private final RingBuffer<Boolean> ghostHandHistory;
    
    // Statistics and ML
    private final ExponentialMovingAverage speedEMA;
    private final ExponentialMovingAverage cpsEMA;
    private final ExponentialMovingAverage reachEMA;
    private final ExponentialMovingAverage pingEMA;
    
    private final CUSUMDetector speedCUSUM;
    private final CUSUMDetector cpsCUSUM;
    private final CUSUMDetector reachCUSUM;
    private final CUSUMDetector pingCUSUM;
    
    private final AnomalyDetector anomalyDetector;
    private final FeatureVector currentFeatures;
    
    // Violation tracking
    private final Map<String, AtomicInteger> violations;
    private final Map<String, AtomicDouble> riskScores;
    private final Queue<String> violationLog;
    
    // Performance tracking
    private final AtomicInteger totalPackets;
    private final AtomicInteger totalMoves;
    private final AtomicInteger totalCombat;
    private final AtomicLong lastUpdateTime;
    
    // Physics state
    private final AtomicReference<MovementVector> lastPosition;
    private final AtomicReference<MovementVector> lastVelocity;
    private final AtomicReference<CombatVector> lastCombat;
    
    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.joinTime = System.currentTimeMillis();
        this.lastMoveTime = new AtomicLong(joinTime);
        this.lastCombatTime = new AtomicLong(joinTime);
        this.lastPacketTime = new AtomicLong(joinTime);
        
        // Initialize buffers with appropriate sizes
        this.movementHistory = new RingBuffer<>(100);
        this.speedHistory = new RingBuffer<>(50);
        this.accelerationHistory = new RingBuffer<>(50);
        this.yawHistory = new RingBuffer<>(50);
        this.pitchHistory = new RingBuffer<>(50);
        this.onGroundHistory = new RingBuffer<>(50);
        this.inVehicleHistory = new RingBuffer<>(50);
        this.flyingHistory = new RingBuffer<>(50);
        this.sprintingHistory = new RingBuffer<>(50);
        this.sneakingHistory = new RingBuffer<>(50);
        
        this.combatHistory = new RingBuffer<>(100);
        this.cpsHistory = new RingBuffer<>(50);
        this.reachHistory = new RingBuffer<>(50);
        this.angleHistory = new RingBuffer<>(50);
        this.hitTimingHistory = new RingBuffer<>(50);
        this.blockingHistory = new RingBuffer<>(50);
        this.criticalHistory = new RingBuffer<>(50);
        
        this.networkHistory = new RingBuffer<>(100);
        this.pingHistory = new RingBuffer<>(50);
        this.jitterHistory = new RingBuffer<>(50);
        this.packetLossHistory = new RingBuffer<>(50);
        this.keepAliveHistory = new RingBuffer<>(50);
        this.rttHistory = new RingBuffer<>(50);
        
        this.itemUseHistory = new RingBuffer<>(50);
        this.interactionTimingHistory = new RingBuffer<>(50);
        this.offhandHistory = new RingBuffer<>(50);
        this.ghostHandHistory = new RingBuffer<>(50);
        
        // Initialize EMAs with appropriate decay factors
        this.speedEMA = new ExponentialMovingAverage(0.1);
        this.cpsEMA = new ExponentialMovingAverage(0.1);
        this.reachEMA = new ExponentialMovingAverage(0.1);
        this.pingEMA = new ExponentialMovingAverage(0.1);
        
        // Initialize CUSUM detectors
        this.speedCUSUM = new CUSUMDetector(2.0, 0.5);
        this.cpsCUSUM = new CUSUMDetector(2.0, 0.5);
        this.reachCUSUM = new CUSUMDetector(2.0, 0.5);
        this.pingCUSUM = new CUSUMDetector(2.0, 0.5);
        
        // Initialize ML components
        this.anomalyDetector = new AnomalyDetector();
        this.currentFeatures = new FeatureVector.Builder()
            .playerId(uuid.toString())
            .timestamp(joinTime)
            .build();
        
        // Initialize tracking maps
        this.violations = new ConcurrentHashMap<>();
        this.riskScores = new ConcurrentHashMap<>();
        this.violationLog = new ConcurrentLinkedQueue<>();
        
        // Initialize performance counters
        this.totalPackets = new AtomicInteger(0);
        this.totalMoves = new AtomicInteger(0);
        this.totalCombat = new AtomicInteger(0);
        this.lastUpdateTime = new AtomicLong(joinTime);
        
        // Initialize physics state
        this.lastPosition = new AtomicReference<>();
        this.lastVelocity = new AtomicReference<>();
        this.lastCombat = new AtomicReference<>();
    }
    
    // Getters
    public UUID getUuid() { return uuid; }
    public long getJoinTime() { return joinTime; }
    public long getLastMoveTime() { return lastMoveTime.get(); }
    public long getLastCombatTime() { return lastCombatTime.get(); }
    public long getLastPacketTime() { return lastPacketTime.get(); }
    
    // Movement data
    public RingBuffer<MovementVector> getMovementHistory() { return movementHistory; }
    public RingBuffer<Double> getSpeedHistory() { return speedHistory; }
    public RingBuffer<Double> getAccelerationHistory() { return accelerationHistory; }
    public RingBuffer<Double> getYawHistory() { return yawHistory; }
    public RingBuffer<Double> getPitchHistory() { return pitchHistory; }
    public RingBuffer<Boolean> getOnGroundHistory() { return onGroundHistory; }
    public RingBuffer<Boolean> getInVehicleHistory() { return inVehicleHistory; }
    public RingBuffer<Boolean> getFlyingHistory() { return flyingHistory; }
    public RingBuffer<Boolean> getSprintingHistory() { return sprintingHistory; }
    public RingBuffer<Boolean> getSneakingHistory() { return sneakingHistory; }
    
    // Combat data
    public RingBuffer<CombatVector> getCombatHistory() { return combatHistory; }
    public RingBuffer<Double> getCpsHistory() { return cpsHistory; }
    public RingBuffer<Double> getReachHistory() { return reachHistory; }
    public RingBuffer<Double> getAngleHistory() { return angleHistory; }
    public RingBuffer<Long> getHitTimingHistory() { return hitTimingHistory; }
    public RingBuffer<Boolean> getBlockingHistory() { return blockingHistory; }
    public RingBuffer<Boolean> getCriticalHistory() { return criticalHistory; }
    
    // Network data
    public RingBuffer<NetworkVector> getNetworkHistory() { return networkHistory; }
    public RingBuffer<Long> getPingHistory() { return pingHistory; }
    public RingBuffer<Long> getJitterHistory() { return jitterHistory; }
    public RingBuffer<Long> getPacketLossHistory() { return packetLossHistory; }
    public RingBuffer<Long> getKeepAliveHistory() { return keepAliveHistory; }
    public RingBuffer<Long> getRttHistory() { return rttHistory; }
    
    // Inventory data
    public RingBuffer<String> getItemUseHistory() { return itemUseHistory; }
    public RingBuffer<Long> getInteractionTimingHistory() { return interactionTimingHistory; }
    public RingBuffer<Boolean> getOffhandHistory() { return offhandHistory; }
    public RingBuffer<Boolean> getGhostHandHistory() { return ghostHandHistory; }
    
    // Statistics
    public ExponentialMovingAverage getSpeedEMA() { return speedEMA; }
    public ExponentialMovingAverage getCpsEMA() { return cpsEMA; }
    public ExponentialMovingAverage getReachEMA() { return reachEMA; }
    public ExponentialMovingAverage getPingEMA() { return pingEMA; }
    
    // CUSUM detectors
    public CUSUMDetector getSpeedCUSUM() { return speedCUSUM; }
    public CUSUMDetector getCpsCUSUM() { return cpsCUSUM; }
    public CUSUMDetector getReachCUSUM() { return reachCUSUM; }
    public CUSUMDetector getPingCUSUM() { return pingCUSUM; }
    
    // ML components
    public AnomalyDetector getAnomalyDetector() { return anomalyDetector; }
    public FeatureVector getCurrentFeatures() { return currentFeatures; }
    
    // Violation tracking
    public Map<String, AtomicInteger> getViolations() { return violations; }
    public Map<String, AtomicDouble> getRiskScores() { return riskScores; }
    public Queue<String> getViolationLog() { return violationLog; }
    
    // Performance counters
    public AtomicInteger getTotalPackets() { return totalPackets; }
    public AtomicInteger getTotalMoves() { return totalMoves; }
    public AtomicInteger getTotalCombat() { return totalCombat; }
    public AtomicLong getLastUpdateTime() { return lastUpdateTime; }
    
    // Physics state
    public AtomicReference<MovementVector> getLastPosition() { return lastPosition; }
    public AtomicReference<MovementVector> getLastVelocity() { return lastVelocity; }
    public AtomicReference<CombatVector> getLastCombat() { return lastCombat; }
    
    // Utility methods
    public void addViolation(String check, double severity) {
        violations.computeIfAbsent(check, k -> new AtomicInteger(0)).incrementAndGet();
        riskScores.computeIfAbsent(check, k -> new AtomicDouble(0.0)).addAndGet(severity);
        
        String logEntry = String.format("[%s] %s: %.2f", 
            System.currentTimeMillis(), check, severity);
        violationLog.offer(logEntry);
        
        // Keep only last 100 violations
        while (violationLog.size() > 100) {
            violationLog.poll();
        }
    }
    
    public int getViolationCount(String check) {
        return violations.getOrDefault(check, new AtomicInteger(0)).get();
    }
    
    public double getRiskScore(String check) {
        return riskScores.getOrDefault(check, new AtomicDouble(0.0)).get();
    }
    
    public double getTotalRiskScore() {
        return riskScores.values().stream()
            .mapToDouble(AtomicDouble::get)
            .sum();
    }
    
    public void updateLastMoveTime() {
        lastMoveTime.set(System.currentTimeMillis());
    }
    
    public void updateLastCombatTime() {
        lastCombatTime.set(System.currentTimeMillis());
    }
    
    public void updateLastPacketTime() {
        lastPacketTime.set(System.currentTimeMillis());
    }
    
    public void incrementPacketCount() {
        totalPackets.incrementAndGet();
    }
    
    public void incrementMoveCount() {
        totalMoves.incrementAndGet();
    }
    
    public void incrementCombatCount() {
        totalCombat.incrementAndGet();
    }
    
    public void updateLastUpdateTime() {
        lastUpdateTime.set(System.currentTimeMillis());
    }
    
    // Statistics methods
    public double getAverageSpeed() {
        return speedHistory.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }
    
    public double getAverageCPS() {
        return cpsHistory.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }
    
    public double getAverageReach() {
        return reachHistory.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }
    
    public double getAveragePing() {
        return pingHistory.stream()
            .mapToDouble(Long::doubleValue)
            .average()
            .orElse(0.0);
    }
    
    public double getSpeedStandardDeviation() {
        return Stats.standardDeviation(speedHistory.stream().mapToDouble(Double::doubleValue).toArray());
    }
    
    public double getCPSStandardDeviation() {
        return Stats.standardDeviation(cpsHistory.stream().mapToDouble(Double::doubleValue).toArray());
    }
    
    public double getReachStandardDeviation() {
        return Stats.standardDeviation(reachHistory.stream().mapToDouble(Double::doubleValue).toArray());
    }
    
    public double getPingStandardDeviation() {
        return Stats.standardDeviation(pingHistory.stream()
            .mapToDouble(Long::doubleValue)
            .toArray());
    }
    
    // Timing tracking methods for compatibility with existing listeners
    private final AtomicLong lastUseTime = new AtomicLong(0);
    private final AtomicLong lastPlaceTime = new AtomicLong(0);
    private final AtomicLong lastClickTime = new AtomicLong(0);
    
    // Ring buffers for timing intervals
    private final RingBuffer<Long> useIntervals = new RingBuffer<>(100);
    private final RingBuffer<Long> placeIntervals = new RingBuffer<>(100);
    private final RingBuffer<Long> clickIntervals = new RingBuffer<>(100);
    
    // Ring buffers for movement deltas
    private final RingBuffer<Double> yawDeltas = new RingBuffer<>(100);
    private final RingBuffer<Double> pitchDeltas = new RingBuffer<>(100);
    private final RingBuffer<Double> horizontalSpeed = new RingBuffer<>(100);
    
    // Getters for timing data
    public long getLastUseMs() { return lastUseTime.get(); }
    public void setLastUseMs(long time) { lastUseTime.set(time); }
    public RingBuffer<Long> getUseIntervalsMs() { return useIntervals; }
    
    public long getLastPlaceMs() { return lastPlaceTime.get(); }
    public void setLastPlaceMs(long time) { lastPlaceTime.set(time); }
    public RingBuffer<Long> getPlaceIntervalsMs() { return placeIntervals; }
    
    public long getLastClickMs() { return lastClickTime.get(); }
    public void setLastClickMs(long time) { lastClickTime.set(time); }
    public RingBuffer<Long> getClickIntervalsMs() { return clickIntervals; }
    
    // Getters for movement data
    public RingBuffer<Double> getYawDeltas() { return yawDeltas; }
    public RingBuffer<Double> getPitchDeltas() { return pitchDeltas; }
    public RingBuffer<Double> getHorizontalSpeed() { return horizontalSpeed; }
    
    // Network data accessor for compatibility
    public NetworkStats getNetwork() {
        // Return a compatibility wrapper for the old NetworkStats interface
        return new NetworkStats() {
            @Override
            public boolean isInitialized() {
                return !networkHistory.isEmpty();
            }
            
            @Override
            public double getRttMs() {
                if (rttHistory.isEmpty()) return 50.0;
                return rttHistory.stream().mapToDouble(Long::doubleValue).average().orElse(50.0);
            }
        };
    }
    
    // Cleanup method
    public void cleanup() {
        // Clear all buffers to free memory
        movementHistory.clear();
        speedHistory.clear();
        accelerationHistory.clear();
        yawHistory.clear();
        pitchHistory.clear();
        onGroundHistory.clear();
        inVehicleHistory.clear();
        flyingHistory.clear();
        sprintingHistory.clear();
        sneakingHistory.clear();
        
        combatHistory.clear();
        cpsHistory.clear();
        reachHistory.clear();
        angleHistory.clear();
        hitTimingHistory.clear();
        blockingHistory.clear();
        criticalHistory.clear();
        
        networkHistory.clear();
        pingHistory.clear();
        jitterHistory.clear();
        packetLossHistory.clear();
        keepAliveHistory.clear();
        rttHistory.clear();
        
        itemUseHistory.clear();
        interactionTimingHistory.clear();
        offhandHistory.clear();
        ghostHandHistory.clear();
        
        violations.clear();
        riskScores.clear();
        violationLog.clear();
    }
}