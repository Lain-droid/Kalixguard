package com.apexguard.checks.api;

import com.apexguard.player.PlayerData;
import com.apexguard.network.PacketView;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public interface Check {
    String name();
    String category();
    void handlePacketAsync(PlayerData data, PacketView packet);
    default void handleTickSync(PlayerData data) {}
    default void handleCombatEvent(PlayerData attacker, EntityDamageByEntityEvent event) {}
}