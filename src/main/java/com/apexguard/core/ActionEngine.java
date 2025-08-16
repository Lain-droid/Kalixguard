package com.apexguard.core;

import com.apexguard.checks.api.FlagContext;
import com.apexguard.logging.JsonLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ActionEngine {
    private final Plugin plugin;
    private final ConfigManager config;
    private final JsonLogger logger;

    private final Map<UUID, Integer> violationScore = new ConcurrentHashMap<>();

    public ActionEngine(Plugin plugin, ConfigManager config, JsonLogger logger) {
        this.plugin = plugin;
        this.config = config;
        this.logger = logger;
    }

    public void flag(UUID uuid, FlagContext ctx) {
        int inc = Math.max(1, (int) Math.round(ctx.severity));
        int newVl = violationScore.merge(uuid, inc, Integer::sum);

        logger.log(Map.of(
                "type", "flag",
                "check", ctx.check,
                "category", ctx.category,
                "severity", ctx.severity,
                "vl", newVl,
                "player", uuid.toString()
        ));

        if (newVl >= config.actionFlag()) notifyStaff(uuid, ctx, newVl);
        if (newVl >= config.actionWarn()) warn(uuid, ctx, newVl);
        if (newVl >= config.actionSlow()) slow(uuid, ctx, newVl);
        if (newVl >= config.actionKick()) kick(uuid, ctx, newVl);
    }

    private void notifyStaff(UUID uuid, FlagContext ctx, int vl) {
        String msg = ChatColor.RED + "[ApexGuard] " + ChatColor.YELLOW + uuid + ChatColor.GRAY +
                " flagged " + ctx.check + " (" + ctx.category + ") VL=" + vl;
        Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("apexguard.admin")).forEach(p -> p.sendMessage(msg));
    }

    private void warn(UUID uuid, FlagContext ctx, int vl) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) p.sendMessage(ChatColor.RED + "Warning: Suspicious behavior detected (" + ctx.check + ")");
    }

    private void slow(UUID uuid, FlagContext ctx, int vl) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 4, 1));
        }
    }

    private void kick(UUID uuid, FlagContext ctx, int vl) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            p.kickPlayer("Kicked by AntiCheat: " + ctx.check);
        }
    }

    public void reset(UUID uuid) { violationScore.remove(uuid); }
}