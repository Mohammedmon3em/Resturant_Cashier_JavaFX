package com.seafoodpos.db;

import com.seafoodpos.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class OrderDAO {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    public synchronized String generateOrderNumber(LocalDate date) {
        String key = date.format(DATE_FMT);
        try {
            conn().setAutoCommit(false);
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT INTO daily_order_sequence(date_key,last_seq) VALUES(?,1) " +
                    "ON CONFLICT(date_key) DO UPDATE SET last_seq=last_seq+1")) {
                ps.setString(1, key); ps.executeUpdate();
            }
            int seq;
            try (PreparedStatement ps = conn().prepareStatement(
                    "SELECT last_seq FROM daily_order_sequence WHERE date_key=?")) {
                ps.setString(1, key);
                ResultSet rs = ps.executeQuery();
                seq = rs.getInt(1);
            }
            conn().commit(); conn().setAutoCommit(true);
            return String.format("%03d", seq);
        } catch (SQLException e) {
            try { conn().rollback(); conn().setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            throw new RuntimeException("Failed to generate order number", e);
        }
    }

    public Order save(Order order) {
        try {
            conn().setAutoCommit(false);
            int orderId;
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT INTO orders(order_number,cashier_id,total_amount,status,notes) VALUES(?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, order.getOrderNumber());
                ps.setInt(2, order.getCashierId());
                ps.setDouble(3, order.getTotalAmount());
                ps.setString(4, order.getStatus() != null ? order.getStatus() : "COMPLETED");
                ps.setString(5, order.getNotes());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                orderId = keys.getInt(1);
                order.setId(orderId);
            }
            for (OrderItem item : order.getItems()) saveItem(item, orderId);
            conn().commit(); conn().setAutoCommit(true);
            return order;
        } catch (SQLException e) {
            try { conn().rollback(); conn().setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            throw new RuntimeException("Failed to save order", e);
        }
    }

    private void saveItem(OrderItem item, int orderId) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO order_items(order_id,menu_item_id,item_name,item_type," +
                "weight_kg,price_per_kg,juice_size,quantity,cooking_method,unit_price,line_total) VALUES(?,?,?,?,?,?,?,?,?,?,?)")) {
            ps.setInt(1, orderId); ps.setInt(2, item.getMenuItemId());
            ps.setString(3, item.getItemName()); ps.setString(4, item.getItemType().name());
            if (item.getWeightKg()  != null) ps.setDouble(5, item.getWeightKg());   else ps.setNull(5, Types.REAL);
            if (item.getPricePerKg()!= null) ps.setDouble(6, item.getPricePerKg()); else ps.setNull(6, Types.REAL);
            if (item.getJuiceSize() != null) ps.setString(7, item.getJuiceSize().name()); else ps.setNull(7, Types.VARCHAR);
            ps.setInt(8, item.getQuantity());
            if (item.getCookingMethod() != null) ps.setString(9, item.getCookingMethod()); else ps.setNull(9, Types.VARCHAR);
            if (item.getUnitPrice() != null) ps.setDouble(10, item.getUnitPrice());  else ps.setNull(10, Types.REAL);
            ps.setDouble(11, item.getLineTotal());
            ps.executeUpdate();
            
            // Deduct from inventory (w2) for FISH_MEAL items with weight
            if (item.getItemType() == ItemType.FISH_MEAL && item.getWeightKg() != null) {
                try (PreparedStatement ips = conn().prepareStatement(
                        "UPDATE inventory SET w2_quantity_kg = w2_quantity_kg - ? WHERE item_name = ?")) {
                    ips.setDouble(1, item.getWeightKg() * item.getQuantity());
                    ips.setString(2, item.getItemName());
                    ips.executeUpdate();
                }
            }
        }
    }

    public List<Order> findByDateRange(LocalDate from, LocalDate to) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name as cashier_name FROM orders o " +
                     "JOIN users u ON o.cashier_id=u.id " +
                     "WHERE date(o.created_at) BETWEEN ? AND ? ORDER BY o.created_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, from.format(DATE_FMT)); ps.setString(2, to.format(DATE_FMT));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order o = mapOrder(rs);
                o.setItems(findItems(o.getId()));
                orders.add(o);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    public List<OrderItem> findItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM order_items WHERE order_id=?")) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) items.add(mapItem(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return items;
    }

    public Map<String, Object> getReportSummary(LocalDate from, LocalDate to) {
        Map<String, Object> result = new LinkedHashMap<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COUNT(*) as cnt, COALESCE(SUM(total_amount),0) as rev " +
                "FROM orders WHERE date(created_at) BETWEEN ? AND ?")) {
            ps.setString(1, from.format(DATE_FMT)); ps.setString(2, to.format(DATE_FMT));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result.put("orderCount",    rs.getInt("cnt"));
                result.put("totalRevenue",  rs.getDouble("rev"));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        double totalExpenses = 0.0, totalPurchases = 0.0, totalDepreciation = 0.0;
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COALESCE(SUM(amount), 0) as exp FROM expenses WHERE date(expense_date) BETWEEN ? AND ? AND name NOT LIKE 'Inventory Purchase:%'")) {
            ps.setString(1, from.format(DATE_FMT)); ps.setString(2, to.format(DATE_FMT));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) totalExpenses = rs.getDouble("exp");
        } catch (SQLException e) { e.printStackTrace(); }

        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COALESCE(SUM(total_cost), 0) as pur FROM inventory_purchases WHERE date(purchase_date) BETWEEN ? AND ?")) {
            ps.setString(1, from.format(DATE_FMT)); ps.setString(2, to.format(DATE_FMT));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) totalPurchases = rs.getDouble("pur");
        } catch (SQLException e) { e.printStackTrace(); }

        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COALESCE(SUM(amount), 0) as dep FROM daily_depreciation WHERE date(dep_date) BETWEEN ? AND ?")) {
            ps.setString(1, from.format(DATE_FMT)); ps.setString(2, to.format(DATE_FMT));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) totalDepreciation = rs.getDouble("dep");
        } catch (SQLException e) { e.printStackTrace(); }

        result.put("totalExpenses", totalExpenses);
        result.put("totalPurchases", totalPurchases);
        result.put("totalDepreciation", totalDepreciation);
        double totalRev = (Double) result.getOrDefault("totalRevenue", 0.0);
        result.put("netProfit", totalRev - totalExpenses - totalPurchases - totalDepreciation);

        Map<String, Map<String, Object>> byType = new HashMap<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT oi.item_type, COUNT(*) as cnt, SUM(oi.line_total) as rev " +
                "FROM order_items oi JOIN orders o ON oi.order_id=o.id " +
                "WHERE date(o.created_at) BETWEEN ? AND ? GROUP BY oi.item_type")) {
            ps.setString(1, from.format(DATE_FMT)); ps.setString(2, to.format(DATE_FMT));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("count", rs.getInt("cnt"));
                m.put("revenue", rs.getDouble("rev"));
                byType.put(rs.getString("item_type"), m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        result.put("byType", byType);

        List<Map<String, Object>> topItems = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT oi.item_name, oi.item_type, COUNT(*) as times, SUM(oi.line_total) as rev " +
                "FROM order_items oi JOIN orders o ON oi.order_id=o.id " +
                "WHERE date(o.created_at) BETWEEN ? AND ? " +
                "GROUP BY oi.item_name,oi.item_type ORDER BY rev DESC LIMIT 20")) {
            ps.setString(1, from.format(DATE_FMT)); ps.setString(2, to.format(DATE_FMT));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("itemName",      rs.getString("item_name"));
                m.put("itemType",      rs.getString("item_type"));
                m.put("timesOrdered",  rs.getInt("times"));
                m.put("itemRevenue",   rs.getDouble("rev"));
                topItems.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        result.put("topItems", topItems);
        return result;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id")); o.setOrderNumber(rs.getString("order_number"));
        o.setCashierId(rs.getInt("cashier_id"));
        try { o.setCashierName(rs.getString("cashier_name")); } catch (SQLException ignored) {}
        o.setTotalAmount(rs.getDouble("total_amount")); o.setStatus(rs.getString("status"));
        o.setNotes(rs.getString("notes"));
        String ca = rs.getString("created_at");
        if (ca != null) {
            try { o.setCreatedAt(LocalDateTime.parse(ca.replace(" ", "T"))); } catch (Exception ignored) {}
        }
        return o;
    }

    private OrderItem mapItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setId(rs.getInt("id")); item.setOrderId(rs.getInt("order_id"));
        item.setMenuItemId(rs.getInt("menu_item_id")); item.setItemName(rs.getString("item_name"));
        item.setItemType(ItemType.valueOf(rs.getString("item_type")));
        double wk = rs.getDouble("weight_kg");   if (!rs.wasNull()) item.setWeightKg(wk);
        double pk = rs.getDouble("price_per_kg"); if (!rs.wasNull()) item.setPricePerKg(pk);
        String js = rs.getString("juice_size");   if (js != null) item.setJuiceSize(JuiceSize.fromString(js));
        item.setQuantity(rs.getInt("quantity"));
        String cm = rs.getString("cooking_method"); if (cm != null) item.setCookingMethod(cm);
        double up = rs.getDouble("unit_price");   if (!rs.wasNull()) item.setUnitPrice(up);
        item.setLineTotal(rs.getDouble("line_total"));
        return item;
    }
}
