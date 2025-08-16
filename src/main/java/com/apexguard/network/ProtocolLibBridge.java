package com.apexguard.network;

import com.apexguard.checks.registry.CheckRegistry;
import com.apexguard.core.ConfigManager;
import com.apexguard.core.PlayerManager;
import com.apexguard.core.TaskEngine;
import com.apexguard.logging.JsonLogger;
import com.apexguard.player.PlayerData;
import com.apexguard.player.ReplayBuffer;
import com.apexguard.player.events.PacketDirection;
import com.apexguard.player.events.PacketRecord;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public final class ProtocolLibBridge implements ProtocolBridge {
    private final Plugin plugin;
    private final TaskEngine taskEngine;
    private final PlayerManager playerManager;
    private final ConfigManager configManager;

    private ProtocolManager protocolManager;

    public ProtocolLibBridge(final Plugin plugin,
                             final TaskEngine taskEngine,
                             final PlayerManager playerManager,
                             final ConfigManager configManager) {
        this.plugin = plugin;
        this.taskEngine = taskEngine;
        this.playerManager = playerManager;
        this.configManager = configManager;
    }

    @Override
    public void register(CheckRegistry registry, PlayerManager playerManager, JsonLogger logger) {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        Set<PacketType> listen = EnumSet.of(
                PacketType.Play.Client.ARM_ANIMATION,
                PacketType.Play.Client.USE_ENTITY,
                PacketType.Play.Client.FLYING,
                PacketType.Play.Client.LOOK,
                PacketType.Play.Client.POSITION,
                PacketType.Play.Client.POSITION_LOOK,
                PacketType.Play.Client.BLOCK_DIG,
                PacketType.Play.Client.BLOCK_PLACE,
                PacketType.Play.Client.KEEP_ALIVE,

                PacketType.Play.Server.KEEP_ALIVE
        );

        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, listen) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                handle(event, PacketDirection.SERVERBOUND);
            }

            @Override
            public void onPacketSending(PacketEvent event) {
                handle(event, PacketDirection.CLIENTBOUND);
            }

            private void handle(PacketEvent event, PacketDirection direction) {
                final UUID uuid = event.getPlayer().getUniqueId();
                final PlayerData data = playerManager.getPlayerData(uuid);
                final PacketContainer packet = event.getPacket();

                // Minimal main-thread work: capture metadata, enqueue
                final long now = System.nanoTime();
                final String type = packet.getType().name();
                final int size = approximateSize(packet);

                final PacketRecord record = new PacketRecord(now, direction, type, size);
                final ReplayBuffer replay = data.getReplayBuffer();
                replay.append(record, configManager.getMaxReplayBytesPerPlayer());

                final PacketContainer clone = packet.shallowClone();
                taskEngine.submit(() -> registry.onPacketAsync(data, clone));
            }
        });
    }

    @Override
    public void shutdown() {
        if (protocolManager != null) {
            protocolManager.removePacketListeners(plugin);
        }
    }

    private int approximateSize(PacketContainer packet) {
        // Fallback size estimation; ProtocolLib can serialize but avoid cost
        return 64;
    }
}