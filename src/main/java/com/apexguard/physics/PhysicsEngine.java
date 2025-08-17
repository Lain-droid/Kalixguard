package com.apexguard.physics;

import com.apexguard.core.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Advanced physics engine for anti-cheat detection
 * Handles movement validation, reach calculations, and physics-based checks
 */
public final class PhysicsEngine {
    private final Map<String, PlayerPhysicsState> playerStates;
    private final AtomicLong lastUpdateTime;
    
    // Physics constants
    private static final double GRAVITY = 0.08;
    private static final double AIR_RESISTANCE = 0.98;
    private static final double WATER_RESISTANCE = 0.8;
    private static final double LAVA_RESISTANCE = 0.5;
    private static final double ICE_FRICTION = 0.6;
    private static final double SOUL_SAND_FRICTION = 0.4;
    private static final double HONEY_FRICTION = 0.2;
    private static final double SLIME_BOUNCE = 0.6;
    
    // Block-specific properties
    private static final Map<Material, BlockProperties> BLOCK_PROPERTIES = new ConcurrentHashMap<>();
    
    static {
        // Initialize block properties
        BLOCK_PROPERTIES.put(Material.ICE, new BlockProperties(ICE_FRICTION, 1.0, 0.0));
        BLOCK_PROPERTIES.put(Material.PACKED_ICE, new BlockProperties(ICE_FRICTION, 1.0, 0.0));
        BLOCK_PROPERTIES.put(Material.BLUE_ICE, new BlockProperties(ICE_FRICTION, 1.0, 0.0));
        BLOCK_PROPERTIES.put(Material.FROSTED_ICE, new BlockProperties(ICE_FRICTION, 1.0, 0.0));
        
        BLOCK_PROPERTIES.put(Material.SOUL_SAND, new BlockProperties(SOUL_SAND_FRICTION, 0.4, 0.0));
        BLOCK_PROPERTIES.put(Material.SOUL_SOIL, new BlockProperties(SOUL_SAND_FRICTION, 0.4, 0.0));
        
        BLOCK_PROPERTIES.put(Material.HONEY_BLOCK, new BlockProperties(HONEY_FRICTION, 0.4, 0.0));
        BLOCK_PROPERTIES.put(Material.SLIME_BLOCK, new BlockProperties(1.0, 1.0, SLIME_BOUNCE));
        
        BLOCK_PROPERTIES.put(Material.WATER, new BlockProperties(1.0, WATER_RESISTANCE, 0.0));
        BLOCK_PROPERTIES.put(Material.LAVA, new BlockProperties(1.0, LAVA_RESISTANCE, 0.0));
        
        BLOCK_PROPERTIES.put(Material.COBWEB, new BlockProperties(0.3, 0.3, 0.0));
        BLOCK_PROPERTIES.put(Material.SWEET_BERRY_BUSH, new BlockProperties(0.3, 0.3, 0.0));
    }
    
    public PhysicsEngine() {
        this.playerStates = new ConcurrentHashMap<>();
        this.lastUpdateTime = new AtomicLong(System.currentTimeMillis());
    }
    
    /**
     * Computes the relaxation multiplier for a player based on their environment
     * @param player The player to compute the multiplier for
     * @return The relaxation multiplier (higher values = more lenient checks)
     */
    public double computeRelaxMultiplier(Player player) {
        if (player == null) return 1.0;
        
        Location location = player.getLocation();
        Block block = location.getBlock();
        Material material = block.getType();
        
        double multiplier = 1.0;
        
        // Check for special blocks
        BlockProperties properties = BLOCK_PROPERTIES.get(material);
        if (properties != null) {
            multiplier *= properties.getRelaxMultiplier();
        }
        
        // Check for water/lava
        if (player.isInWater()) {
            multiplier *= 1.5; // More lenient in water
        }
        if (player.isInLava()) {
            multiplier *= 2.0; // Much more lenient in lava
        }
        
        // Check for flying
        if (player.isFlying()) {
            multiplier *= 1.3; // More lenient when flying
        }
        
        // Check for vehicles
        if (player.isInsideVehicle()) {
            multiplier *= 1.2; // More lenient in vehicles
        }
        
        // Check for elytra
        if (player.isGliding()) {
            multiplier *= 1.4; // More lenient with elytra
        }
        
        // Check for potion effects
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
            multiplier *= 1.2; // More lenient with speed
        }
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS)) {
            multiplier *= 0.8; // Less lenient with slowness
        }
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.DOLPHINS_GRACE)) {
            multiplier *= 1.3; // More lenient with dolphin's grace
        }
        
        return Math.max(0.5, Math.min(3.0, multiplier)); // Clamp between 0.5 and 3.0
    }
    
    /**
     * Validates movement between two positions
     * @param player The player
     * @param from The starting position
     * @param to The ending position
     * @param deltaTime The time difference in milliseconds
     * @return Movement validation result
     */
    public MovementValidationResult validateMovement(Player player, Location from, Location to, long deltaTime) {
        if (player == null || from == null || to == null) {
            return new MovementValidationResult(false, 0.0, "Invalid parameters");
        }
        
        // Calculate distances
        double horizontalDistance = Math.sqrt(
            Math.pow(to.getX() - from.getX(), 2) + 
            Math.pow(to.getZ() - from.getZ(), 2)
        );
        double verticalDistance = to.getY() - from.getY();
        double totalDistance = Math.sqrt(
            Math.pow(horizontalDistance, 2) + 
            Math.pow(verticalDistance, 2)
        );
        
        // Calculate speed
        double speed = totalDistance / (deltaTime / 1000.0); // blocks per second
        
        // Get player physics state
        PlayerPhysicsState state = getPlayerPhysicsState(player);
        
        // Validate against physics limits
        double maxSpeed = getMaxSpeed(player);
        double maxHorizontalSpeed = getMaxHorizontalSpeed(player);
        double maxVerticalSpeed = getMaxVerticalSpeed(player);
        
        boolean isValid = true;
        double violationScore = 0.0;
        StringBuilder details = new StringBuilder();
        
        // Check total speed
        if (speed > maxSpeed) {
            isValid = false;
            violationScore += (speed - maxSpeed) / maxSpeed;
            details.append(String.format("Speed: %.2f > %.2f, ", speed, maxSpeed));
        }
        
        // Check horizontal speed
        if (horizontalDistance > maxHorizontalSpeed * (deltaTime / 1000.0)) {
            isValid = false;
            violationScore += (horizontalDistance - maxHorizontalSpeed * (deltaTime / 1000.0)) / maxHorizontalSpeed;
            details.append(String.format("Horizontal: %.2f > %.2f, ", horizontalDistance, maxHorizontalSpeed * (deltaTime / 1000.0)));
        }
        
        // Check vertical speed
        if (Math.abs(verticalDistance) > maxVerticalSpeed * (deltaTime / 1000.0)) {
            isValid = false;
            violationScore += Math.abs(verticalDistance - maxVerticalSpeed * (deltaTime / 1000.0)) / maxVerticalSpeed;
            details.append(String.format("Vertical: %.2f > %.2f, ", Math.abs(verticalDistance), maxVerticalSpeed * (deltaTime / 1000.0)));
        }
        
        // Check for impossible movements
        if (isImpossibleMovement(from, to, player)) {
            isValid = false;
            violationScore += 2.0; // High penalty for impossible movements
            details.append("Impossible movement detected, ");
        }
        
        // Update player state
        state.updateMovement(from, to, deltaTime, speed, isValid);
        
        return new MovementValidationResult(isValid, violationScore, details.toString());
    }
    
    /**
     * Calculates the maximum allowed speed for a player
     * @param player The player
     * @return The maximum speed in blocks per second
     */
    public double getMaxSpeed(Player player) {
        if (player == null) return 1.0;
        
        double baseSpeed = 0.8; // Base walking speed
        
        // Apply potion effects
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
            int level = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SPEED).getAmplifier();
            baseSpeed *= (1.0 + (level + 1) * 0.2);
        }
        
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS)) {
            int level = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS).getAmplifier();
            baseSpeed *= (1.0 - (level + 1) * 0.15);
        }
        
        // Apply environment modifiers
        baseSpeed *= computeRelaxMultiplier(player);
        
        // Apply sprinting modifier
        if (player.isSprinting()) {
            baseSpeed *= 1.3;
        }
        
        return Math.max(0.1, Math.min(5.0, baseSpeed)); // Clamp between 0.1 and 5.0
    }
    
    /**
     * Calculates the maximum allowed horizontal speed for a player
     * @param player The player
     * @return The maximum horizontal speed in blocks per second
     */
    public double getMaxHorizontalSpeed(Player player) {
        return getMaxSpeed(player) * 0.8; // Horizontal speed is typically lower
    }
    
    /**
     * Calculates the maximum allowed vertical speed for a player
     * @param player The player
     * @return The maximum vertical speed in blocks per second
     */
    public double getMaxVerticalSpeed(Player player) {
        if (player == null) return 1.0;
        
        double baseSpeed = 1.0;
        
        // Flying players have higher vertical speed
        if (player.isFlying()) {
            baseSpeed *= 2.0;
        }
        
        // Elytra gliding
        if (player.isGliding()) {
            baseSpeed *= 3.0;
        }
        
        // Apply environment modifiers
        baseSpeed *= computeRelaxMultiplier(player);
        
        return Math.max(0.1, Math.min(10.0, baseSpeed)); // Clamp between 0.1 and 10.0
    }
    
    /**
     * Checks if a movement is physically impossible
     * @param from The starting position
     * @param to The ending position
     * @param player The player
     * @return true if the movement is impossible
     */
    private boolean isImpossibleMovement(Location from, Location to, Player player) {
        if (player == null || from == null || to == null) return false;
        
        // Check for teleportation (instant movement over large distances)
        double distance = from.distance(to);
        if (distance > 100.0) { // 100 blocks is suspicious
            return true;
        }
        
        // Check for moving through solid blocks
        if (hasCollision(from, to, player)) {
            return true;
        }
        
        // Check for moving through unloaded chunks
        if (!from.getWorld().isChunkLoaded(from.getBlockX() >> 4, from.getBlockZ() >> 4) ||
            !to.getWorld().isChunkLoaded(to.getBlockX() >> 4, to.getBlockZ() >> 4)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if there's a collision between two points
     * @param from The starting point
     * @param to The ending point
     * @param player The player
     * @return true if there's a collision
     */
    private boolean hasCollision(Location from, Location to, Player player) {
        if (player == null || from == null || to == null) return false;
        
        // Simple collision check - can be enhanced with more sophisticated algorithms
        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        
        if (distance == 0) return false;
        
        direction.normalize();
        
        // Check multiple points along the path
        int steps = Math.max(1, (int) (distance * 2)); // Check every 0.5 blocks
        
        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            Location checkLocation = from.clone().add(direction.clone().multiply(distance * t));
            
            Block block = checkLocation.getBlock();
            if (block.getType().isSolid() && !canPassThrough(block, player)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if a player can pass through a block
     * @param block The block to check
     * @param player The player
     * @return true if the player can pass through
     */
    private boolean canPassThrough(Block block, Player player) {
        if (player == null || block == null) return false;
        
        Material material = block.getType();
        
        // Flying players can pass through some blocks
        if (player.isFlying()) {
            return material == Material.AIR || 
                   material == Material.CAVE_AIR || 
                   material == Material.VOID_AIR ||
                   material == Material.WATER ||
                   material == Material.LAVA;
        }
        
        // Normal players can only pass through non-solid blocks
        return !material.isSolid();
    }
    
    /**
     * Calculates reach distance between two entities
     * @param attacker The attacking entity
     * @param target The target entity
     * @return The reach distance
     */
    public double calculateReach(Player attacker, org.bukkit.entity.Entity target) {
        if (attacker == null || target == null) return 0.0;
        
        Location attackerLoc = attacker.getLocation();
        Location targetLoc = target.getLocation();
        
        // Calculate 3D distance
        double distance = attackerLoc.distance(targetLoc);
        
        // Adjust for entity hitboxes
        double attackerRadius = getEntityRadius(attacker);
        double targetRadius = getEntityRadius(target);
        
        // Effective reach is distance minus entity radii
        double effectiveReach = distance - attackerRadius - targetRadius;
        
        return Math.max(0.0, effectiveReach);
    }
    
    /**
     * Gets the radius of an entity for reach calculations
     * @param entity The entity
     * @return The entity radius
     */
    private double getEntityRadius(org.bukkit.entity.Entity entity) {
        if (entity == null) return 0.0;
        
        // Default entity radius
        double radius = 0.3;
        
        // Adjust for specific entity types
        if (entity instanceof Player) {
            radius = 0.6; // Player hitbox
        } else if (entity instanceof org.bukkit.entity.Zombie) {
            radius = 0.6;
        } else if (entity instanceof org.bukkit.entity.Skeleton) {
            radius = 0.6;
        } else if (entity instanceof org.bukkit.entity.Creeper) {
            radius = 0.6;
        }
        
        return radius;
    }
    
    /**
     * Gets the physics state for a player
     * @param player The player
     * @return The physics state
     */
    private PlayerPhysicsState getPlayerPhysicsState(Player player) {
        if (player == null) return null;
        
        return playerStates.computeIfAbsent(player.getUniqueId().toString(), 
            k -> new PlayerPhysicsState(player.getUniqueId().toString()));
    }
    
    /**
     * Updates the physics engine
     */
    public void update() {
        long currentTime = System.currentTimeMillis();
        lastUpdateTime.set(currentTime);
        
        // Clean up old player states
        playerStates.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getLastUpdateTime() > 300000); // 5 minutes
    }
    
    /**
     * Gets statistics about the physics engine
     */
    public PhysicsEngineStats getStats() {
        return new PhysicsEngineStats(
            playerStates.size(),
            lastUpdateTime.get()
        );
    }
    
    // Inner classes
    public static class MovementValidationResult {
        public final boolean isValid;
        public final double violationScore;
        public final String details;
        
        public MovementValidationResult(boolean isValid, double violationScore, String details) {
            this.isValid = isValid;
            this.violationScore = violationScore;
            this.details = details;
        }
    }
    
    public static class BlockProperties {
        private final double friction;
        private final double resistance;
        private final double bounce;
        
        public BlockProperties(double friction, double resistance, double bounce) {
            this.friction = friction;
            this.resistance = resistance;
            this.bounce = bounce;
        }
        
        public double getFriction() { return friction; }
        public double getResistance() { return resistance; }
        public double getBounce() { return bounce; }
        
        public double getRelaxMultiplier() {
            // Higher friction and resistance = more lenient checks
            return 1.0 + (1.0 - friction) * 0.5 + (1.0 - resistance) * 0.3;
        }
    }
    
    public static class PlayerPhysicsState {
        private final String playerId;
        private final AtomicLong lastUpdateTime;
        private Location lastLocation;
        private double lastSpeed;
        private boolean lastValid;
        private int consecutiveViolations;
        private int totalMovements;
        
        public PlayerPhysicsState(String playerId) {
            this.playerId = playerId;
            this.lastUpdateTime = new AtomicLong(System.currentTimeMillis());
            this.consecutiveViolations = 0;
            this.totalMovements = 0;
        }
        
        public void updateMovement(Location from, Location to, long deltaTime, double speed, boolean isValid) {
            this.lastLocation = to;
            this.lastSpeed = speed;
            this.lastValid = isValid;
            this.lastUpdateTime.set(System.currentTimeMillis());
            this.totalMovements++;
            
            if (!isValid) {
                this.consecutiveViolations++;
            } else {
                this.consecutiveViolations = 0;
            }
        }
        
        public String getPlayerId() { return playerId; }
        public Location getLastLocation() { return lastLocation; }
        public double getLastSpeed() { return lastSpeed; }
        public boolean isLastValid() { return lastValid; }
        public int getConsecutiveViolations() { return consecutiveViolations; }
        public int getTotalMovements() { return totalMovements; }
        public long getLastUpdateTime() { return lastUpdateTime.get(); }
    }
    
    public static class PhysicsEngineStats {
        public final int activePlayers;
        public final long lastUpdateTime;
        
        public PhysicsEngineStats(int activePlayers, long lastUpdateTime) {
            this.activePlayers = activePlayers;
            this.lastUpdateTime = lastUpdateTime;
        }
    }
}