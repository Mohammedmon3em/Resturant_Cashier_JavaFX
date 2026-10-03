package com.seafoodpos.service;

import com.seafoodpos.db.SettingsDAO;
import com.seafoodpos.model.ItemType;
import com.seafoodpos.model.Order;
import com.seafoodpos.model.OrderItem;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import java.time.format.DateTimeFormatter;

public class PrintService {
    private static final int WIDTH = 42;
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TM = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final SettingsDAO settings = new SettingsDAO();

    public void printOrder(Order order) {
        String rName    = settings.get("restaurant_name",    "AL-Muaid Fish Restaurant");
        String rNameAr  = settings.get("restaurant_name_ar", "مطعم المؤيد للأسماك");
        String footer   = settings.get("receipt_footer",     "Thank you for dining with us!");
        String footerAr = settings.get("receipt_footer_ar",  "\u0634\u0643\u0631\u0627\u064b \u0644\u0632\u064a\u0627\u0631\u062a\u0643\u0645");
        String currency = settings.get("currency_symbol",    "SDG");
        String pCust    = settings.get("printer_customer",   "");
        String pKitch   = settings.get("printer_kitchen",    "");
        String pJuice   = settings.get("printer_juice",      "");

        String date = order.getCreatedAt() != null ? order.getCreatedAt().format(DT)
                      : java.time.LocalDate.now().format(DT);
        String time = order.getCreatedAt() != null ? order.getCreatedAt().format(TM)
                      : java.time.LocalTime.now().format(TM);

        // Customer receipt (all items)
        sendToPrinter(buildCustomer(order, rName, rNameAr, date, time, footer, footerAr, currency),
            pCust, "Customer-" + order.getOrderNumber());

        // Kitchen slip (fish only)
        if (order.hasFishMeals())
            sendToPrinter(buildKitchen(order, rName, date, time),
                pKitch, "Kitchen-" + order.getOrderNumber());

        // Juice slip
        if (order.hasJuices())
            sendToPrinter(buildJuice(order, rName, date, time),
                pJuice, "Juice-" + order.getOrderNumber());
    }

    private String buildCustomer(Order o, String rName, String rNameAr, String date, String time,
                                  String footer, String footerAr, String currency) {
        StringBuilder sb = new StringBuilder();
        sb.append(line('=')).append(center(rName)).append("\n")
          .append(center(rNameAr)).append("\n")
          .append(center("Date / \u0627\u0644\u062a\u0627\u0631\u064a\u062e: " + date)).append("\n")
          .append(center("Time / \u0627\u0644\u0648\u0642\u062a: " + time)).append("\n")
          .append(line('='))
          .append(center("ORDER No. / \u0631\u0642\u0645 \u0627\u0644\u0637\u0644\u0628")).append("\n")
          .append(center("*** " + o.getOrderNumber() + " ***")).append("\n")
          .append(line('='));
        sb.append(pad("Item / \u0627\u0644\u0635\u0646\u0641", 26)).append(pad("Total", 14)).append("\n");
        sb.append(line('-'));
        for (OrderItem item : o.getItems()) {
            sb.append(pad(item.getItemName(), 26))
              .append(padL(String.format("%.2f %s", item.getLineTotal(), currency), 14)).append("\n")
              .append("  ").append(item.getDetailDisplay()).append("\n");
            if (item.getItemType() == ItemType.FISH_MEAL && item.getPricePerKg() != null)
                sb.append(String.format("  @ %.2f %s/kg%n", item.getPricePerKg(), currency));
        }
        sb.append(line('='))
          .append(pad("TOTAL / \u0627\u0644\u0625\u062c\u0645\u0627\u0644\u064a:", 26))
          .append(padL(String.format("%.2f %s", o.getTotalAmount(), currency), 14)).append("\n")
          .append(line('='))
          .append(center(footer)).append("\n")
          .append(center(footerAr)).append("\n")
          .append(center("Cashier: " + o.getCashierName())).append("\n")
          .append(line('=')).append("\n\n\n");
        return sb.toString();
    }

    private String buildKitchen(Order o, String rName, String date, String time) {
        StringBuilder sb = new StringBuilder();
        sb.append(line('='))
          .append(center("** KITCHEN / \u0627\u0644\u0645\u0637\u0628\u062e **")).append("\n")
          .append(center(rName)).append("\n")
          .append(center(date + " " + time)).append("\n")
          .append(line('='))
          .append(center("ORDER *** " + o.getOrderNumber() + " ***")).append("\n")
          .append(line('='));
        for (OrderItem item : o.getFishMeals())
            sb.append(">> ").append(item.getItemName()).append("\n")
              .append("   ").append(item.getDetailDisplay()).append("\n");
        sb.append(line('=')).append(center("Items: " + o.getFishMeals().size())).append("\n\n\n");
        return sb.toString();
    }

    private String buildJuice(Order o, String rName, String date, String time) {
        StringBuilder sb = new StringBuilder();
        sb.append(line('='))
          .append(center("** JUICE / \u0627\u0644\u0639\u0635\u0627\u0626\u0631 **")).append("\n")
          .append(center(rName)).append("\n")
          .append(center(date + " " + time)).append("\n")
          .append(line('='))
          .append(center("ORDER *** " + o.getOrderNumber() + " ***")).append("\n")
          .append(line('='));
        for (OrderItem item : o.getJuices()) {
            String qtyPrefix = item.getQuantity() > 1 ? item.getQuantity() + "x " : "";
            sb.append(">> ").append(qtyPrefix).append(item.getItemName()).append("\n")
              .append("   ").append(item.getDetailDisplay()).append("\n");
        }
        sb.append(line('=')).append(center("Items: " + o.getJuices().size())).append("\n\n\n");
        return sb.toString();
    }

    private void sendToPrinter(String content, String printerName, String jobName) {
        javax.print.PrintService ps = findPrinter(printerName);
        if (ps == null) {
            System.out.println("=== PRINT (no printer configured: " + printerName + "): " + jobName + " ===");
            System.out.println(content);
            return;
        }
        try {
            DocPrintJob job = ps.createPrintJob();
            Doc doc = new SimpleDoc(content.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
            job.print(doc, new HashPrintRequestAttributeSet());
        } catch (Exception e) {
            System.err.println("Print failed: " + jobName);
            e.printStackTrace();
            System.out.println(content);
        }
    }

    private javax.print.PrintService findPrinter(String name) {
        if (name == null || name.isBlank()) return null;
        for (javax.print.PrintService ps : PrintServiceLookup.lookupPrintServices(null, null))
            if (ps.getName().equalsIgnoreCase(name.trim())) return ps;
        return null;
    }

    public javax.print.PrintService[] getAvailablePrinters() {
        return PrintServiceLookup.lookupPrintServices(null, null);
    }

    private String line(char c) { return String.valueOf(c).repeat(WIDTH) + "\n"; }
    private String center(String t) {
        if (t == null) return "";
        int pad = Math.max(0, (WIDTH - t.length()) / 2);
        return " ".repeat(pad) + t;
    }
    private String pad(String t, int w) {
        if (t == null) t = "";
        return t.length() >= w ? t.substring(0, w) : t + " ".repeat(w - t.length());
    }
    private String padL(String t, int w) {
        if (t == null) t = "";
        return t.length() >= w ? t : " ".repeat(w - t.length()) + t;
    }
}
