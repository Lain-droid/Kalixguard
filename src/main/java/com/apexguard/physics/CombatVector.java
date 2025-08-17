package com.apexguard.physics;

import java.util.Objects;

/**
 * Represents combat-related data including reach, angles, and timing
 */
public final class CombatVector {
    private final double reach;
    private final double angle;
    private final long timestamp;
    private final long hitTiming;
    private final boolean blocking;
    private final boolean critical;
    private final boolean offhand;
    private final String weapon;
    private final double damage;
    
    public CombatVector(double reach, double angle, long timestamp, long hitTiming,
                       boolean blocking, boolean critical, boolean offhand, 
                       String weapon, double damage) {
        this.reach = reach;
        this.angle = angle;
        this.timestamp = timestamp;
        this.hitTiming = hitTiming;
        this.blocking = blocking;
        this.critical = critical;
        this.offhand = offhand;
        this.weapon = weapon;
        this.damage = damage;
    }
    
    // Getters
    public double getReach() { return reach; }
    public double getAngle() { return angle; }
    public long getTimestamp() { return timestamp; }
    public long getHitTiming() { return hitTiming; }
    public boolean isBlocking() { return blocking; }
    public boolean isCritical() { return critical; }
    public boolean isOffhand() { return offhand; }
    public String getWeapon() { return weapon; }
    public double getDamage() { return damage; }
    
    /**
     * Calculates the reach efficiency (reach vs expected reach)
     * @param expectedReach The expected reach for the weapon
     * @return The reach efficiency ratio
     */
    public double getReachEfficiency(double expectedReach) {
        if (expectedReach <= 0) return Double.NaN;
        return reach / expectedReach;
    }
    
    /**
     * Calculates the angle efficiency (angle vs expected angle)
     * @param expectedAngle The expected angle for the weapon
     * @return The angle efficiency ratio
     */
    public double getAngleEfficiency(double expectedAngle) {
        if (expectedAngle <= 0) return Double.NaN;
        return angle / expectedAngle;
    }
    
    /**
     * Checks if the reach is suspiciously long
     * @param maxReach The maximum allowed reach
     * @return true if the reach exceeds the maximum
     */
    public boolean isReachSuspicious(double maxReach) {
        return reach > maxReach;
    }
    
    /**
     * Checks if the angle is suspiciously wide
     * @param maxAngle The maximum allowed angle
     * @return true if the angle exceeds the maximum
     */
    public boolean isAngleSuspicious(double maxAngle) {
        return angle > maxAngle;
    }
    
    /**
     * Checks if the hit timing is suspiciously fast
     * @param minTiming The minimum allowed timing
     * @return true if the timing is below the minimum
     */
    public boolean isTimingSuspicious(long minTiming) {
        return hitTiming < minTiming;
    }
    
    /**
     * Creates a new combat vector with updated reach
     * @param newReach The new reach value
     * @return A new combat vector with the updated reach
     */
    public CombatVector withReach(double newReach) {
        return new CombatVector(newReach, angle, timestamp, hitTiming, 
                              blocking, critical, offhand, weapon, damage);
    }
    
    /**
     * Creates a new combat vector with updated angle
     * @param newAngle The new angle value
     * @return A new combat vector with the updated angle
     */
    public CombatVector withAngle(double newAngle) {
        return new CombatVector(reach, newAngle, timestamp, hitTiming, 
                              blocking, critical, offhand, weapon, damage);
    }
    
    /**
     * Creates a new combat vector with updated timestamp
     * @param newTimestamp The new timestamp
     * @return A new combat vector with the updated timestamp
     */
    public CombatVector withTimestamp(long newTimestamp) {
        return new CombatVector(reach, angle, newTimestamp, hitTiming, 
                              blocking, critical, offhand, weapon, damage);
    }
    
    /**
     * Creates a new combat vector with updated hit timing
     * @param newHitTiming The new hit timing
     * @return A new combat vector with the updated hit timing
     */
    public CombatVector withHitTiming(long newHitTiming) {
        return new CombatVector(reach, angle, timestamp, newHitTiming, 
                              blocking, critical, offhand, weapon, damage);
    }
    
    /**
     * Creates a new combat vector with updated flags
     * @param newBlocking The new blocking flag
     * @param newCritical The new critical flag
     * @param newOffhand The new offhand flag
     * @return A new combat vector with the updated flags
     */
    public CombatVector withFlags(boolean newBlocking, boolean newCritical, boolean newOffhand) {
        return new CombatVector(reach, angle, timestamp, hitTiming, 
                              newBlocking, newCritical, newOffhand, weapon, damage);
    }
    
    /**
     * Creates a new combat vector with updated weapon and damage
     * @param newWeapon The new weapon
     * @param newDamage The new damage
     * @return A new combat vector with the updated weapon and damage
     */
    public CombatVector withWeapon(String newWeapon, double newDamage) {
        return new CombatVector(reach, angle, timestamp, hitTiming, 
                              blocking, critical, offhand, newWeapon, newDamage);
    }
    
    /**
     * Creates a copy of this combat vector
     * @return A new combat vector with the same values
     */
    public CombatVector copy() {
        return new CombatVector(reach, angle, timestamp, hitTiming, 
                              blocking, critical, offhand, weapon, damage);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CombatVector that = (CombatVector) obj;
        return Double.compare(that.reach, reach) == 0 &&
               Double.compare(that.angle, angle) == 0 &&
               timestamp == that.timestamp &&
               hitTiming == that.hitTiming &&
               blocking == that.blocking &&
               critical == that.critical &&
               offhand == that.offhand &&
               Double.compare(that.damage, damage) == 0 &&
               Objects.equals(weapon, that.weapon);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(reach, angle, timestamp, hitTiming, blocking, 
                          critical, offhand, weapon, damage);
    }
    
    @Override
    public String toString() {
        return String.format("CombatVector{reach=%.2f, angle=%.2f, timestamp=%d, " +
                           "hitTiming=%d, blocking=%s, critical=%s, offhand=%s, " +
                           "weapon='%s', damage=%.2f}",
                           reach, angle, timestamp, hitTiming, blocking, 
                           critical, offhand, weapon, damage);
    }
    
    /**
     * Builder class for creating CombatVector instances
     */
    public static class Builder {
        private double reach, angle, damage;
        private long timestamp, hitTiming;
        private boolean blocking, critical, offhand;
        private String weapon;
        
        public Builder() {
            this.timestamp = System.currentTimeMillis();
            this.weapon = "unknown";
        }
        
        public Builder reach(double reach) {
            this.reach = reach;
            return this;
        }
        
        public Builder angle(double angle) {
            this.angle = angle;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder hitTiming(long hitTiming) {
            this.hitTiming = hitTiming;
            return this;
        }
        
        public Builder blocking(boolean blocking) {
            this.blocking = blocking;
            return this;
        }
        
        public Builder critical(boolean critical) {
            this.critical = critical;
            return this;
        }
        
        public Builder offhand(boolean offhand) {
            this.offhand = offhand;
            return this;
        }
        
        public Builder weapon(String weapon) {
            this.weapon = weapon;
            return this;
        }
        
        public Builder damage(double damage) {
            this.damage = damage;
            return this;
        }
        
        public CombatVector build() {
            return new CombatVector(reach, angle, timestamp, hitTiming, 
                                  blocking, critical, offhand, weapon, damage);
        }
    }
}