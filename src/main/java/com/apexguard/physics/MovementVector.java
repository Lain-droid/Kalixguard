package com.apexguard.physics;

import java.util.Objects;

/**
 * Represents a 3D movement vector with position, velocity, and acceleration
 */
public final class MovementVector {
    private final double x, y, z;
    private final double yaw, pitch;
    private final long timestamp;
    private final boolean onGround;
    private final boolean inVehicle;
    private final boolean flying;
    private final boolean sprinting;
    private final boolean sneaking;
    
    public MovementVector(double x, double y, double z, double yaw, double pitch, 
                         long timestamp, boolean onGround, boolean inVehicle, 
                         boolean flying, boolean sprinting, boolean sneaking) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.timestamp = timestamp;
        this.onGround = onGround;
        this.inVehicle = inVehicle;
        this.flying = flying;
        this.sprinting = sprinting;
        this.sneaking = sneaking;
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getYaw() { return yaw; }
    public double getPitch() { return pitch; }
    public long getTimestamp() { return timestamp; }
    public boolean isOnGround() { return onGround; }
    public boolean isInVehicle() { return inVehicle; }
    public boolean isFlying() { return flying; }
    public boolean isSprinting() { return sprinting; }
    public boolean isSneaking() { return sneaking; }
    
    /**
     * Calculates the distance to another movement vector
     * @param other The other movement vector
     * @return The Euclidean distance
     */
    public double distanceTo(MovementVector other) {
        if (other == null) return Double.NaN;
        
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    /**
     * Calculates the horizontal distance to another movement vector
     * @param other The other movement vector
     * @return The horizontal distance (ignoring Y coordinate)
     */
    public double horizontalDistanceTo(MovementVector other) {
        if (other == null) return Double.NaN;
        
        double dx = x - other.x;
        double dz = z - other.z;
        
        return Math.sqrt(dx * dx + dz * dz);
    }
    
    /**
     * Calculates the vertical distance to another movement vector
     * @param other The other movement vector
     * @return The vertical distance (Y coordinate difference)
     */
    public double verticalDistanceTo(MovementVector other) {
        if (other == null) return Double.NaN;
        
        return Math.abs(y - other.y);
    }
    
    /**
     * Calculates the angle difference in yaw
     * @param other The other movement vector
     * @return The yaw angle difference in degrees
     */
    public double yawDifference(MovementVector other) {
        if (other == null) return Double.NaN;
        
        double diff = Math.abs(yaw - other.yaw);
        return Math.min(diff, 360.0 - diff);
    }
    
    /**
     * Calculates the angle difference in pitch
     * @param other The other movement vector
     * @return The pitch angle difference in degrees
     */
    public double pitchDifference(MovementVector other) {
        if (other == null) return Double.NaN;
        
        return Math.abs(pitch - other.pitch);
    }
    
    /**
     * Creates a new movement vector with updated position
     * @param newX The new X coordinate
     * @param newY The new Y coordinate
     * @param newZ The new Z coordinate
     * @return A new movement vector with the updated position
     */
    public MovementVector withPosition(double newX, double newY, double newZ) {
        return new MovementVector(newX, newY, newZ, yaw, pitch, timestamp, 
                                onGround, inVehicle, flying, sprinting, sneaking);
    }
    
    /**
     * Creates a new movement vector with updated rotation
     * @param newYaw The new yaw angle
     * @param newPitch The new pitch angle
     * @return A new movement vector with the updated rotation
     */
    public MovementVector withRotation(double newYaw, double newPitch) {
        return new MovementVector(x, y, z, newYaw, newPitch, timestamp, 
                                onGround, inVehicle, flying, sprinting, sneaking);
    }
    
    /**
     * Creates a new movement vector with updated timestamp
     * @param newTimestamp The new timestamp
     * @return A new movement vector with the updated timestamp
     */
    public MovementVector withTimestamp(long newTimestamp) {
        return new MovementVector(x, y, z, yaw, pitch, newTimestamp, 
                                onGround, inVehicle, flying, sprinting, sneaking);
    }
    
    /**
     * Creates a new movement vector with updated flags
     * @param newOnGround The new onGround flag
     * @param newInVehicle The new inVehicle flag
     * @param newFlying The new flying flag
     * @param newSprinting The new sprinting flag
     * @param newSneaking The new sneaking flag
     * @return A new movement vector with the updated flags
     */
    public MovementVector withFlags(boolean newOnGround, boolean newInVehicle, 
                                   boolean newFlying, boolean newSprinting, boolean newSneaking) {
        return new MovementVector(x, y, z, yaw, pitch, timestamp, 
                                newOnGround, newInVehicle, newFlying, newSprinting, newSneaking);
    }
    
    /**
     * Creates a copy of this movement vector
     * @return A new movement vector with the same values
     */
    public MovementVector copy() {
        return new MovementVector(x, y, z, yaw, pitch, timestamp, 
                                onGround, inVehicle, flying, sprinting, sneaking);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MovementVector that = (MovementVector) obj;
        return Double.compare(that.x, x) == 0 &&
               Double.compare(that.y, y) == 0 &&
               Double.compare(that.z, z) == 0 &&
               Double.compare(that.yaw, yaw) == 0 &&
               Double.compare(that.pitch, pitch) == 0 &&
               timestamp == that.timestamp &&
               onGround == that.onGround &&
               inVehicle == that.inVehicle &&
               flying == that.flying &&
               sprinting == that.sprinting &&
               sneaking == that.sneaking;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, yaw, pitch, timestamp, onGround, inVehicle, flying, sprinting, sneaking);
    }
    
    @Override
    public String toString() {
        return String.format("MovementVector{x=%.2f, y=%.2f, z=%.2f, yaw=%.2f, pitch=%.2f, " +
                           "timestamp=%d, onGround=%s, inVehicle=%s, flying=%s, sprinting=%s, sneaking=%s}",
                           x, y, z, yaw, pitch, timestamp, onGround, inVehicle, flying, sprinting, sneaking);
    }
    
    /**
     * Builder class for creating MovementVector instances
     */
    public static class Builder {
        private double x, y, z;
        private double yaw, pitch;
        private long timestamp;
        private boolean onGround, inVehicle, flying, sprinting, sneaking;
        
        public Builder() {
            this.timestamp = System.currentTimeMillis();
        }
        
        public Builder position(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }
        
        public Builder rotation(double yaw, double pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder onGround(boolean onGround) {
            this.onGround = onGround;
            return this;
        }
        
        public Builder inVehicle(boolean inVehicle) {
            this.inVehicle = inVehicle;
            return this;
        }
        
        public Builder flying(boolean flying) {
            this.flying = flying;
            return this;
        }
        
        public Builder sprinting(boolean sprinting) {
            this.sprinting = sprinting;
            return this;
        }
        
        public Builder sneaking(boolean sneaking) {
            this.sneaking = sneaking;
            return this;
        }
        
        public MovementVector build() {
            return new MovementVector(x, y, z, yaw, pitch, timestamp, 
                                    onGround, inVehicle, flying, sprinting, sneaking);
        }
    }
}