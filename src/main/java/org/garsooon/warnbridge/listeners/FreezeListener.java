package org.garsooon.warnbridge.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.garsooon.warnbridge.WarnBridge;
import org.garsooon.warnbridge.commands.WarnCommand;
import uk.org.whoami.authme.cache.auth.PlayerCache;

import java.util.UUID;

public class FreezeListener implements Listener {
    private final WarnBridge plugin;

    public FreezeListener(WarnBridge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!isFrozen(player)) {
            return;
        }

        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!isFrozen(player)) {
            return;
        }

        String commandLabel = event.getMessage().split(" ")[0].toLowerCase();
        if (commandLabel.equals("/acknowledge") || commandLabel.equals("/ack")) {
            return;
        }

        event.setCancelled(true);
        repeatWarning(player, uuid);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        if (isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    private boolean isFrozen(Player player) {
        if (!plugin.getWarningStore().hasPendingWarning(player.getUniqueId())) {
            return false;
        }

        if (Bukkit.getPluginManager().getPlugin("AuthMe") != null) {
            return PlayerCache.getInstance().isAuthenticated(player.getName().toLowerCase());
        }

        return true;
    }

    private void repeatWarning(Player player, UUID uuid) {
        String warnedBy = plugin.getWarningStore().getWarnedBy(uuid);
        String message = plugin.getWarningStore().getPendingMessage(uuid);
        WarnCommand.deliverWarning(player, warnedBy, message);
    }
}
