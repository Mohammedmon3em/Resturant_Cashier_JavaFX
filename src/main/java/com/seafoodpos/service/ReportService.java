package com.seafoodpos.service;

import com.seafoodpos.db.OrderDAO;
import com.seafoodpos.db.SettingsDAO;
import com.seafoodpos.model.Order;

import javax.print.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.seafoodpos.db.InventoryDAO;
import com.seafoodpos.db.ExpenseDAO;

public class ReportService {
    private final OrderDAO    orderDAO = new OrderDAO();
    private final SettingsDAO settings = new SettingsDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ExpenseDAO expenseDAO = new ExpenseDAO();

    public record ReportData(
        String title, String titleAr, String periodLabel,
        LocalDate from, LocalDate to,
        List<Order> orders,
        List<Map<String,Object>> purchases,
        List<Map<String,Object>> expenses,
        Map<String, Object> summary) {}

    public ReportData getDailyReport(LocalDate date) {
        return build("Daily Report", "\u062a\u0642\u0631\u064a\u0631 \u064a\u0648\u0645\u064a",
            date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), date, date);
    }

    public ReportData getWeeklyReport(LocalDate any) {
        LocalDate mon = any.minusDays(any.getDayOfWeek().getValue() - 1);
        LocalDate sun = mon.plusDays(6);
        return build("Weekly Report", "\u062a\u0642\u0631\u064a\u0631 \u0623\u0633\u0628\u0648\u0639\u064a",
            mon + " to " + sun, mon, sun);
    }

    public ReportData getMonthlyReport(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return build("Monthly Report", "\u062a\u0642\u0631\u064a\u0631 \u0634\u0647\u0631\u064a",
            ym.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            ym.atDay(1), ym.atEndOfMonth());
    }

    public ReportData getQuarterlyReport(int year, int quarter) {
        int sm = (quarter - 1) * 3 + 1;
        LocalDate from = LocalDate.of(year, sm, 1);
        LocalDate to   = from.plusMonths(3).minusDays(1);
        return build("Q" + quarter + " " + year, "\u0631\u0628\u0639 " + quarter,
            "Q" + quarter + " " + year, from, to);
    }

    public ReportData getHalfYearlyReport(int year, boolean first) {
        LocalDate from = first ? LocalDate.of(year, 1, 1)  : LocalDate.of(year, 7, 1);
        LocalDate to   = first ? LocalDate.of(year, 6, 30) : LocalDate.of(year, 12, 31);
        return build((first ? "First" : "Second") + " Half " + year,
            first ? "\u0627\u0644\u0646\u0635\u0641 \u0627\u0644\u0623\u0648\u0644"
                  : "\u0627\u0644\u0646\u0635\u0641 \u0627\u0644\u062b\u0627\u0646\u064a",
            (first ? "H1" : "H2") + " " + year, from, to);
    }

    public ReportData getYearlyReport(int year) {
        return build("Yearly Report " + year,
            "\u0627\u0644\u062a\u0642\u0631\u064a\u0631 \u0627\u0644\u0633\u0646\u0648\u064a " + year,
            String.valueOf(year),
            LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    }

    private ReportData build(String title, String titleAr, String period,
                              LocalDate from, LocalDate to) {
        return new ReportData(title, titleAr, period, from, to,
            orderDAO.findByDateRange(from, to),
            inventoryDAO.getPurchases(from, to),
            expenseDAO.getExpenses(from, to),
            orderDAO.getReportSummary(from, to));
    }

    public void printReport(ReportData r, String category) {
        String content = generateReportText(r, category);
        try {
            String printerName = settings.get("printer_reports", "");
            javax.print.PrintService ps = null;
            if (!printerName.isBlank()) {
                for (javax.print.PrintService s : PrintServiceLookup.lookupPrintServices(null, null)) {
                    if (s.getName().equalsIgnoreCase(printerName.trim())) {
                        ps = s; break;
                    }
                }
            }
            if (ps == null) ps = PrintServiceLookup.lookupDefaultPrintService();

            if (ps != null) {
                DocPrintJob job = ps.createPrintJob();
                Doc doc = new SimpleDoc(content.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
                job.print(doc, null);
            } else System.out.println(content);
        } catch (Exception e) { System.out.println(content); }
    }

    public void saveAsPdf(ReportData r, java.io.File file, String category) throws Exception {
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
            org.apache.pdfbox.pdmodel.common.PDRectangle a4 = org.apache.pdfbox.pdmodel.common.PDRectangle.A4;
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage(a4);
            doc.addPage(page);
            
            de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D g2 = new de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D(doc, a4.getWidth(), a4.getHeight());
            
            // Draw Modern Background and Header
            g2.setColor(java.awt.Color.WHITE);
            g2.fillRect(0, 0, (int)a4.getWidth(), (int)a4.getHeight());
            
            java.awt.Color primary = new java.awt.Color(0, 180, 216); // #00b4d8
            java.awt.Color dark = new java.awt.Color(13, 27, 42); // #0d1b2a
            
            g2.setColor(primary);
            g2.fillRect(0, 0, (int)a4.getWidth(), 80);
            
            java.awt.Font titleFont = new java.awt.Font("Arial", java.awt.Font.BOLD, 24);
            g2.setFont(titleFont);
            g2.setColor(java.awt.Color.WHITE);
            g2.drawString(settings.get("restaurant_name", "AL-Muaid Fish Restaurant"), 40, 40);
            
            java.awt.Font subFont = new java.awt.Font("Arial", java.awt.Font.PLAIN, 14);
            g2.setFont(subFont);
            g2.drawString(category + " - " + r.periodLabel(), 40, 65);
            
            g2.setColor(dark);
            int y = 120;
            
            java.awt.Font normalFont = new java.awt.Font("Arial", java.awt.Font.PLAIN, 12);
            java.awt.Font boldFont = new java.awt.Font("Arial", java.awt.Font.BOLD, 12);
            String currency = settings.get("currency_symbol", "SDG");
            
            boolean isProfit = category.contains("Profit") || category.contains("الأرباح");
            
            if (isProfit) {
                // Draw summary metrics at the top
                g2.setFont(boldFont);
                g2.drawString("Profit Summary Metrics", 40, y);
                y += 20;
                g2.setFont(normalFont);
                
                double rev = (double) r.summary().getOrDefault("totalRevenue", 0.0);
                double pur = (double) r.summary().getOrDefault("totalPurchases", 0.0);
                double exp = (double) r.summary().getOrDefault("totalExpenses", 0.0);
                double dep = (double) r.summary().getOrDefault("totalDepreciation", 0.0);
                double net = (double) r.summary().getOrDefault("netProfit", 0.0);
                
                g2.drawString(String.format("Total Revenue: %.2f %s", rev, currency), 40, y);
                g2.drawString(String.format("Total Purchases: %.2f %s", pur, currency), 240, y);
                g2.drawString(String.format("Total Expenses: %.2f %s", exp, currency), 440, y);
                y += 20;
                g2.drawString(String.format("Depreciation: %.2f %s", dep, currency), 40, y);
                g2.setFont(boldFont);
                g2.drawString(String.format("Net Profit: %.2f %s", net, currency), 240, y);
                g2.setFont(normalFont);
                y += 40;
            } else {
                // No summary metrics for other reports!
            }
            
            // Draw Table based on category
            g2.setColor(primary);
            g2.fillRect(40, y, (int)a4.getWidth() - 80, 25);
            g2.setColor(java.awt.Color.WHITE);
            g2.setFont(boldFont);
            
            boolean isSales = category.contains("Sales") || category.contains("المبيعات");
            boolean isPurch = category.contains("Purchase") || category.contains("المشتريات");
            boolean isExp = category.contains("Expenses") || category.contains("المصروفات");
            
            if (isSales) {
                g2.drawString("Order #", 50, y + 17);
                g2.drawString("Cashier", 150, y + 17);
                g2.drawString("Amount", 350, y + 17);
                g2.drawString("Time", 450, y + 17);
                y += 25;
                g2.setColor(dark);
                g2.setFont(normalFont);
                for (Order o : r.orders()) {
                    g2.drawString(o.getOrderNumber(), 50, y + 15);
                    g2.drawString(o.getCashierName(), 150, y + 15);
                    g2.drawString(String.format("%.2f", o.getTotalAmount()), 350, y + 15);
                    g2.drawString(o.getCreatedAt() != null ? o.getCreatedAt().toString().replace("T", " ").substring(0,16) : "", 450, y + 15);
                    y += 20;
                    if (y > a4.getHeight() - 50) break; // simplistic pagination guard
                }
                y += 10;
                g2.setColor(primary);
                g2.drawLine(40, y, (int)a4.getWidth() - 40, y);
                y += 20;
                g2.setFont(boldFont);
                g2.setColor(dark);
                double rev = (double) r.summary().getOrDefault("totalRevenue", 0.0);
                g2.drawString(String.format("Total Sales: %.2f %s", rev, currency), 350, y);
            } else if (isPurch) {
                g2.drawString("Item Name", 50, y + 17);
                g2.drawString("Quantity", 250, y + 17);
                g2.drawString("Cost", 350, y + 17);
                g2.drawString("Time", 450, y + 17);
                y += 25;
                g2.setColor(dark);
                g2.setFont(normalFont);
                for (Map<String,Object> p : r.purchases()) {
                    g2.drawString(String.valueOf(p.get("itemName")), 50, y + 15);
                    g2.drawString(String.format("%.2f kg", (Double)p.get("quantity")), 250, y + 15);
                    g2.drawString(String.format("%.2f", (Double)p.get("cost")), 350, y + 15);
                    g2.drawString(String.valueOf(p.get("date")).replace("T", " ").substring(0,16), 450, y + 15);
                    y += 20;
                    if (y > a4.getHeight() - 50) break;
                }
                y += 10;
                g2.setColor(primary);
                g2.drawLine(40, y, (int)a4.getWidth() - 40, y);
                y += 20;
                g2.setFont(boldFont);
                g2.setColor(dark);
                double pur = (double) r.summary().getOrDefault("totalPurchases", 0.0);
                g2.drawString(String.format("Total Purchases: %.2f %s", pur, currency), 350, y);
            } else if (isExp) {
                g2.drawString("Expense Name", 50, y + 17);
                g2.drawString("Amount", 350, y + 17);
                g2.drawString("Time", 450, y + 17);
                y += 25;
                g2.setColor(dark);
                g2.setFont(normalFont);
                for (Map<String,Object> ex : r.expenses()) {
                    g2.drawString(String.valueOf(ex.get("name")), 50, y + 15);
                    g2.drawString(String.format("%.2f", (Double)ex.get("amount")), 350, y + 15);
                    g2.drawString(String.valueOf(ex.get("date")).replace("T", " ").substring(0,16), 450, y + 15);
                    y += 20;
                    if (y > a4.getHeight() - 50) break;
                }
                y += 10;
                g2.setColor(primary);
                g2.drawLine(40, y, (int)a4.getWidth() - 40, y);
                y += 20;
                g2.setFont(boldFont);
                g2.setColor(dark);
                double exp = (double) r.summary().getOrDefault("totalExpenses", 0.0);
                g2.drawString(String.format("Total Expenses: %.2f %s", exp, currency), 350, y);
            }
            
            g2.dispose();
            try (org.apache.pdfbox.pdmodel.PDPageContentStream cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page)) {
                cs.drawForm(g2.getXFormObject());
            }
            doc.save(file);
        }
    }

    private String generateReportText(ReportData r, String category) {
        String currency = settings.get("currency_symbol", "SDG");
        int    cnt = (int)    r.summary().getOrDefault("orderCount",   0);
        double rev = (double) r.summary().getOrDefault("totalRevenue", 0.0);
        double pur = (double) r.summary().getOrDefault("totalPurchases", 0.0);
        double exp = (double) r.summary().getOrDefault("totalExpenses", 0.0);
        double dep = (double) r.summary().getOrDefault("totalDepreciation", 0.0);
        double net = (double) r.summary().getOrDefault("netProfit", 0.0);
        
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(50)).append("\n")
          .append(c50(r.title())).append("\n")
          .append(c50(r.titleAr())).append("\n")
          .append(c50("Period: " + r.periodLabel())).append("\n")
          .append("=".repeat(50)).append("\n");
          
        boolean isProfit = category.contains("Profit") || category.contains("الأرباح");
        if (isProfit) {
            sb.append(String.format("Revenue / \u0625\u064a\u0631\u0627\u062f\u0627\u062a: %.2f %s%n", rev, currency))
              .append(String.format("Purchases / المشتريات: %.2f %s%n", pur, currency))
              .append(String.format("Expenses / \u0645\u0635\u0631\u0648\u0641\u0627\u062a: %.2f %s%n", exp, currency))
              .append(String.format("Depreciation / الإهلاك: %.2f %s%n", dep, currency))
              .append(String.format("Net Profit / \u0635\u0627\u0641\u064a \u0627\u0644\u0631\u0628\u062d: %.2f %s%n", net, currency))
              .append("-".repeat(50)).append("\n");
        } else {
            sb.append(String.format("Orders / \u0637\u0644\u0628\u0627\u062a: %d%n", cnt))
              .append("-".repeat(50)).append("\n");
        }
        boolean isSales = category.contains("Sales") || category.contains("المبيعات");
        boolean isPurch = category.contains("Purchase") || category.contains("المشتريات");
        boolean isExp = category.contains("Expenses") || category.contains("المصروفات");

        if (isSales) {
            sb.append(String.format("%-10s %-15s %-10s %-15s%n", "Order #", "Cashier", "Amount", "Time"));
            sb.append("-".repeat(50)).append("\n");
            for (Order o : r.orders()) {
                sb.append(String.format("%-10s %-15s %-10.2f %-15s%n", 
                    o.getOrderNumber(), 
                    o.getCashierName().length() > 14 ? o.getCashierName().substring(0,14) : o.getCashierName(), 
                    o.getTotalAmount(), 
                    o.getCreatedAt() != null ? o.getCreatedAt().toString().replace("T", " ").substring(0,16) : ""));
            }
            sb.append("-".repeat(50)).append("\n");
            sb.append(String.format("Total Sales: %.2f %s%n", rev, currency));
        } else if (isPurch) {
            sb.append(String.format("%-15s %-10s %-10s %-15s%n", "Item Name", "Qty", "Cost", "Time"));
            sb.append("-".repeat(50)).append("\n");
            for (Map<String,Object> p : r.purchases()) {
                String n = String.valueOf(p.get("itemName"));
                sb.append(String.format("%-15s %-10.2f %-10.2f %-15s%n", 
                    n.length() > 14 ? n.substring(0,14) : n, 
                    (Double)p.get("quantity"), 
                    (Double)p.get("cost"), 
                    String.valueOf(p.get("date")).replace("T", " ").substring(0,16)));
            }
            sb.append("-".repeat(50)).append("\n");
            sb.append(String.format("Total Purchases: %.2f %s%n", pur, currency));
        } else if (isExp) {
            sb.append(String.format("%-25s %-10s %-15s%n", "Expense Name", "Amount", "Time"));
            sb.append("-".repeat(50)).append("\n");
            for (Map<String,Object> ex : r.expenses()) {
                String n = String.valueOf(ex.get("name"));
                sb.append(String.format("%-25s %-10.2f %-15s%n", 
                    n.length() > 24 ? n.substring(0,24) : n, 
                    (Double)ex.get("amount"), 
                    String.valueOf(ex.get("date")).replace("T", " ").substring(0,16)));
            }
            sb.append("-".repeat(50)).append("\n");
            sb.append(String.format("Total Expenses: %.2f %s%n", exp, currency));
        }

        sb.append("=".repeat(50)).append("\n\n\n");
        return sb.toString();
    }

    private String c50(String t) {
        if (t == null) return "";
        int p = Math.max(0, (50 - t.length()) / 2);
        return " ".repeat(p) + t;
    }
}
