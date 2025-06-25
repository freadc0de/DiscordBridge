package dev.fread.discordbridge;

import dev.fread.discordbridge.account.LinkManager;
import dev.fread.discordbridge.command.DChatCommand;
import dev.fread.discordbridge.config.ConfigManager;
import dev.fread.discordbridge.discord.DiscordBot;
import dev.fread.discordbridge.listener.ChatListener;
import dev.fread.discordbridge.listener.JoinQuitListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class DiscordChatBridge extends JavaPlugin {

    private ConfigManager configManager;
    private DiscordBot    discordBot;
    private LinkManager   linkManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        configManager.load();

        linkManager = new LinkManager(this);
        linkManager.load();

        try {
            discordBot = new DiscordBot(this,
                    getConfig().getString("discord.token"),
                    getConfig().getString("discord.channelId"));
            getLogger().info("Discord bot started.");
        } catch (Exception ex) {
            getLogger().severe("Failed to init Discord bot: " + ex.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        /* listeners */
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);

        /* command */
        getCommand("dchat").setExecutor(new DChatCommand(this));
        getCommand("dchat").setTabCompleter((s, c, l, a) -> java.util.Collections.singletonList("reload"));

        getLogger().info(ChatColor.GREEN + "DiscordChatBridge enabled!");
    }

    @Override
    public void onDisable() {
        if (discordBot != null) discordBot.shutdown();
        linkManager.save();
        getLogger().info("DiscordChatBridge disabled.");
    }

    public ConfigManager getConfigManager() { return configManager; }
    public DiscordBot    getDiscordBot()    { return discordBot;    }
    public LinkManager   getLinkManager()   { return linkManager;   }
}
