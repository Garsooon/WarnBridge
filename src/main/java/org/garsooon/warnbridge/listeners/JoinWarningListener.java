package org.garsooon.warnbridge.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.garsooon.warnbridge.WarnBridge;
import org.garsooon.warnbridge.commands.WarnCommand;

import java.util.UUID;

//fallback for authme listener
public class JoinWarningListener implements Listener {
    private final WarnBridge plugin;

    public JoinWarningListener(WarnBridge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!plugin.getWarningStore().hasPendingWarning(uuid)) {
            return;
        }

        String warnedBy = plugin.getWarningStore().getWarnedBy(uuid);
        String message = plugin.getWarningStore().getPendingMessage(uuid);

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (player.isOnline()) {
                WarnCommand.deliverWarning(player, warnedBy, message);
            }
        }, 5L);
    }
}
