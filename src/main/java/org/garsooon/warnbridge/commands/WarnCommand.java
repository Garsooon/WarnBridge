package org.garsooon.warnbridge.commands;

import com.johnymuffin.beta.discordauth.DiscordAuthentication;
import com.projectposeidon.api.PoseidonUUID;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.garsooon.warnbridge.WarnBridge;
import org.garsooon.warnbridge.WarningStore;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

public class WarnCommand implements CommandExecutor {
    private static final Color EMBED_COLOR = new Color(0xE67E22);

    private final WarnBridge plugin;

    public WarnCommand(WarnBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("warnbridge.warn") && !sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to warn players.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /warn <player> <message>");
            return true;
        }

        String targetName = args[0];
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        UUID targetUuid = PoseidonUUID.getPlayerGracefulUUID(targetName);
        if (targetUuid == null) {
            sender.sendMessage(ChatColor.RED + "Could not resolve a player named '" + targetName + "'.");
            return true;
        }

        String warnedBy = sender instanceof Player ? sender.getName() : "Console";

        WarningStore warningStore = plugin.getWarningStore();
        warningStore.addWarning(targetUuid, warnedBy, message);

        sender.sendMessage(ChatColor.GREEN + "Warned " + ChatColor.AQUA + targetName + ChatColor.GREEN + ": " + message);

        Player onlineTarget = Bukkit.getServer().getPlayer(targetUuid);
        if (onlineTarget != null && onlineTarget.isOnline()) {
            deliverWarning(onlineTarget, warnedBy, message);
        }

        int warningCount = warningStore.getWarningCount(targetUuid);
        postToDiscord(targetUuid, targetName, warnedBy, message, warningCount);
        attemptDiscordDm(targetUuid, targetName, message);

        return true;
    }

    public static void deliverWarning(Player player, String warnedBy, String message) {
        player.sendMessage("");
        player.sendMessage(ChatColor.RED + "You have received a warning from " + warnedBy + ":");
        player.sendMessage(ChatColor.RED + message);
        player.sendMessage(ChatColor.RED + "Type /acknowledge to continue.");
        player.sendMessage("");
    }

    private void postToDiscord(UUID targetUuid, String targetName, String warnedBy, String message, int warningCount) {
        String channelId = plugin.getWarnBridgeConfig().getWarnChannelId();
        TextChannel channel = plugin.getDiscordBot().getJda().getTextChannelById(channelId);
        if (channel == null) {
            plugin.logger(Level.WARNING, "Could not find warn channel " + channelId);
            return;
        }

        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz").format(new Date());

        String description = "Moderator: `" + warnedBy + "` warned"
                + "\nPlayer: `" + targetName + "`"
                + "\nTotal Warnings: `" + warningCount + "`"
                + "\nMessage: `" + message + "`"
                + "\nTime: `" + time + "`";

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Warning")
                .setDescription(description)
                .setColor(EMBED_COLOR);

        channel.sendMessage(embed.build()).queue(
                sentMessage -> plugin.getWarningStore().setDiscordMessageId(targetUuid, sentMessage.getId()),
                error -> plugin.logger(Level.WARNING, "Failed to post warning embed: " + error.getMessage())
        );
    }

    private void attemptDiscordDm(UUID targetUuid, String targetName, String message) {
        Plugin discordAuthPlugin = Bukkit.getPluginManager().getPlugin("DiscordAuthentication");
        if (!(discordAuthPlugin instanceof DiscordAuthentication)) {
            return;
        }

        DiscordAuthentication discordAuth = (DiscordAuthentication) discordAuthPlugin;
        if (!discordAuth.getData().isUUIDAlreadyLinked(targetUuid)) {
            return;
        }

        long discordId = discordAuth.getData().getDiscordIDFromUUID(targetUuid);
        if (discordId == 0) {
            return;
        }

        EmbedBuilder dmEmbed = new EmbedBuilder()
                .setTitle("You have received a warning")
                .setDescription("Reason: `" + message + "`\n\nLog in and type `/acknowledge` to continue.")
                .setColor(EMBED_COLOR);

        plugin.getDiscordBot().getJda().retrieveUserById(discordId).queue(
                user -> user.openPrivateChannel().queue(
                        channel -> channel.sendMessageEmbeds(dmEmbed.build()).queue(),
                        error -> plugin.logger(Level.INFO, "Could not open a DM channel with " + targetName + "'s linked Discord account: " + error.getMessage())
                ),
                error -> plugin.logger(Level.INFO, "Could not resolve Discord user for " + targetName + ": " + error.getMessage())
        );
    }
}
