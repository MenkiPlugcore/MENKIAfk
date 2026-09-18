package store.menkiestes.menkiafk.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import store.menkiestes.menkiafk.MenkiAfkPlugin;
import store.menkiestes.menkiafk.afk.AfkManager;
import store.menkiestes.menkiafk.stats.StatsManager;
import store.menkiestes.menkiafk.util.Text;

import java.util.Locale;
import java.util.UUID;

public final class MenkiAfkExpansion extends PlaceholderExpansion {
    private final MenkiAfkPlugin plugin;
    private final AfkManager manager;
    private final StatsManager statsManager;

    public MenkiAfkExpansion(MenkiAfkPlugin plugin, AfkManager manager, StatsManager statsManager) {
        this.plugin = plugin;
        this.manager = manager;
        this.statsManager = statsManager;
    }

    @Override
    public String getIdentifier() {
        return "menkiafk";
    }

    @Override
    public String getAuthor() {
        return "MENKIESTES";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String getRequiredPlugin() {
        return "MENKIAFK";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return "";

        UUID id = player.getUniqueId();
        String key = params == null ? "" : params.toLowerCase(Locale.ROOT);

        return switch (key) {
            // Hot-path placeholders deliberately avoid StatsManager#snapshot().
            // TAB/scoreboard plugins can request these very frequently.
            case "status" -> manager.placeholderStatus(id);
            case "reason" -> manager.placeholderReason(id);
            case "time" -> manager.placeholderTime(id);
            case "type" -> manager.placeholderType(id);

            // Statistics are resolved lazily only when a stats placeholder is actually requested.
            case "last_afk" -> lastAfk(statsManager.snapshot(id));
            case "stats_today" -> Text.duration(statsManager.snapshot(id).todayMillis());
            case "stats_week" -> Text.duration(statsManager.snapshot(id).weekMillis());
            case "stats_total" -> Text.duration(statsManager.snapshot(id).totalMillis());
            case "stats_total_seconds" -> String.valueOf(statsManager.snapshot(id).totalMillis() / 1_000L);
            case "stats_total_minutes" -> String.valueOf(statsManager.snapshot(id).totalMillis() / 60_000L);
            case "stats_total_hours" -> String.valueOf(statsManager.snapshot(id).totalMillis() / 3_600_000L);
            case "stats_sessions" -> String.valueOf(statsManager.snapshot(id).sessions());
            case "stats_manual_sessions" -> String.valueOf(statsManager.snapshot(id).manualSessions());
            case "stats_auto_sessions" -> String.valueOf(statsManager.snapshot(id).autoSessions());
            case "stats_longest" -> Text.duration(statsManager.snapshot(id).longestMillis());
            default -> null;
        };
    }

    private String lastAfk(StatsManager.Snapshot stats) {
        if (stats.lastAfkAt() <= 0L) {
            return Text.color(plugin.getConfig().getString("placeholder.never-afk-text", "Belum pernah"));
        }
        long elapsed = Math.max(0L, System.currentTimeMillis() - stats.lastAfkAt());
        return Text.duration(elapsed) + " lalu";
    }
}
