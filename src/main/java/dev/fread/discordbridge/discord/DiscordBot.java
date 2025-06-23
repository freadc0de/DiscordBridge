package dev.fread.discordbridge.discord;

import dev.fread.discordbridge.DiscordChatBridge;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.regex.Pattern;

public class DiscordBot {

    private final JDA    jda;
    private final String channelId;
    private final DiscordChatBridge plugin;
    private       MessageChannel    channel;

    private static final Pattern COLOR_CODE = Pattern.compile("§[0-9A-FK-ORXa-fk-orx]");

    public DiscordBot(DiscordChatBridge plugin,
                      String token,
                      String channelId)
            throws LoginException, InterruptedException {

        this.plugin    = plugin;
        this.channelId = channelId;

        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT
        );

        jda = JDABuilder.createDefault(token, intents)
                .build()
                .awaitReady();

        channel = jda.getTextChannelById(channelId);
        if (channel == null)
            plugin.getLogger().warning("Канал " + channelId + " не найден!");

        jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent e) {
                if (e.getAuthor().isBot() || !e.getChannel().getId().equals(channelId)) return;

                String content = e.getMessage().getContentDisplay();
                String formatted = plugin.getConfigManager()
                        .formatDiscordToMinecraft(
                                e.getAuthor().getName(), content);

                Bukkit.getScheduler().runTask(plugin,
                        () -> Bukkit.broadcastMessage(formatted));
            }
        });
    }

    public void sendMinecraftEmbed(Player player, String plainMessage) {
        if (channel == null) return;
        EmbedBuilder eb = baseEmbed(player)
                .setDescription(plainMessage)
                .setColor(Color.decode("#adfbff"));

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    public void sendJoinLeaveEmbed(Player player, boolean joined) {
        if (channel == null) return;

        Color color = joined ? Color.decode("#57F287")
                : Color.decode("#ED4245");
        String action = joined ? "зашёл на сервер" : "вышел с сервера";

        EmbedBuilder eb = baseEmbed(player)
                .setDescription("**" + player.getName() + "** " + action + ".")
                .setColor(color);

        channel.sendMessageEmbeds(eb.build()).queue();
    }


    private EmbedBuilder baseEmbed(Player player) {
        String avatarUrl = "https://mc-heads.net/avatar/" + player.getName() + "/64";
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        return new EmbedBuilder()
                .setAuthor(player.getName(), null, avatarUrl)
                .setFooter("Stats • " + time);
    }

    public void sendToDiscordPlain(String msg) {
        if (channel != null)
            channel.sendMessage(COLOR_CODE.matcher(msg).replaceAll("")).queue();
    }

    public void shutdown() { jda.shutdownNow(); }
}
