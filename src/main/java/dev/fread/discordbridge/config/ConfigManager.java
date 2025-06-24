package dev.fread.discordbridge.config;

import dev.fread.discordbridge.DiscordChatBridge;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigManager {

    private static final Pattern HEX_PATTERN = Pattern.compile("&([A-Fa-f0-9]{6})");

    private final DiscordChatBridge plugin;

    private String       toDiscordPrefix;
    private String       toMinecraftPrefix;
    private List<String> mcToDiscordFormat;
    private List<String> discordToMinecraftFormat;
    private List<String> discordHoverLines;

    public ConfigManager(DiscordChatBridge plugin) { this.plugin = plugin; }

    public void load() { reload(); }

    public void reload() {
        plugin.reloadConfig();
        ConfigurationSection c = plugin.getConfig();

        toDiscordPrefix          = c.getString("chat.to-discord-prefix",   "&00FF00[MC]&r ");
        toMinecraftPrefix        = c.getString("chat.to-minecraft-prefix", "&00FF00[DISCORD]&r ");

        mcToDiscordFormat        = c.getStringList("messages.minecraft-to-discord");
        discordToMinecraftFormat = c.getStringList("messages.discord-to-minecraft");
        discordHoverLines        = c.getStringList("messages.discord-hover");
    }


    public String formatMcToDiscord(Player p, String msg) {
        String base = mcToDiscordFormat.get(0)
                .replace("{player}",  p.getName())
                .replace("{message}", msg);
        return stripColors(applyColors(base));
    }

    public String formatDiscordToMinecraft(String author, String msg) {
        String base = discordToMinecraftFormat.get(0)
                .replace("{author}",  author)
                .replace("{message}", msg);
        return applyColors(toMinecraftPrefix + base);
    }

    public List<String> buildDiscordHover(String author, String msg) {
        return discordHoverLines.stream()
                .map(s -> applyColors(
                        s.replace("{author}", author)
                                .replace("{message}", msg)))
                .collect(Collectors.toList());
    }


    private String applyColors(String s) {
        Matcher m = HEX_PATTERN.matcher(s);
        StringBuffer buf = new StringBuffer();
        while (m.find())
            m.appendReplacement(buf,
                    ChatColor.of("#" + m.group(1)).toString());
        m.appendTail(buf);
        return ChatColor.translateAlternateColorCodes('&', buf.toString());
    }

    private String stripColors(String s) {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', s));
    }

    public String getToDiscordPrefix()   { return stripColors(applyColors(toDiscordPrefix)); }
    public String getToMinecraftPrefix() { return applyColors(toMinecraftPrefix);            }
}
