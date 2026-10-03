package com.seafoodpos.db;

import com.seafoodpos.model.ItemType;
import com.seafoodpos.model.MenuItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDAO {
    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    public List<MenuItem> findAll() {
        return query("SELECT * FROM menu_items ORDER BY item_type, display_order, name");
    }

    public List<MenuItem> findAllAvailable() {
        return query("SELECT * FROM menu_items WHERE is_available=1 ORDER BY item_type, display_order, name");
    }

    public MenuItem findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM menu_items WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean insert(MenuItem item) {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO menu_items(name,item_type,price_per_kg,price_small,price_large," +
                "category,is_available,display_order) VALUES(?,?,?,?,?,?,?,?)")) {
            setParams(ps, item); ps.executeUpdate(); return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(MenuItem item) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE menu_items SET name=?,item_type=?,price_per_kg=?,price_small=?," +
                "price_large=?,category=?,is_available=?,display_order=? WHERE id=?")) {
            setParams(ps, item); ps.setInt(9, item.getId()); ps.executeUpdate(); return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean setAvailable(int id, boolean available) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE menu_items SET is_available=? WHERE id=?")) {
            ps.setInt(1, available ? 1 : 0); ps.setInt(2, id);
            ps.executeUpdate(); return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM menu_items WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate(); return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private void setParams(PreparedStatement ps, MenuItem item) throws SQLException {
        ps.setString(1, item.getName());
        ps.setString(2, item.getItemType().name());
        if (item.getPricePerKg() != null) ps.setDouble(3, item.getPricePerKg());
        else ps.setNull(3, Types.REAL);
        if (item.getPriceSmall() != null) ps.setDouble(4, item.getPriceSmall());
        else ps.setNull(4, Types.REAL);
        if (item.getPriceLarge() != null) ps.setDouble(5, item.getPriceLarge());
        else ps.setNull(5, Types.REAL);
        ps.setString(6, item.getCategory() != null ? item.getCategory() : "General");
        ps.setInt(7, item.isAvailable() ? 1 : 0);
        ps.setInt(8, item.getDisplayOrder());
    }

    private List<MenuItem> query(String sql) {
        List<MenuItem> list = new ArrayList<>();
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private MenuItem mapRow(ResultSet rs) throws SQLException {
        MenuItem m = new MenuItem();
        m.setId(rs.getInt("id")); m.setName(rs.getString("name"));
        m.setItemType(ItemType.valueOf(rs.getString("item_type")));
        double pkg = rs.getDouble("price_per_kg"); if (!rs.wasNull()) m.setPricePerKg(pkg);
        double ps2 = rs.getDouble("price_small");  if (!rs.wasNull()) m.setPriceSmall(ps2);
        double pl  = rs.getDouble("price_large");  if (!rs.wasNull()) m.setPriceLarge(pl);
        m.setCategory(rs.getString("category"));
        m.setAvailable(rs.getInt("is_available") == 1);
        m.setDisplayOrder(rs.getInt("display_order"));
        return m;
    }
}
