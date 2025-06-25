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

/**
 * Handles Discord ↔ Minecraft messaging and DM-based account linking.
 */
public class DiscordBot {

    private final DiscordChatBridge plugin;
    private final JDA               jda;
    private final String            channelId;
    private       MessageChannel    channel;

    private static final Pattern COLOR_CODE = Pattern.compile("§[0-9A-FK-ORXa-fk-orx]");

    public DiscordBot(DiscordChatBridge plugin, String token, String channelId)
            throws LoginException, InterruptedException {
        this.plugin    = plugin;
        this.channelId = channelId;

        /* include DIRECT_MESSAGES so we can read private codes */
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT);

        jda = JDABuilder.createDefault(token, intents)
                .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI,
                        CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .build()
                .awaitReady();

        channel = jda.getTextChannelById(channelId);
        if (channel == null)
            plugin.getLogger().warning("Discord channel " + channelId + " not found!");

        /* ---------------- Discord (guild) → Minecraft ---------------- */
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
                    hover = hover.append(LegacyComponentSerializer.legacySection()
                            .deserialize(hoverLines.get(i)));
                    if (i < hoverLines.size() - 1) hover = hover.append(Component.newline());
                }

                line = line.hoverEvent(HoverEvent.showText(hover))
                        .clickEvent(ClickEvent.openUrl(e.getMessage().getJumpUrl()));

                for (Player p : Bukkit.getOnlinePlayers()) p.sendMessage(line);
            }
        });

        /* ---------------- Discord DM → account link ------------------ */
        jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent e) {
                if (e.isFromGuild() || e.getAuthor().isBot()) return;

                String msg = e.getMessage().getContentDisplay().trim();

                plugin.getLinkManager().consumeCode(msg).ifPresentOrElse(uuid -> {
                    // SUCCESS
                    plugin.getLinkManager().link(uuid, e.getAuthor().getId());
                    sendDMEmbed(e.getChannel(), Color.decode("#57F287"),
                            "✅ Ваш аккаунт успешно слинкован!");
                    plugin.getLogger().info("Linked " + uuid + " <-> " + e.getAuthor().getAsTag());
                }, () -> {
                    // either already linked or invalid code
                    sendDMEmbed(e.getChannel(), Color.decode("#ED4245"),
                            "❌ Этот код недействителен либо вы уже линковали ваш аккаунт.");
                });
            }
        });
    }

    /* ------------------------------------------------------------------ */
    /*                          public helpers                             */
    /* ------------------------------------------------------------------ */
    public String getBotName() {
        return jda.getSelfUser().getName() + "#" + jda.getSelfUser().getDiscriminator();
    }

    /* Minecraft → Discord chat embed */
    public void sendMinecraftEmbed(Player player, String plainMessage) {
        if (channel == null) return;
        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor(player.getName(), null,
                        "https://mc-heads.net/avatar/" + player.getName() + "/64")
                .setDescription(plainMessage)
                .setColor(Color.decode("#adfbff"))
                .setFooter("Stats • " +
                        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        channel.sendMessageEmbeds(eb.build()).queue();
    }

    /* join / quit */
    public void sendJoinLeaveEmbed(Player player, boolean joined) {
        if (channel == null) return;
        Color  c = joined ? Color.decode("#57F287") : Color.decode("#ED4245");
        String text = "**" + player.getName() + "** " + (joined ? "зашел на сервер." : "вышел с сервера.");
        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor(player.getName(), null,
                        "https://mc-heads.net/avatar/" + player.getName() + "/64")
                .setDescription(text)
                .setColor(c)
                .setFooter("Stats • " +
                        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        channel.sendMessageEmbeds(eb.build()).queue();
    }

    public void sendToDiscordPlain(String msg) {
        if (channel != null)
            channel.sendMessage(COLOR_CODE.matcher(msg).replaceAll("")).queue();
    }

    public void shutdown() { jda.shutdownNow(); }

    /* ------------------------------------------------------------------ */
    /*                         private helpers                             */
    /* ------------------------------------------------------------------ */
    private void sendDMEmbed(MessageChannel dm, Color color, String text) {
        EmbedBuilder eb = new EmbedBuilder()
                .setDescription(text)
                .setColor(color);
        dm.sendMessageEmbeds(eb.build()).queue();
    }
}
