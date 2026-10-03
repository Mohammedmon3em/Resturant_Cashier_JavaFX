package com.seafoodpos.db;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsDAO {
    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    public String get(String key) { return get(key, null); }

    public String get(String key, String defaultValue) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT value FROM settings WHERE key=?")) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String v = rs.getString("value");
                return v != null ? v : defaultValue;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return defaultValue;
    }

    public void set(String key, String value) {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO settings(key,value) VALUES(?,?) ON CONFLICT(key) DO UPDATE SET value=?")) {
            ps.setString(1, key); ps.setString(2, value); ps.setString(3, value);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public Map<String, String> getAll() {
        Map<String, String> map = new LinkedHashMap<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT key,value FROM settings")) {
            while (rs.next()) map.put(rs.getString("key"), rs.getString("value"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }
}
