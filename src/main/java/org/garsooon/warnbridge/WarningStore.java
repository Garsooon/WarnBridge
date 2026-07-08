package org.garsooon.warnbridge;

import org.bukkit.util.config.Configuration;

import java.io.File;
import java.util.UUID;

public class WarningStore extends Configuration {

    public WarningStore(File dataFile) {
        super(dataFile);
        load();
    }

    public void addWarning(UUID target, String warnedBy, String message) {
        String base = key(target);
        long timestamp = System.currentTimeMillis();
        int newCount = getWarningCount(target) + 1;

        setProperty(base + ".message", message);
        setProperty(base + ".warned-by", warnedBy);
        setProperty(base + ".timestamp", timestamp);
        setProperty(base + ".pending", true);
        setProperty(base + ".count", newCount);

        String historyBase = base + ".history." + newCount;
        setProperty(historyBase + ".warned-by", warnedBy);
        setProperty(historyBase + ".message", message);
        setProperty(historyBase + ".timestamp", timestamp);

        save();
    }

    public int getWarningCount(UUID target) {
        Object count = getProperty(key(target) + ".count");
        return count instanceof Number ? ((Number) count).intValue() : 0;
    }

    public WarningEntry getHistoryEntry(UUID target, int index) {
        String base = key(target) + ".history." + index;
        String warnedBy = getString(base + ".warned-by");
        if (warnedBy == null) {
            return null;
        }

        String message = getString(base + ".message");
        Object timestampRaw = getProperty(base + ".timestamp");
        long timestamp = timestampRaw instanceof Number ? ((Number) timestampRaw).longValue() : 0L;

        return new WarningEntry(warnedBy, message, timestamp);
    }

    public static final class WarningEntry {
        private final String warnedBy;
        private final String message;
        private final long timestamp;

        public WarningEntry(String warnedBy, String message, long timestamp) {
            this.warnedBy = warnedBy;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getWarnedBy() {
            return warnedBy;
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public boolean hasPendingWarning(UUID target) {
        return Boolean.TRUE.equals(getProperty(key(target) + ".pending"));
    }

    public String getPendingMessage(UUID target) {
        return getString(key(target) + ".message");
    }

    public String getWarnedBy(UUID target) {
        return getString(key(target) + ".warned-by");
    }

    public void setDiscordMessageId(UUID target, String messageId) {
        setProperty(key(target) + ".discord-message-id", messageId);
        save();
    }

    public String getDiscordMessageId(UUID target) {
        return getString(key(target) + ".discord-message-id");
    }

    public void acknowledge(UUID target) {
        setProperty(key(target) + ".pending", false);
        save();
    }

    private String key(UUID target) {
        return "warnings." + target.toString();
    }
}
