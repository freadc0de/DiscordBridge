package dev.fread.discordbridge.listener;

import dev.fread.discordbridge.DiscordChatBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoinQuitListener implements Listener {

    private final DiscordChatBridge plugin;
    private static final Pattern HEX_PATTERN = Pattern.compile("&([A-Fa-f0-9]{6})");

    public JoinQuitListener(DiscordChatBridge plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        e.joinMessage(null);

        if (!plugin.getLinkManager().isLinked(e.getPlayer().getUniqueId())) {
            String code = plugin.getLinkManager().createCode(e.getPlayer().getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> kickWithColours(e, code));
            return;
        }

        plugin.getDiscordBot().sendJoinLeaveEmbed(e.getPlayer(), true);
    }

    private void kickWithColours(PlayerJoinEvent e, String code) {
        List<String> lines = plugin.getConfig().getStringList("link.kick-message");
        for (int i = 0; i < lines.size(); i++) {
            lines.set(i, lines.get(i)
                    .replace("{code}", code)
                    .replace("{bot}", plugin.getDiscordBot().getBotName()));
        }
        String legacy = colourise(String.join("\n", lines));
        Component reason = LegacyComponentSerializer.legacySection().deserialize(legacy);
        e.getPlayer().kick(reason);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        e.quitMessage(null);
        if (plugin.getLinkManager().isLinked(e.getPlayer().getUniqueId()))
            plugin.getDiscordBot().sendJoinLeaveEmbed(e.getPlayer(), false);
    }

    private String colourise(String input) {
        Matcher m = HEX_PATTERN.matcher(input);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String hex = m.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) replacement.append('§').append(c);
            m.appendReplacement(out, replacement.toString());
        }
        m.appendTail(out);
        return ChatColor.translateAlternateColorCodes('&', out.toString());
    }
}
