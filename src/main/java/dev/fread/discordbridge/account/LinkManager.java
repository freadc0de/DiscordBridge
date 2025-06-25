package dev.fread.discordbridge.account;

import dev.fread.discordbridge.DiscordChatBridge;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LinkManager {

    private final DiscordChatBridge plugin;
    private final File              file;
    private       YamlConfiguration data;

    private final Map<UUID, String> uuidToDiscord = new HashMap<>();
    private final Map<String, UUID> discordToUuid = new HashMap<>();
    private final Map<UUID, String> pendingCodes  = new HashMap<>();

    public LinkManager(DiscordChatBridge plugin) {
        this.plugin = plugin;
        this.file   = new File(plugin.getDataFolder(), "links.yml");
    }

    /* --------------------------------------------------------------------- */

    public void load() {
        try {
            if (!file.exists()) {
                // создаём пустой файл и сразу записываем «{}»
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException ex) {
            plugin.getLogger().severe("Cannot create links.yml: " + ex.getMessage());
        }

        data = YamlConfiguration.loadConfiguration(file);

        for (String key : data.getKeys(false)) {
            UUID   uuid = UUID.fromString(key);
            String id   = data.getString(key + ".discord");
            if (id != null) {
                uuidToDiscord.put(uuid, id);
                discordToUuid.put(id, uuid);
            }
        }
    }

    public void save() {
        if (data == null) return;                        // безопасно на случай раннего disable
        for (Map.Entry<UUID, String> e : uuidToDiscord.entrySet()) {
            data.set(e.getKey().toString() + ".discord", e.getValue());
        }
        try { data.save(file); }
        catch (IOException ex) {
            plugin.getLogger().severe("Failed to save links.yml: " + ex.getMessage());
        }
    }

    /* --------------------------------------------------------------------- */

    public boolean isLinked(UUID uuid)          { return uuidToDiscord.containsKey(uuid); }
    public String  getDiscordId(UUID uuid)      { return uuidToDiscord.get(uuid); }

    public String createCode(UUID uuid) {
        String code = String.format("%04d", new Random().nextInt(10000));
        pendingCodes.put(uuid, code);
        return code;
    }

    public Optional<UUID> consumeCode(String code) {
        return pendingCodes.entrySet().stream()
                .filter(e -> e.getValue().equals(code))
                .map(Map.Entry::getKey)
                .findFirst()
                .map(uuid -> { pendingCodes.remove(uuid); return uuid; });
    }

    public void link(UUID uuid, String discordId) {
        uuidToDiscord.put(uuid, discordId);
        discordToUuid.put(discordId, uuid);
        save();
    }
}
