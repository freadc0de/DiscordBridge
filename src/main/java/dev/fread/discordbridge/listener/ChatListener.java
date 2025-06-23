package dev.fread.discordbridge.listener;

import dev.fread.discordbridge.DiscordChatBridge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final DiscordChatBridge plugin;
    public ChatListener(DiscordChatBridge plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        plugin.getDiscordBot()
                .sendMinecraftEmbed(e.getPlayer(), e.getMessage());
    }
}
