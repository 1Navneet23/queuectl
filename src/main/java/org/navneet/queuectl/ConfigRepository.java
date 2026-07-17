package org.navneet.queuectl;

import org.navneet.queuectl.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigRepository {

    public void set(String key, String value) {
        String sql = """
                INSERT INTO config(key, value)
                VALUES(?, ?)
                ON CONFLICT(key)
                DO UPDATE SET value = excluded.value
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Unable to save config", e);
        }
    }

    public String get(String key, String defaultValue) {

        String sql = "SELECT value FROM config WHERE key = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, key);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getString("value");
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Unable to read config", e);
        }

        return defaultValue;
    }

}