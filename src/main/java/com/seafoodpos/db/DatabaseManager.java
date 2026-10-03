package com.seafoodpos.db;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.*;

public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String DB_URL = "jdbc:sqlite:seafood_pos.db";
    private Connection connection;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            try (Statement s = connection.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON");
                s.execute("PRAGMA journal_mode = WAL");
            }
            createTables();
            seedDefaultData();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                try (Statement s = connection.createStatement()) {
                    s.execute("PRAGMA foreign_keys = ON");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get DB connection", e);
        }
        return connection;
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    username   TEXT    NOT NULL UNIQUE COLLATE NOCASE,
                    password   TEXT    NOT NULL,
                    full_name  TEXT    NOT NULL,
                    role       TEXT    NOT NULL CHECK(role IN ('ADMIN','MODERATOR','CASHIER')),
                    is_active  INTEGER NOT NULL DEFAULT 1,
                    created_at TEXT    NOT NULL DEFAULT (datetime('now'))
                )""");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS menu_items (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    name          TEXT    NOT NULL,
                    item_type     TEXT    NOT NULL CHECK(item_type IN ('FISH_MEAL','JUICE')),
                    price_per_kg  REAL,
                    price_small   REAL,
                    price_large   REAL,
                    category      TEXT    DEFAULT 'General',
                    is_available  INTEGER NOT NULL DEFAULT 1,
                    display_order INTEGER NOT NULL DEFAULT 0,
                    created_at    TEXT    NOT NULL DEFAULT (datetime('now'))
                )""");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_number TEXT    NOT NULL UNIQUE,
                    cashier_id   INTEGER NOT NULL REFERENCES users(id),
                    total_amount REAL    NOT NULL DEFAULT 0,
                    status       TEXT    NOT NULL DEFAULT 'COMPLETED',
                    notes        TEXT,
                    created_at   TEXT    NOT NULL DEFAULT (datetime('now'))
                )""");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS order_items (
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id     INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                    menu_item_id INTEGER NOT NULL,
                    item_name    TEXT    NOT NULL,
                    item_type    TEXT    NOT NULL,
                    weight_kg    REAL,
                    price_per_kg REAL,
                    juice_size   TEXT,
                    quantity     INTEGER NOT NULL DEFAULT 1,
                    cooking_method TEXT,
                    unit_price   REAL,
                    line_total   REAL    NOT NULL
                )""");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS daily_order_sequence (
                    date_key TEXT    PRIMARY KEY,
                    last_seq INTEGER NOT NULL DEFAULT 0
                )""");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS settings (
                    key   TEXT PRIMARY KEY,
                    value TEXT
                )""");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inventory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_name TEXT NOT NULL UNIQUE,
                    w1_quantity_kg REAL NOT NULL DEFAULT 0,
                    w2_quantity_kg REAL NOT NULL DEFAULT 0
                )""");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inventory_purchases (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    inventory_id INTEGER NOT NULL REFERENCES inventory(id),
                    quantity_kg REAL NOT NULL,
                    total_cost REAL NOT NULL,
                    purchase_date TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
                )""");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS cleaning_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    inventory_id INTEGER NOT NULL REFERENCES inventory(id),
                    w1_quantity_used REAL NOT NULL,
                    w2_quantity_produced REAL NOT NULL,
                    cleaning_date TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
                )""");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    amount REAL NOT NULL,
                    expense_date TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
                )""");
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS daily_depreciation (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    amount REAL NOT NULL,
                    dep_date TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
                )""");
            
            // Migration for existing databases
            try { stmt.execute("ALTER TABLE order_items ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE order_items ADD COLUMN cooking_method TEXT"); } catch (SQLException ignored) {}
        }
    }

    private void seedDefaultData() throws SQLException {
        // Seed default admin user
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.getInt(1) == 0) {
                insertUser("admin",     "Admin123",  "System Administrator", "ADMIN");
            }
        }
        // Seed sample menu
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM menu_items")) {
            if (rs.getInt(1) == 0) {
                insertFish("Hammour (Grouper)", "Grilled",  85.0, 1);
                insertFish("Seabass",           "Grilled",  75.0, 2);
                insertFish("Red Snapper",       "Grilled",  70.0, 3);
                insertFish("Safi (Rabbitfish)", "Grilled",  65.0, 4);
                insertFish("Shrimp",            "Fried",    90.0, 5);
                insertFish("Calamari",          "Fried",    65.0, 6);
                insertFish("Crab",              "Steamed",  95.0, 7);
                insertFish("Lobster",           "Steamed", 150.0, 8);
                insertFish("Salmon",            "Grilled",  80.0, 9);
                insertFish("Tuna Steak",        "Grilled",  78.0, 10);
                insertJuice("Fresh Mango Juice",  "Tropical", 12.0, 18.0, 11);
                insertJuice("Watermelon Juice",   "Fresh",    10.0, 15.0, 12);
                insertJuice("Orange Juice",       "Fresh",    10.0, 15.0, 13);
                insertJuice("Lemon Mint",         "Fresh",    10.0, 15.0, 14);
                insertJuice("Strawberry Juice",   "Tropical", 12.0, 18.0, 15);
                insertJuice("Mixed Fruit Juice",  "Tropical", 14.0, 20.0, 16);
            }
        }
        // Seed settings
        String[][] defaults = {
            {"restaurant_name",    "AL-Muaid Fish Restaurant"},
            {"restaurant_name_ar", "مطعم المؤيد للأسماك"},
            {"printer_customer",   ""},
            {"printer_kitchen",    ""},
            {"printer_juice",      ""},
            {"receipt_footer",     "Thank you for dining with us!"},
            {"receipt_footer_ar",  "\u0634\u0643\u0631\u0627\u064b \u0644\u0632\u064a\u0627\u0631\u062a\u0643\u0645"},
            {"currency_symbol",    "SDG"},
            {"ui_language",        "en"}
        };
        for (String[] kv : defaults) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR IGNORE INTO settings(key,value) VALUES(?,?)")) {
                ps.setString(1, kv[0]); ps.setString(2, kv[1]); ps.executeUpdate();
            }
        }
    }

    private void insertUser(String username, String password, String fullName, String role)
            throws SQLException {
        String hash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users(username,password,full_name,role) VALUES(?,?,?,?)")) {
            ps.setString(1, username); ps.setString(2, hash);
            ps.setString(3, fullName); ps.setString(4, role);
            ps.executeUpdate();
        }
    }

    private void insertFish(String name, String category, double priceKg, int order)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO menu_items(name,item_type,price_per_kg,category,display_order) VALUES(?,?,?,?,?)")) {
            ps.setString(1, name); ps.setString(2, "FISH_MEAL");
            ps.setDouble(3, priceKg); ps.setString(4, category); ps.setInt(5, order);
            ps.executeUpdate();
        }
    }

    private void insertJuice(String name, String category, double small, double large, int order)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO menu_items(name,item_type,price_small,price_large,category,display_order) VALUES(?,?,?,?,?,?)")) {
            ps.setString(1, name); ps.setString(2, "JUICE");
            ps.setDouble(3, small); ps.setDouble(4, large);
            ps.setString(5, category); ps.setInt(6, order);
            ps.executeUpdate();
        }
    }

    public void close() {
        try { if (connection != null && !connection.isClosed()) connection.close(); }
        catch (SQLException e) { e.printStackTrace(); }
    }
}
