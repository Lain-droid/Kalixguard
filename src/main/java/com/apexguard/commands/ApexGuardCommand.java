package com.apexguard.commands;

import com.apexguard.core.ApexGuard;
import com.apexguard.core.ConfigManager;
import com.apexguard.logging.JsonLogger;
import com.apexguard.player.ReplayBuffer;
import com.apexguard.player.events.PacketRecord;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ApexGuardCommand implements CommandExecutor, TabCompleter {
    private final ApexGuard apexGuard;

    public ApexGuardCommand(ApexGuard apexGuard) { this.apexGuard = apexGuard; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("apexguard.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "/apexguard profile <name> | verbose <player> | replay export <player> [seconds]");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "profile":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.YELLOW + "Active profile: " + apexGuard.getConfigManager().getActiveProfileName());
                } else {
                    sender.sendMessage(ChatColor.RED + "Profile switching at runtime not supported yet; edit config.yml and /reload.");
                }
                return true;
            case "verbose":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /apexguard verbose <player>");
                    return true;
                }
                Player target = sender.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found");
                    return true;
                }
                JsonLogger logger = apexGuard.getJsonLogger();
                boolean newState;
                if (logger.isVerbose(target.getUniqueId())) { logger.setVerbose(target.getUniqueId(), false); newState = false; }
                else { logger.setVerbose(target.getUniqueId(), true); newState = true; }
                sender.sendMessage(ChatColor.GREEN + "Verbose for " + target.getName() + ": " + newState);
                return true;
            case "replay":
                if (args.length >= 2 && args[1].equalsIgnoreCase("export")) {
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /apexguard replay export <player> [seconds]");
                        return true;
                    }
                    Player p = sender.getServer().getPlayer(args[2]);
                    if (p == null) {
                        sender.sendMessage(ChatColor.RED + "Player not found");
                        return true;
                    }
                    int seconds = apexGuard.getConfigManager().getReplaySeconds();
                    if (args.length >= 4) {
                        try { seconds = Integer.parseInt(args[3]); } catch (NumberFormatException ignored) {}
                    }
                    exportReplay(sender, p.getUniqueId(), seconds);
                    return true;
                }
                break;
        }
        return false;
    }

    private void exportReplay(CommandSender sender, UUID uuid, int seconds) {
        ReplayBuffer rb = apexGuard.getPlayerData(uuid).getReplayBuffer();
        PacketRecord[] records = rb.snapshot();
        File out = new File(apexGuard.getJsonLogger().getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        File dir = new File(out.getParentFile(), "ApexGuard-replays");
        dir.mkdirs();
        File file = new File(dir, uuid + "-" + System.currentTimeMillis() + ".jsonl");
        long cutoffNanos = System.nanoTime() - seconds * 1_000_000_000L;
        try (FileOutputStream fos = new FileOutputStream(file, false)) {
            for (PacketRecord r : records) {
                if (r.nanoTime < cutoffNanos) continue;
                String line = "{\"ts\":" + r.nanoTime + ",\"dir\":\"" + r.direction + "\",\"type\":\"" + r.type + "\",\"size\":" + r.approxSize + "}";
                fos.write(line.getBytes(StandardCharsets.UTF_8));
                fos.write('\n');
            }
            sender.sendMessage(ChatColor.GREEN + "Replay exported: " + file.getName());
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Failed to export: " + e.getMessage());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.add("profile"); out.add("verbose"); out.add("replay");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("profile")) {
            out.add("safe_default"); out.add("aggressive_qa"); out.add("tournament_strict");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("replay")) {
            out.add("export");
        }
        return out;
    }
}