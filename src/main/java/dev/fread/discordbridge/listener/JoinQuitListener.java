package dev.fread.discordbridge.listener;

import dev.fread.discordbridge.DiscordChatBridge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {

    private final DiscordChatBridge plugin;
    public JoinQuitListener(DiscordChatBridge plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        plugin.getDiscordBot().sendJoinLeaveEmbed(e.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        plugin.getDiscordBot().sendJoinLeaveEmbed(e.getPlayer(), false);
    }
}
