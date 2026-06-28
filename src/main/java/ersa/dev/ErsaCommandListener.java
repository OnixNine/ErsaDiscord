package ersa.dev;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import dev.ersa.api.check.ICheck;
import dev.ersa.api.events.impl.ErsaCommandExecuteEvent;
import dev.ersa.api.user.IErsaPlayer;
import dev.onix.EventHandler;
import org.bukkit.configuration.file.FileConfiguration;

import java.awt.*;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErsaCommandListener {
    private static WebhookClient client;
    private int embedColor;
    private String staticContent = "";
    private String embedTitle = "";
    public static final Pattern WEBHOOK_PATTERN = Pattern.compile("(?:https?://)?(?:\\w+\\.)?\\w+\\.\\w+/api(?:/v\\d+)?/webhooks/(\\d+)/([\\w-]+)(?:/(?:\\w+)?)?");
    public ErsaCommandListener(Main plugin) {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        try {
            if (!config.getBoolean("enabled", false)) return;
            String webhook = config.getString("webhook", "");
            if (webhook.isEmpty()) {
                plugin.getLogger().severe("Discord webhook is empty, disabling Discord alerts");
                client = null;
                return;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @EventHandler
    public void onCommand(ErsaCommandExecuteEvent event) {
        String command = event.command();
        if (command.equalsIgnoreCase("[webhook]")) {
            ICheck check = event.check();
            IErsaPlayer player = event.player();
            String verbose = event.verbose();
            if (client != null) {
                String content = staticContent;
                content = content.replace("%check%", check.getName());
                content = content.replace("%violations%", Integer.toString((int) check.getViolations()));
                content = content.replaceAll("%brand%",player.brand()).replaceAll("%player%", player.name()).replaceAll("%sens%", String.valueOf(player.sensitivity())).replaceAll("%version%",player.version()).replaceAll("%ping%", String.valueOf(player.ping()));
                content = content.replace("_", "\\_");

                WebhookEmbedBuilder embed = new WebhookEmbedBuilder()
                        .setImageUrl("https://i.stack.imgur.com/Fzh0w.png")
                        .setThumbnailUrl("https://crafthead.net/helm/" + player.uuid())
                        .setColor(embedColor)
                        .setTitle(new WebhookEmbed.EmbedTitle(embedTitle, null))
                        .setDescription(content)
                        .setTimestamp(Instant.now());

                if (!verbose.isEmpty()) {
                    embed.addField(new WebhookEmbed.EmbedField(true, "Verbose", verbose));
                }

                sendWebhookEmbed(embed);
            }
        }

    }
    public void sendWebhookEmbed(WebhookEmbedBuilder embed) {
        try {
            client.send(embed.build());
        } catch (Exception ignored) {
        }
    }
}
