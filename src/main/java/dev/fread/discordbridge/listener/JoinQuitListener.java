package dev.fread.discordbridge.listener;

import dev.fread.discordbridge.DiscordChatBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoinQuitListener implements Listener {

    private final DiscordChatBridge plugin;
    private static final Pattern HEX = Pattern.compile("&([A-Fa-f0-9]{6})");

    public JoinQuitListener(DiscordChatBridge plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent e) {

        if (!plugin.getLinkManager().isLinked(e.getPlayer().getUniqueId())) {
            String code = plugin.getLinkManager().createCode(e.getPlayer().getUniqueId());
            Component kick = buildKickMessage(plugin.getConfig().getStringList("link.kick-message"), code);
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, kick);
            return;
        }

        if (!plugin.getDiscordBot().hasRequiredRole(e.getPlayer().getUniqueId())) {
            List<String> roleLines = plugin.getConfig().getStringList("link.role-kick");
            Component kick = buildKickMessage(roleLines, ""); // no {code} needed
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, kick);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        e.joinMessage(null);
        if (plugin.getLinkManager().isLinked(e.getPlayer().getUniqueId()) &&
                plugin.getDiscordBot().hasRequiredRole(e.getPlayer().getUniqueId())) {
            plugin.getDiscordBot().sendJoinLeaveEmbed(e.getPlayer(), true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        e.quitMessage(null);
        if (plugin.getLinkManager().isLinked(e.getPlayer().getUniqueId())) {
            plugin.getDiscordBot().sendJoinLeaveEmbed(e.getPlayer(), false);
        }
    }

    private Component buildKickMessage(List<String> lines, String code) {
        for (int i = 0; i < lines.size(); i++) {
            lines.set(i, lines.get(i)
                    .replace("{code}", code)
                    .replace("{bot}", plugin.getDiscordBot().getBotName()));
        }
        String legacy = colourise(String.join("\n", lines));
        return LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    private String colourise(String s) {
        Matcher m = HEX.matcher(s);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            StringBuilder rep = new StringBuilder("§x");
            for (char c : m.group(1).toCharArray()) rep.append('§').append(c);
            m.appendReplacement(out, rep.toString());
        }
        m.appendTail(out);
        return ChatColor.translateAlternateColorCodes('&', out.toString());
    }
}
