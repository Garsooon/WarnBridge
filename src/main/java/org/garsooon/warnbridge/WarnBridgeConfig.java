package org.garsooon.warnbridge;

import org.bukkit.util.config.Configuration;

import java.io.File;

public class WarnBridgeConfig extends Configuration {

    public WarnBridgeConfig(WarnBridge plugin, File configFile) {
        super(configFile);
        reload();
    }

    private void reload() {
        load();
        if (getString("warn-channel-id") == null) {
            setProperty("warn-channel-id", "CHANNEL_ID_HERE");
        }
        save();
    }

    public String getWarnChannelId() {
        return getString("warn-channel-id", "CHANNEL_ID_HERE");
    }
}
