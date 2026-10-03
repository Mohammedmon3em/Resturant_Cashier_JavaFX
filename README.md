# Al Muaid Restaurant POS System
*(نظام نقاط البيع لمطعم المؤيد للمأكولات البحرية)*

A comprehensive, modern Point of Sale (POS) system tailored specifically for a seafood restaurant environment. It features a complete cashier window, advanced reporting, dual-warehouse inventory management (to account for raw vs. cleaned weights), and dynamic multi-language support (English & Arabic).

---

## 🚀 Key Features (المميزات الرئيسية)

### 🛒 Cashier & Ordering (نافذة الكاشير والطلبات)
- **Interactive Menu:** Highly visual order screen categorized into Drinks, Main Courses, and Appetizers.
- **Dynamic Shopping Cart:** Add items, modify quantities, delete items, and auto-calculate VAT and service charges.
- **Dining Preferences:** Easily switch between 'Dining In' and 'Takeaway' orders.
- **Cooking Instructions:** Log specific cooking methods required for each dish directly into the cart.

### 📦 Inventory Management (إدارة المخزون المزدوج)
- **Dual-Warehouse System:** Specialized feature to track both purchase quantities (raw) and usable quantities (after cleaning and scaling).
- **Purchase Logging:** Track supplier purchases, quantities, and direct material costs.

### 💰 Financials & Expenses (إدارة المالية والمصروفات)
- **Expense Logging:** Track daily operating expenses (rent, utilities, etc.).
- **Daily Depreciation:** Dedicated feature to calculate and log daily asset depreciation values at closing time.

### 📊 Advanced Reporting (التقارير المتقدمة)
- **Multiple Report Types:** Generate distinct reports for Sales, Purchases, Expenses, and Net Profit.
- **Flexible Timeframes:** Filter data daily, weekly, monthly, quarterly, or yearly.
- **Modern PDF Export:** Prints professional, styled A4 receipts and summary metric PDF reports using a custom rendering engine.

### ⚙️ System & User Management (إدارة النظام والمستخدمين)
- **Role-based Access Control:** Strict division between 'Admin' (full access) and 'User' / Cashier (restricted access).
- **User Management:** Create/remove users, activate/deactivate accounts, and reset passwords securely.
- **Menu Customization:** Admins can easily add, edit, or remove menu items and categories.
- **Bilingual Interface:** Real-time toggling between English and Arabic throughout the entire system.
- **Settings configuration:** Dynamic configurations for Restaurant Name, Currency, and VAT percentage.

---

## 🛠️ Tech Stack (التقنيات المستخدمة)

### Core Technologies
- **Java (JDK 17/21):** The core robust programming language powering the application's backend logic.
- **JavaFX:** Utilized for building a modern, interactive, and highly responsive Graphical User Interface (GUI).
- **FXML & CSS:** Used for strictly separating the UI layout (FXML) from the modern visual aesthetics (CSS).

### Database & Storage
- **SQLite:** Embedded, serverless SQL database used to store products, orders, inventory, expenses, and users locally without complex database setups.
- **JDBC:** Java Database Connectivity for executing secure parameterized SQL queries.

### Libraries & Tools
- **Maven:** Robust project management and dependency management.
- **Apache PDFBox & PdfBoxGraphics2D:** Advanced libraries utilized for drawing complex, formatted PDF receipts and financial reports.
- **Bcrypt:** State-of-the-art password hashing to ensure user passwords are encrypted securely in the database.
- **SLF4J:** Simple Logging Facade for Java for diagnostic logging.

---

## 💻 Installation & Setup

1. **Clone the repository** to your local machine.
2. Ensure you have **Java 17 (or newer)** and **Maven** installed.
3. Open a terminal in the root directory and build the project:
   ```bash
   mvn clean package
   ```
4. Run the compiled application:
   ```bash
   java -jar target/seafood-pos-1.0.0-shaded.jar
   ```

*(The SQLite database `pos.db` will be auto-generated on first run).*

---

