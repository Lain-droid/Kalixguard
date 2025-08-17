package com.apexguard.physics;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class PhysicsEngine {
    public double computeRelaxMultiplier(Player player) {
        if (player == null) return 1.0;
        // Disable/strongly relax in vehicles or elytra
        if (player.isInsideVehicle()) return 4.0;
        if (player.isGliding()) return 5.0;
        if (player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE)) return 3.0;

        boolean inWater = player.getLocation().getBlock().isLiquid();
        boolean onIce = isBelow(player, Material.ICE) || isBelow(player, Material.PACKED_ICE) || isBelow(player, Material.BLUE_ICE) || isBelow(player, Material.FROSTED_ICE);
        boolean onHoney = isBelow(player, Material.HONEY_BLOCK);
        boolean onSlime = isBelow(player, Material.SLIME_BLOCK);

        double m = 1.0;
        if (inWater) m += 1.2;
        if (onIce) m += 0.9;
        if (onHoney) m += 0.6;
        if (onSlime) m += 1.0;

        if (player.isSprinting()) m += 0.2;
        if (player.isFlying()) m += 1.5;
        if (player.isSneaking()) m -= 0.2;

        int speedLevel = player.getPotionEffect(PotionEffectType.SPEED) != null ? player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() + 1 : 0;
        if (speedLevel > 0) m += Math.min(0.3 * speedLevel, 1.2);

        return Math.max(0.6, m);
    }

    private boolean isBelow(Player player, Material mat) {
        return player.getLocation().clone().subtract(0, 1, 0).getBlock().getType() == mat;
    }
}