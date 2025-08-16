package com.apexguard.commands;

import com.apexguard.core.ApexGuard;
import com.apexguard.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class StatsCommand implements CommandExecutor {
    private final ApexGuard guard;

    public StatsCommand(ApexGuard guard) { this.guard = guard; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("apexguard.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        Player target;
        if (args.length >= 1) target = sender.getServer().getPlayer(args[0]); else target = sender instanceof Player ? (Player) sender : null;
        if (target == null) { sender.sendMessage(ChatColor.RED + "Player not found."); return true; }
        PlayerData d = guard.getPlayerData(target.getUniqueId());
        sender.sendMessage(ChatColor.YELLOW + "CPS window: " + d.getClickIntervalsMs().size());
        sender.sendMessage(ChatColor.YELLOW + "Yaw deltas: " + d.getYawDeltas().size());
        sender.sendMessage(ChatColor.YELLOW + "Speed samples: " + d.getHorizontalSpeed().size());
        return true;
    }
}