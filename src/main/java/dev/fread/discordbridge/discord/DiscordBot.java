package dev.fread.discordbridge.discord;


import dev.fread.discordbridge.DiscordChatBridge;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

public class DiscordBot {

    private final DiscordChatBridge plugin;
    private final JDA               jda;
    private final String            channelId;
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
                .disableCache(CacheFlag.VOICE_STATE,
                        CacheFlag.EMOJI,
                        CacheFlag.STICKER,
                        CacheFlag.SCHEDULED_EVENTS)
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .build()
                .awaitReady();

        channel = jda.getTextChannelById(channelId);
        if (channel == null)
            plugin.getLogger().warning("Канал Discord " + channelId + " не найден!");

        jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent e) {
                if (e.getAuthor().isBot() || !e.getChannel().getId().equals(channelId)) return;

                String author  = e.getAuthor().getName();
                String content = e.getMessage().getContentDisplay();

                String legacyLine = plugin.getConfigManager()
                        .formatDiscordToMinecraft(author, content);
                Component line = LegacyComponentSerializer.legacySection().deserialize(legacyLine);

                List<String> hoverLines = plugin.getConfigManager().buildDiscordHover(author, content);
                Component hover = Component.empty();
                for (int i = 0; i < hoverLines.size(); i++) {
                    hover = hover.append(
                            LegacyComponentSerializer.legacySection().deserialize(hoverLines.get(i)));
                    if (i < hoverLines.size() - 1)
                        hover = hover.append(Component.newline());
                }

                line = line.hoverEvent(HoverEvent.showText(hover))
                        .clickEvent(ClickEvent.openUrl(e.getMessage().getJumpUrl()));

                for (Player p : Bukkit.getOnlinePlayers())
                    p.sendMessage(line);
            }
        });
    }


    public void sendMinecraftEmbed(Player player, String plainMessage) {
        if (channel == null) return;

        String avatar = "https://mc-heads.net/avatar/" + player.getName() + "/64";
        String time   = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor(player.getName(), null, avatar)
                .setDescription(plainMessage)
                .setColor(Color.decode("#adfbff"))
                .setFooter("Stats • " + time);

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    public void sendJoinLeaveEmbed(Player player, boolean joined) {
        if (channel == null) return;

        Color  color  = joined ? Color.decode("#57F287") : Color.decode("#ED4245");
        String action = joined ? "зашёл на сервер."      : "вышел с сервера.";

        String avatar = "https://mc-heads.net/avatar/" + player.getName() + "/64";
        String time   = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor(player.getName(), null, avatar)
                .setDescription("**" + player.getName() + "** " + action)
                .setColor(color)
                .setFooter("Stats • " + time);

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    public void sendToDiscordPlain(String msg) {
        if (channel != null)
            channel.sendMessage(COLOR_CODE.matcher(msg).replaceAll("")).queue();
    }

    public void shutdown() { jda.shutdownNow(); }
}
