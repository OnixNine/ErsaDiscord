package ersa.dev;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import dev.ersa.api.check.ICheck;
import dev.ersa.api.events.impl.ErsaCommandExecuteEvent;
import dev.ersa.api.user.ErsaUser;
import dev.onix.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.awt.*;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErsaCommandListener {
    private static volatile WebhookClient client;
    private final Main plugin;
    private volatile int embedColor;
    private volatile String staticContent = "";
    private volatile String embedTitle = "";
    private volatile boolean enabled;
    public static final Pattern WEBHOOK_PATTERN = Pattern.compile("(?:https?://)?(?:\\w+\\.)?\\w+\\.\\w+/api(?:/v\\d+)?/webhooks/(\\d+)/([\\w-]+)(?:/(?:\\w+)?)?");
    public ErsaCommandListener(Main plugin) {
        this.plugin = plugin;
        reload();
    }

    public synchronized boolean reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        shutdown();
        enabled = false;
        try {
            if (!config.getBoolean("enabled", false)) {
                plugin.getLogger().info("Discord alerts disabled in config.");
                return false;
            }
            String webhook = config.getString("webhook", "");
            if (webhook.isEmpty()) {
                plugin.getLogger().severe("Discord webhook is empty, disabling Discord alerts");
                client = null;
                return false;
            }
            Matcher matcher = WEBHOOK_PATTERN.matcher(webhook);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Failed to parse webhook URL");
            }
            client = WebhookClient.withId(Long.parseUnsignedLong(matcher.group(1)), matcher.group(2));
            client.setTimeout(15000);

            embedTitle = config.getString("embed-title", "**Ersa Alert**");

            try {
                embedColor = Color.decode(config.getString("embed-color", "#00FFFF")).getRGB();
            } catch (NumberFormatException e) {
                plugin.getLogger().severe("Discord embed color is invalid");
            }
            StringBuilder sb = new StringBuilder();
            for (String string : config.getStringList("violation-content")) {
                sb.append(string).append("\n");
            }
            staticContent = sb.toString();
            enabled = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isEnabled() {
        return enabled && client != null;
    }
    @EventHandler
    public void onCommand(ErsaCommandExecuteEvent event) {
        String command = event.command();
        if (!command.equalsIgnoreCase("[webhook]")) return;
        if (client == null) return;

        ICheck check = event.check();
        ErsaUser player = event.player();
        final String checkName = check.getName();
        final int violations = (int) check.getViolations();
        final String sensX = String.valueOf(player.getHumanSensitivityX());
        final String sensY = String.valueOf(player.getHumanSensitivityY());
        final String brand = String.valueOf(player.brand());
        final String playerName = String.valueOf(player.getName());
        final String version = String.valueOf(player.version());
        final int ping = player.ping();
        final String uuid = String.valueOf(player.uuid());
        final String verbose = String.valueOf(event.verbose());
        final String template = staticContent;
        final String title = embedTitle;
        final int color = embedColor;

        event.cancel();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String content = template
                        .replace("%check%", checkName)
                        .replace("%violations%", Integer.toString(violations))
                        .replace("%sensX%", sensX)
                        .replace("%sensY%", sensY)
                        .replace("%brand%", brand)
                        .replace("%player%", playerName)
                        .replace("%version%", version)
                        .replace("%ping%", String.valueOf(ping));
                content = content.replace("_", "\\_");

                WebhookEmbedBuilder embed = new WebhookEmbedBuilder()
                        .setImageUrl("https://i.stack.imgur.com/Fzh0w.png")
                        .setThumbnailUrl("https://crafthead.net/helm/" + uuid)
                        .setColor(color)
                        .setTitle(new WebhookEmbed.EmbedTitle(title, null))
                        .setDescription(content)
                        .setTimestamp(Instant.now());

                if (!verbose.isEmpty()) {
                    embed.addField(new WebhookEmbed.EmbedField(true, "Verbose", verbose));
                }
                sendWebhookEmbed(embed);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
            }
        });
    }
    public void sendWebhookEmbed(WebhookEmbedBuilder embed) {
        try {
            client.send(embed.build()).exceptionally(t -> {
                plugin.getLogger().warning("Failed to send Discord webhook: " + t.getMessage());
                return null;
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
        }
    }

    public static void shutdown() {
        try {
            if (client != null) client.close();
        } catch (Exception ignored) {
        } finally {
            client = null;
        }
    }
}
