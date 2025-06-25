package dev.fread.discordbridge.discord;

import dev.fread.discordbridge.DiscordChatBridge;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
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
    private final String            requiredRoleId;
    private       TextChannel       channel;

    private static final Pattern COLOR_CODE = Pattern.compile("§[0-9A-FK-ORXa-fk-orx]");

    public DiscordBot(DiscordChatBridge plugin, String token, String channelId)
            throws LoginException, InterruptedException {
        this.plugin    = plugin;
        this.channelId = channelId;
        this.requiredRoleId = plugin.getConfig().getString("discord.requiredRoleId", "");

        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MEMBERS);

        jda = JDABuilder.createDefault(token, intents)
                .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI,
                        CacheFlag.STICKER,     CacheFlag.SCHEDULED_EVENTS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build()
                .awaitReady();

        channel = jda.getTextChannelById(channelId);
        if (channel == null)
            plugin.getLogger().warning("Discord channel " + channelId + " not found!");

        jda.addEventListener(new ListenerAdapter() {
            @Override public void onMessageReceived(@NotNull MessageReceivedEvent e) {
                if (e.getAuthor().isBot() || !e.getChannel().getId().equals(channelId)) return;
                relayToMinecraft(e);
            }
        });

        jda.addEventListener(new ListenerAdapter() {
            @Override public void onMessageReceived(@NotNull MessageReceivedEvent e) {
                if (e.isFromGuild() || e.getAuthor().isBot()) return;
                handleDmCode(e);
            }
        });
    }

    public boolean hasRequiredRole(java.util.UUID uuid) {
        if (requiredRoleId.isEmpty()) return true;
        String discId = plugin.getLinkManager().getDiscordId(uuid);
        if (discId == null || channel == null) return false;
        Member m = channel.getGuild().getMemberById(discId);
        return m != null && m.getRoles().stream().anyMatch(r -> r.getId().equals(requiredRoleId));
    }

    private void handleDmCode(MessageReceivedEvent e) {
        String code = e.getMessage().getContentDisplay().trim();
        plugin.getLinkManager().consumeCode(code).ifPresentOrElse(uuid -> {
            plugin.getLinkManager().link(uuid, e.getAuthor().getId());
            sendDmEmbed(e.getChannel(), new Color(0x57F287),
                    "✅ Linked! You may now join the server.");
        }, () -> sendDmEmbed(e.getChannel(), new Color(0xED4245),
                "❌ Invalid code or already linked."));
    }

    private void sendDmEmbed(MessageChannel dm, Color color, String text) {
        dm.sendMessageEmbeds(List.of(new EmbedBuilder()
                .setColor(color)
                .setDescription(text)
                .build())).queue();
    }

    private void relayToMinecraft(MessageReceivedEvent e) {
        String author  = e.getAuthor().getName();
        String content = e.getMessage().getContentDisplay();

        String legacy  = plugin.getConfigManager().formatDiscordToMinecraft(author, content);
        Component line = LegacyComponentSerializer.legacySection().deserialize(legacy);

        List<Component> hoverParts = plugin.getConfigManager()
                .buildDiscordHover(author, content).stream()
                .map(s -> LegacyComponentSerializer.legacySection().deserialize(s))
                .collect(java.util.stream.Collectors.toList());

        Component hover = Component.join(JoinConfiguration.separator(Component.newline()), hoverParts);

        line = line.hoverEvent(HoverEvent.showText(hover))
                .clickEvent(ClickEvent.openUrl(e.getMessage().getJumpUrl()));

        for (Player p : Bukkit.getOnlinePlayers()) p.sendMessage(line);
    }

    public String getBotName() { return jda.getSelfUser().getName() + "#" + jda.getSelfUser().getDiscriminator(); }

    public void sendMinecraftEmbed(Player player, String plain) {
        if (channel == null) return;
        channel.sendMessageEmbeds(List.of(new EmbedBuilder()
                .setAuthor(player.getName(), null, "https://mc-heads.net/avatar/"+player.getName()+"/64")
                .setDescription(plain)
                .setColor(Color.decode("#adfbff"))
                .setFooter("Stats • " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
                .build())).queue();
    }

    public void sendJoinLeaveEmbed(Player p, boolean join) {
        if (channel == null) return;
        Color c = join ? new Color(0x57F287) : new Color(0xED4245);
        channel.sendMessageEmbeds(List.of(new EmbedBuilder()
                .setAuthor(p.getName(), null, "https://mc-heads.net/avatar/"+p.getName()+"/64")
                .setDescription("**"+p.getName()+"** "+(join?"joined.":"left."))
                .setColor(c)
                .setFooter("Stats • " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
                .build())).queue();
    }

    public void sendToDiscordPlain(String msg) {
        if (channel != null)
            channel.sendMessage(COLOR_CODE.matcher(msg).replaceAll("")).queue();
    }

    public void shutdown() { jda.shutdownNow(); }
}
