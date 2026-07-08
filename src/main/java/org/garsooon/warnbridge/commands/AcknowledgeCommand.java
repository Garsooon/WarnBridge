package org.garsooon.warnbridge.commands;

import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.garsooon.warnbridge.WarnBridge;

import java.util.UUID;
import java.util.logging.Level;

public class AcknowledgeCommand implements CommandExecutor {
    private final WarnBridge plugin;

    public AcknowledgeCommand(WarnBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by players.");
            return true;
        }

        if (!sender.hasPermission("warnbridge.acknowledge") && !sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (!plugin.getWarningStore().hasPendingWarning(uuid)) {
            player.sendMessage(ChatColor.YELLOW + "You have no warnings to acknowledge.");
            return true;
        }

        reactToDiscordMessage(uuid);
        plugin.getWarningStore().acknowledge(uuid);
        player.sendMessage(ChatColor.GREEN + "You have acknowledged your warning.");
        return true;
    }

    private void reactToDiscordMessage(UUID uuid) {
        String messageId = plugin.getWarningStore().getDiscordMessageId(uuid);
        if (messageId == null) {
            return;
        }

        String channelId = plugin.getWarnBridgeConfig().getWarnChannelId();
        TextChannel channel = plugin.getDiscordBot().getJda().getTextChannelById(channelId);
        if (channel == null) {
            return;
        }

        channel.retrieveMessageById(messageId).queue(
                message -> message.addReaction("✅").queue(),
                error -> plugin.logger(Level.INFO, "Could not react to warning message " + messageId + ": " + error.getMessage())
        );
    }
}
