package dev.fread.discordbridge.account;

import dev.fread.discordbridge.DiscordChatBridge;
import dev.fread.discordbridge.storage.MySQLStorage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LinkManager {

    private final Map<UUID, String> uuidToDiscord = new HashMap<>();
    private final Map<String, UUID> discordToUuid = new HashMap<>();
    private final Map<UUID, String> pendingCodes  = new HashMap<>();

    private final DiscordChatBridge plugin;

    private final File              file;
    private       YamlConfiguration yaml;

    private       boolean       mysqlEnabled;
    private       MySQLStorage  sql;

    public LinkManager(DiscordChatBridge plugin) {
        this.plugin = plugin;
        this.file   = new File(plugin.getDataFolder(), "links.yml");
    }

    public void load() {
        mysqlEnabled = plugin.getConfig().getBoolean("mysql.enabled", false);
        if (mysqlEnabled) {
            sql = new MySQLStorage(plugin);
            if (sql.init()) {
                uuidToDiscord.putAll(sql.loadAll());
                uuidToDiscord.forEach((u, d) -> discordToUuid.put(d, u));
                plugin.getLogger().info("Loaded " + uuidToDiscord.size() + " linked accounts from MySQL");
                return;
            } else {
                plugin.getLogger().warning("Falling back to local links.yml storage");
                mysqlEnabled = false;
            }
        }
        try { if (!file.exists()) { file.getParentFile().mkdirs(); file.createNewFile(); } }
        catch (IOException ex) { plugin.getLogger().severe("Cannot create links.yml: "+ex.getMessage()); }
        yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getKeys(false)) {
            UUID  uuid = UUID.fromString(key);
            String id  = yaml.getString(key + ".discord");
            if (id != null) {
                uuidToDiscord.put(uuid, id);
                discordToUuid.put(id, uuid);
            }
        }
        plugin.getLogger().info("Loaded " + uuidToDiscord.size() + " linked accounts from links.yml");
    }

    public void save() {
        if (mysqlEnabled) return;
        if (yaml == null) return;
        uuidToDiscord.forEach((u, d) -> yaml.set(u.toString() + ".discord", d));
        try { yaml.save(file); } catch (IOException ex) {
            plugin.getLogger().severe("Failed to save links.yml: " + ex.getMessage());
        }
    }

    public void close() {
        if (sql != null) sql.close();
    }

    public boolean isLinked(UUID uuid) { return uuidToDiscord.containsKey(uuid); }
    public String  getDiscordId(UUID uuid) { return uuidToDiscord.get(uuid); }

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
                .map(u -> { pendingCodes.remove(u); return u; });
    }

    public void link(UUID uuid, String discordId) {
        uuidToDiscord.put(uuid, discordId);
        discordToUuid.put(discordId, uuid);
        if (mysqlEnabled && sql != null) sql.save(uuid, discordId);
        else save();
    }

    public void unlink(UUID uuid) {
        String dId = uuidToDiscord.remove(uuid);
        if (dId != null) discordToUuid.remove(dId);
        if (mysqlEnabled && sql != null) sql.delete(uuid);
        else save();
    }
}
