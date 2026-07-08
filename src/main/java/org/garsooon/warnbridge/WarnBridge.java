package org.garsooon.warnbridge;

import com.johnymuffin.discordcore.DiscordBot;
import com.johnymuffin.discordcore.DiscordCore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.garsooon.warnbridge.commands.AcknowledgeCommand;
import org.garsooon.warnbridge.commands.WarnCommand;
import org.garsooon.warnbridge.commands.WarningsCommand;
import org.garsooon.warnbridge.listeners.AuthMeLoginListener;
import org.garsooon.warnbridge.listeners.FreezeListener;
import org.garsooon.warnbridge.listeners.JoinWarningListener;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WarnBridge extends JavaPlugin {
    private Logger log;
    private String pluginName;
    private PluginDescriptionFile pdf;

    private WarnBridgeConfig config;
    private WarningStore warningStore;
    private DiscordBot discordBot;

    @Override
    public void onEnable() {
        log = Bukkit.getServer().getLogger();
        pdf = getDescription();
        pluginName = pdf.getName();
        getDataFolder().mkdirs();

        DiscordCore discordCore = (DiscordCore) Bukkit.getPluginManager().getPlugin("DiscordCore");
        if (discordCore == null) {
            logger(Level.SEVERE, "DiscordCore not found, disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        discordBot = discordCore.getDiscordBot();

        config = new WarnBridgeConfig(this, new File(getDataFolder(), "config.yml"));
        warningStore = new WarningStore(new File(getDataFolder(), "warnings.yml"));

        getCommand("warn").setExecutor(new WarnCommand(this));
        getCommand("acknowledge").setExecutor(new AcknowledgeCommand(this));
        getCommand("warnings").setExecutor(new WarningsCommand(this));

        Bukkit.getPluginManager().registerEvents(new FreezeListener(this), this);

        if (Bukkit.getPluginManager().getPlugin("AuthMe") != null) {
            Bukkit.getPluginManager().registerEvents(new AuthMeLoginListener(this), this);
            logger(Level.INFO, "Hooked AuthMe login event for post-login warning delivery.");
        } else {
            Bukkit.getPluginManager().registerEvents(new JoinWarningListener(this), this);
            logger(Level.WARNING, "AuthMe not found, falling back to PlayerJoinEvent for warning delivery (no auth gate).");
        }

        if (Bukkit.getPluginManager().getPlugin("DiscordAuthentication") == null) {
            logger(Level.INFO, "DiscordAuthentication not found; warned players will not receive a Discord DM.");
        }

        logger(Level.INFO, "Is Loaded, Version: " + pdf.getVersion());
    }

    @Override
    public void onDisable() {
        logger(Level.INFO, "Is Unloaded, Version: " + pdf.getVersion());
    }

    public void logger(Level level, String message) {
        log.log(level, "[" + pluginName + "] " + message);
    }

    public WarnBridgeConfig getWarnBridgeConfig() {
        return config;
    }

    public WarningStore getWarningStore() {
        return warningStore;
    }

    public DiscordBot getDiscordBot() {
        return discordBot;
    }
}
