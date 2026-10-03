package com.seafoodpos.db;

import com.seafoodpos.model.Role;
import com.seafoodpos.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    public User findByUsername(String username) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM users WHERE LOWER(username)=LOWER(?) AND is_active=1")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public User findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM users WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users ORDER BY full_name")) {
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    public boolean insert(User user) {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO users(username,password,full_name,role,is_active) VALUES(?,?,?,?,?)")) {
            ps.setString(1, user.getUsername()); ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName()); ps.setString(4, user.getRole().name());
            ps.setInt(5, user.isActive() ? 1 : 0);
            ps.executeUpdate(); return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(User user) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE users SET full_name=?,role=?,is_active=? WHERE id=?")) {
            ps.setString(1, user.getFullName()); ps.setString(2, user.getRole().name());
            ps.setInt(3, user.isActive() ? 1 : 0); ps.setInt(4, user.getId());
            ps.executeUpdate(); return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean setActive(int id, boolean active) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE users SET is_active=? WHERE id=?")) {
            ps.setInt(1, active ? 1 : 0); ps.setInt(2, id);
            ps.executeUpdate(); return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updatePassword(int id, String hashedPass) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE users SET password=? WHERE id=?")) {
            ps.setString(1, hashedPass); ps.setInt(2, id);
            ps.executeUpdate(); return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM users WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate(); return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"), rs.getString("username"), rs.getString("password"),
            rs.getString("full_name"), Role.valueOf(rs.getString("role")),
            rs.getInt("is_active") == 1, rs.getString("created_at"));
    }
}
