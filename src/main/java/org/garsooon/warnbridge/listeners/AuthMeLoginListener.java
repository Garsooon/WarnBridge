package org.garsooon.warnbridge.listeners;

import com.legacyminecraft.poseidon.event.PoseidonCustomListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.garsooon.warnbridge.WarnBridge;
import org.garsooon.warnbridge.commands.WarnCommand;
import uk.org.whoami.authme.event.AuthLoginEvent;

import java.util.UUID;

public class AuthMeLoginListener implements PoseidonCustomListener {
    private final WarnBridge plugin;

    public AuthMeLoginListener(WarnBridge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAuthLogin(AuthLoginEvent event) {
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
