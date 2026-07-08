package org.garsooon.warnbridge.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.garsooon.warnbridge.WarnBridge;
import org.garsooon.warnbridge.commands.WarnCommand;

import java.util.UUID;

public class FreezeListener implements Listener {
    private final WarnBridge plugin;

    public FreezeListener(WarnBridge plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getWarningStore().hasPendingWarning(player.getUniqueId())) {
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

        if (!plugin.getWarningStore().hasPendingWarning(uuid)) {
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
        if (plugin.getWarningStore().hasPendingWarning(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    private void repeatWarning(Player player, UUID uuid) {
        String warnedBy = plugin.getWarningStore().getWarnedBy(uuid);
        String message = plugin.getWarningStore().getPendingMessage(uuid);
        WarnCommand.deliverWarning(player, warnedBy, message);
    }
}
