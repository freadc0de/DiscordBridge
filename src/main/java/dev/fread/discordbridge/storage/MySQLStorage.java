package dev.fread.discordbridge.storage;

import dev.fread.discordbridge.DiscordChatBridge;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MySQLStorage {

    private final DiscordChatBridge plugin;
    private Connection conn;

    private PreparedStatement psSelect;
    private PreparedStatement psInsert;
    private PreparedStatement psUpdate;
    private PreparedStatement psDelete;

    public MySQLStorage(DiscordChatBridge plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        try {
            String host = plugin.getConfig().getString("mysql.host", "localhost");
            int    port = plugin.getConfig().getInt("mysql.port", 3306);
            String db   = plugin.getConfig().getString("mysql.database", "dcb");
            String user = plugin.getConfig().getString("mysql.user", "root");
            String pass = plugin.getConfig().getString("mysql.password", "");

            conn = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + db +
                            "?useSSL=false&characterEncoding=utf8", user, pass);

            conn.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS dcb_links (" +
                            "uuid CHAR(36) PRIMARY KEY, " +
                            "discord_id VARCHAR(32) NOT NULL)");

            psSelect = conn.prepareStatement(
                    "SELECT discord_id FROM dcb_links WHERE uuid=?");
            psInsert = conn.prepareStatement(
                    "INSERT INTO dcb_links(uuid, discord_id) VALUES(?,?)");
            psUpdate = conn.prepareStatement(
                    "UPDATE dcb_links SET discord_id=? WHERE uuid=?");
            psDelete = conn.prepareStatement(
                    "DELETE FROM dcb_links WHERE uuid=?");

            plugin.getLogger().info("MySQL connected");
            return true;

        } catch (SQLException ex) {
            plugin.getLogger().severe("MySQL init error: " + ex.getMessage());
            return false;
        }
    }

    public Map<UUID, String> loadAll() {
        Map<UUID, String> map = new HashMap<>();
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM dcb_links")) {
            while (rs.next()) {
                map.put(UUID.fromString(rs.getString("uuid")), rs.getString("discord_id"));
            }
        } catch (SQLException ex) {
            plugin.getLogger().warning("MySQL loadAll error: " + ex.getMessage());
        }
        return map;
    }

    public void save(UUID uuid, String discordId) {
        try {
            psUpdate.setString(1, discordId);
            psUpdate.setString(2, uuid.toString());
            if (psUpdate.executeUpdate() == 0) {           // no rows updated → insert
                psInsert.setString(1, uuid.toString());
                psInsert.setString(2, discordId);
                psInsert.executeUpdate();
            }
        } catch (SQLException ex) {
            plugin.getLogger().warning("MySQL save error: " + ex.getMessage());
        }
    }

    public void delete(UUID uuid) {
        try {
            psDelete.setString(1, uuid.toString());
            psDelete.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().warning("MySQL delete error: " + ex.getMessage());
        }
    }

    public String get(UUID uuid) {
        try {
            psSelect.setString(1, uuid.toString());
            try (ResultSet rs = psSelect.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException ex) {
            plugin.getLogger().warning("MySQL get error: " + ex.getMessage());
        }
        return null;
    }

    public void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (SQLException ignored) {}
    }
}
