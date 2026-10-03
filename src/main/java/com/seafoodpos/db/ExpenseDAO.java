package com.seafoodpos.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseDAO {
    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    public List<Map<String, Object>> getExpenses() {
        return getExpenses(null, null);
    }

    public List<Map<String, Object>> getExpenses(java.time.LocalDate from, java.time.LocalDate to) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM expenses ";
        if (from != null && to != null) {
            sql += "WHERE date(expense_date) BETWEEN ? AND ? ";
        }
        sql += "ORDER BY expense_date DESC";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            if (from != null && to != null) {
                ps.setString(1, from.toString());
                ps.setString(2, to.toString());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getInt("id"));
                    map.put("name", rs.getString("name"));
                    map.put("amount", rs.getDouble("amount"));
                    map.put("date", rs.getString("expense_date"));
                    list.add(map);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void addExpense(String name, double amount) {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO expenses(name, amount) VALUES(?,?)")) {
            ps.setString(1, name);
            ps.setDouble(2, amount);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void addDepreciation(double amount) {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO daily_depreciation(amount) VALUES(?)")) {
            ps.setDouble(1, amount);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public double getDepreciation(java.time.LocalDate from, java.time.LocalDate to) {
        double total = 0;
        String sql = "SELECT SUM(amount) FROM daily_depreciation WHERE date(dep_date) BETWEEN ? AND ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, from.toString());
            ps.setString(2, to.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) total = rs.getDouble(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return total;
    }
}
