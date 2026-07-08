package org.garsooon.warnbridge.commands;

import com.projectposeidon.api.PoseidonUUID;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.garsooon.warnbridge.WarnBridge;
import org.garsooon.warnbridge.WarningStore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class WarningsCommand implements CommandExecutor {
    private final WarnBridge plugin;

    public WarningsCommand(WarnBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("warnbridge.warnings") && !sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to view warnings.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /warnings <player> [#]");
            return true;
        }

        String targetName = args[0];
        UUID targetUuid = PoseidonUUID.getPlayerGracefulUUID(targetName);
        if (targetUuid == null) {
            sender.sendMessage(ChatColor.RED + "Could not resolve a player named '" + targetName + "'.");
            return true;
        }

        WarningStore warningStore = plugin.getWarningStore();

        if (args.length >= 2) {
            showSingleWarning(sender, warningStore, targetUuid, targetName, args[1]);
            return true;
        }

        listWarnings(sender, warningStore, targetUuid, targetName);
        return true;
    }

    private void listWarnings(CommandSender sender, WarningStore warningStore, UUID targetUuid, String targetName) {
        int count = warningStore.getWarningCount(targetUuid);
        if (count == 0) {
            sender.sendMessage(ChatColor.YELLOW + targetName + " has no warnings.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "Warnings for " + ChatColor.AQUA + targetName + ChatColor.GOLD + ":");
        for (int i = 1; i <= count; i++) {
            WarningStore.WarningEntry entry = warningStore.getHistoryEntry(targetUuid, i);
            String warnedBy = entry != null ? entry.getWarnedBy() : "Unknown";
            sender.sendMessage(ChatColor.YELLOW + "#" + i + " " + ChatColor.AQUA + warnedBy);
        }
    }

    private void showSingleWarning(CommandSender sender, WarningStore warningStore, UUID targetUuid, String targetName, String indexArg) {
        int index;
        try {
            index = Integer.parseInt(indexArg);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "'" + indexArg + "' is not a valid warning number.");
            return;
        }

        WarningStore.WarningEntry entry = warningStore.getHistoryEntry(targetUuid, index);
        if (entry == null) {
            sender.sendMessage(ChatColor.RED + "Warning #" + index + " not found for " + targetName + ".");
            return;
        }

        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz").format(new Date(entry.getTimestamp()));
        sender.sendMessage(ChatColor.GOLD + "Warning #" + index + " for " + ChatColor.AQUA + targetName + ChatColor.GOLD + ":");
        sender.sendMessage(ChatColor.YELLOW + "Moderator: " + ChatColor.AQUA + entry.getWarnedBy());
        sender.sendMessage(ChatColor.YELLOW + "Reason: " + ChatColor.GRAY + entry.getMessage());
        sender.sendMessage(ChatColor.YELLOW + "Time: " + ChatColor.GRAY + time);
    }
}
