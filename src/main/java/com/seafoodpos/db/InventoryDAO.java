package com.seafoodpos.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class InventoryDAO {
    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    public List<Map<String, Object>> getInventory() {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM inventory ORDER BY item_name")) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("itemName", rs.getString("item_name"));
                map.put("w1Qty", rs.getDouble("w1_quantity_kg"));
                map.put("w2Qty", rs.getDouble("w2_quantity_kg"));
                list.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Map<String, Object>> getPurchases(java.time.LocalDate from, java.time.LocalDate to) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT p.*, i.item_name FROM inventory_purchases p " +
                     "JOIN inventory i ON p.inventory_id = i.id ";
        if (from != null && to != null) {
            sql += "WHERE date(p.purchase_date) BETWEEN ? AND ? ";
        }
        sql += "ORDER BY p.purchase_date DESC";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            if (from != null && to != null) {
                ps.setString(1, from.toString());
                ps.setString(2, to.toString());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getInt("id"));
                    map.put("itemName", rs.getString("item_name"));
                    map.put("quantity", rs.getDouble("quantity_kg"));
                    map.put("cost", rs.getDouble("total_cost"));
                    map.put("date", rs.getString("purchase_date"));
                    list.add(map);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void addPurchase(String itemName, double qty, double cost) {
        try {
            conn().setAutoCommit(false);
            
            // Ensure inventory item exists
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT OR IGNORE INTO inventory(item_name) VALUES(?)")) {
                ps.setString(1, itemName);
                ps.executeUpdate();
            }
            
            int invId = 0;
            try (PreparedStatement ps = conn().prepareStatement("SELECT id FROM inventory WHERE item_name=?")) {
                ps.setString(1, itemName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) invId = rs.getInt(1);
            }
            
            // Record purchase
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT INTO inventory_purchases(inventory_id, quantity_kg, total_cost) VALUES(?,?,?)")) {
                ps.setInt(1, invId);
                ps.setDouble(2, qty);
                ps.setDouble(3, cost);
                ps.executeUpdate();
            }
            
            // Update W1
            try (PreparedStatement ps = conn().prepareStatement(
                    "UPDATE inventory SET w1_quantity_kg = w1_quantity_kg + ? WHERE id=?")) {
                ps.setDouble(1, qty);
                ps.setInt(2, invId);
                ps.executeUpdate();
            }
            
            // Removed: Add as expense
            
            conn().commit(); conn().setAutoCommit(true);
        } catch (SQLException e) {
            try { conn().rollback(); conn().setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        }
    }

    public void cleanItem(int invId, double w1Used, double w2Produced) {
        try {
            conn().setAutoCommit(false);
            
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT INTO cleaning_entries(inventory_id, w1_quantity_used, w2_quantity_produced) VALUES(?,?,?)")) {
                ps.setInt(1, invId);
                ps.setDouble(2, w1Used);
                ps.setDouble(3, w2Produced);
                ps.executeUpdate();
            }
            
            try (PreparedStatement ps = conn().prepareStatement(
                    "UPDATE inventory SET w1_quantity_kg = MAX(0, w1_quantity_kg - ?), w2_quantity_kg = w2_quantity_kg + ? WHERE id=?")) {
                ps.setDouble(1, w1Used);
                ps.setDouble(2, w2Produced);
                ps.setInt(3, invId);
                ps.executeUpdate();
            }
            
            conn().commit(); conn().setAutoCommit(true);
        } catch (SQLException e) {
            try { conn().rollback(); conn().setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        }
    }

    public double getLatestCostPerKg(String itemName) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT (total_cost / quantity_kg) FROM inventory_purchases p " +
                "JOIN inventory i ON p.inventory_id = i.id " +
                "WHERE i.item_name = ? ORDER BY p.purchase_date DESC LIMIT 1")) {
            ps.setString(1, itemName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }
}
