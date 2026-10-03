package com.seafoodpos.controller;

import com.seafoodpos.model.Order;
import com.seafoodpos.model.Role;
import com.seafoodpos.service.ReportService;
import com.seafoodpos.util.AlertHelper;
import com.seafoodpos.util.I18n;
import com.seafoodpos.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.time.LocalDate;
import java.util.Map;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {
    @FXML private Label                    titleLabel;
    @FXML private ComboBox<String>         reportCategoryCombo;
    @FXML private ComboBox<String>         reportTypeCombo;
    @FXML private DatePicker               datePicker;
    @FXML private ComboBox<String>         monthCombo;
    @FXML private ComboBox<Integer>        yearCombo;
    @FXML private ComboBox<String>         quarterCombo;
    @FXML private ComboBox<String>         halfCombo;
    @FXML private Button                   btnGenerate, btnPrint, btnSavePdf;
    @FXML private javafx.scene.layout.FlowPane summaryCardsPane;
    @FXML private Label                    lblTotalRevenue, lblTotalPurchases, lblTotalExpenses, lblTotalDepreciation, lblNetProfit;
    @FXML private TableView<Order>         ordersTable;
    @FXML private TableColumn<Order,String> colOrderNum;
    @FXML private TableColumn<Order,String> colCashier;
    @FXML private TableColumn<Order,Double> colAmount;
    @FXML private TableColumn<Order,String> colTime;


    @FXML private TableView<Map<String,Object>> purchasesTable;
    @FXML private TableColumn<Map<String,Object>,String> colPurchItem;
    @FXML private TableColumn<Map<String,Object>,Double> colPurchQty;
    @FXML private TableColumn<Map<String,Object>,Double> colPurchCost;
    @FXML private TableColumn<Map<String,Object>,String> colPurchTime;

    @FXML private TableView<Map<String,Object>> expensesTable;
    @FXML private TableColumn<Map<String,Object>,String> colExpName;
    @FXML private TableColumn<Map<String,Object>,Double> colExpAmount;
    @FXML private TableColumn<Map<String,Object>,String> colExpTime;

    private final ReportService rs = new ReportService();
    private final com.seafoodpos.db.InventoryDAO inventoryDAO = new com.seafoodpos.db.InventoryDAO();
    private final com.seafoodpos.db.ExpenseDAO expenseDAO = new com.seafoodpos.db.ExpenseDAO();
    
    private ReportService.ReportData current;
    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    private final ObservableList<Map<String,Object>> purchases = FXCollections.observableArrayList();
    private final ObservableList<Map<String,Object>> expenses = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        applyI18n(); setupControls(); setupTable();
    }

    private void applyI18n() {
        titleLabel.setText(I18n.t("report.title"));
        btnGenerate.setText(I18n.t("report.generate"));
        btnSavePdf.setText("Save PDF / حفظ PDF");
        btnPrint.setText(I18n.t("report.print"));
    }

    private void setupControls() {
        boolean restricted = SessionManager.getInstance().getCurrentUser() != null
            && SessionManager.getInstance().getCurrentUser().getRole() == Role.CASHIER;

        if (restricted) {
            reportTypeCombo.getItems().add(I18n.t("report.daily"));
        } else {
            reportTypeCombo.getItems().addAll(
                I18n.t("report.daily"), I18n.t("report.weekly"),
                I18n.t("report.monthly"), I18n.t("report.quarterly"),
                I18n.t("report.half_yearly"), I18n.t("report.yearly"));
        }
        reportTypeCombo.setValue(I18n.t("report.daily"));
        datePicker.setValue(LocalDate.now());

        monthCombo.getItems().addAll("January","February","March","April",
            "May","June","July","August","September","October","November","December");
        monthCombo.setValue("January");

        int yr = LocalDate.now().getYear();
        for (int y = yr; y >= yr - 5; y--) yearCombo.getItems().add(y);
        yearCombo.setValue(yr);

        quarterCombo.getItems().addAll("Q1","Q2","Q3","Q4");
        quarterCombo.setValue("Q1");
        halfCombo.getItems().addAll(I18n.t("report.first_half"), I18n.t("report.second_half"));
        halfCombo.setValue(I18n.t("report.first_half"));

        reportCategoryCombo.getItems().addAll(
            I18n.isArabic() ? "تقرير المبيعات" : "Sales Report",
            I18n.isArabic() ? "تقرير المشتريات" : "Purchase Report",
            I18n.isArabic() ? "تقرير المصروفات" : "Expenses Report",
            I18n.isArabic() ? "تقرير الأرباح" : "Profit Report"
        );
        reportCategoryCombo.setValue(reportCategoryCombo.getItems().get(0));
        reportCategoryCombo.valueProperty().addListener((o,old,v) -> updateCategoryVis(v));
        updateCategoryVis(reportCategoryCombo.getValue());

        reportTypeCombo.valueProperty().addListener((o,old,v) -> updateVis(v));
        updateVis(I18n.t("report.daily"));
        btnPrint.setDisable(true);
        btnSavePdf.setDisable(true);
    }

    private void updateCategoryVis(String cat) {
        boolean sales = cat.contains("Sales") || cat.contains("المبيعات");
        boolean purch = cat.contains("Purchase") || cat.contains("المشتريات");
        boolean exp = cat.contains("Expenses") || cat.contains("المصروفات");
        boolean profit = cat.contains("Profit") || cat.contains("الأرباح");
        
        sv(ordersTable, sales);
        sv(purchasesTable, purch);
        sv(expensesTable, exp);
        sv(summaryCardsPane, profit);
    }

    private void updateVis(String type) {
        boolean daily      = type.equals(I18n.t("report.daily"));
        boolean weekly     = type.equals(I18n.t("report.weekly"));
        boolean monthly    = type.equals(I18n.t("report.monthly"));
        boolean quarterly  = type.equals(I18n.t("report.quarterly"));
        boolean halfYearly = type.equals(I18n.t("report.half_yearly"));
        sv(datePicker,   daily || weekly);
        sv(monthCombo,   monthly);
        sv(quarterCombo, quarterly);
        sv(halfCombo,    halfYearly);
        sv(yearCombo,    !(daily || weekly));
    }

    private void sv(Node n, boolean v) { n.setVisible(v); n.setManaged(v); }

    private void setupTable() {
        colOrderNum.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colCashier.setCellValueFactory(new PropertyValueFactory<>("cashierName"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colTime.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getCreatedAt() != null
            ? d.getValue().getCreatedAt().toString().replace("T"," ").substring(0,19) : ""));
        colAmount.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean e) {
                super.updateItem(v, e);
                if (!e && v != null) { setText(String.format("%.2f %s", v, I18n.t("common.currency"))); setStyle("-fx-text-fill:#06d6a0;"); }
                else { setText(null); setStyle(""); }
            }
        });
        ordersTable.setItems(orders);

        // Purchases Table
        colPurchItem.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("itemName"))));
        colPurchQty.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>((Double) d.getValue().get("quantity")));
        colPurchCost.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>((Double) d.getValue().get("cost")));
        colPurchTime.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("date")).replace("T"," ").substring(0, Math.min(19, String.valueOf(d.getValue().get("date")).length()))));
        purchasesTable.setItems(purchases);
        colPurchCost.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean e) {
                super.updateItem(v, e);
                if (!e && v != null) { setText(String.format("%.2f %s", v, I18n.t("common.currency"))); }
                else { setText(null); }
            }
        });

        // Expenses Table
        colExpName.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("name"))));
        colExpAmount.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>((Double) d.getValue().get("amount")));
        colExpTime.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("date")).replace("T"," ").substring(0, Math.min(19, String.valueOf(d.getValue().get("date")).length()))));
        expensesTable.setItems(expenses);
        colExpAmount.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean e) {
                super.updateItem(v, e);
                if (!e && v != null) { setText(String.format("%.2f %s", v, I18n.t("common.currency"))); }
                else { setText(null); }
            }
        });
    }

    @FXML private void handleGenerate() {
        try {
            String type = reportTypeCombo.getValue();
            int year = yearCombo.getValue() != null ? yearCombo.getValue() : LocalDate.now().getYear();
            if      (type.equals(I18n.t("report.daily")))
                current = rs.getDailyReport(datePicker.getValue());
            else if (type.equals(I18n.t("report.weekly")))
                current = rs.getWeeklyReport(datePicker.getValue());
            else if (type.equals(I18n.t("report.monthly")))
                current = rs.getMonthlyReport(year, monthCombo.getSelectionModel().getSelectedIndex() + 1);
            else if (type.equals(I18n.t("report.quarterly")))
                current = rs.getQuarterlyReport(year, quarterCombo.getSelectionModel().getSelectedIndex() + 1);
            else if (type.equals(I18n.t("report.half_yearly")))
                current = rs.getHalfYearlyReport(year, halfCombo.getValue().equals(I18n.t("report.first_half")));
            else
                current = rs.getYearlyReport(year);

            displayReport(current);
            btnPrint.setDisable(false);
            btnSavePdf.setDisable(false);
        } catch (Exception e) {
            AlertHelper.showError(I18n.t("common.error"), e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayReport(ReportService.ReportData r) {
        double rev = (double) r.summary().getOrDefault("totalRevenue", 0.0);
        double pur = (double) r.summary().getOrDefault("totalPurchases", 0.0);
        double exp = (double) r.summary().getOrDefault("totalExpenses", 0.0);
        double dep = (double) r.summary().getOrDefault("totalDepreciation", 0.0);
        double net = (double) r.summary().getOrDefault("netProfit", 0.0);
        
        String cur = I18n.t("common.currency");
        lblTotalRevenue.setText(String.format("%.2f %s", rev, cur));
        lblTotalPurchases.setText(String.format("%.2f %s", pur, cur));
        lblTotalExpenses.setText(String.format("%.2f %s", exp, cur));
        lblTotalDepreciation.setText(String.format("%.2f %s", dep, cur));
        lblNetProfit.setText(String.format("%.2f %s", net, cur));
        orders.setAll(r.orders());
        
        purchases.setAll(inventoryDAO.getPurchases(r.from(), r.to()));
        expenses.setAll(expenseDAO.getExpenses(r.from(), r.to()));
    }

    @FXML private void handlePrint() {
        if (current != null) {
            String cat = reportCategoryCombo.getValue();
            rs.printReport(current, cat);
            AlertHelper.showInfo(I18n.t("common.success"), "Report sent to printer");
        }
    }

    @FXML private void handleSavePdf() {
        if (current == null) return;
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Save Report as PDF");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        String cleanTitle = current.title().replaceAll("[^a-zA-Z0-9_-]", "_");
        fc.setInitialFileName("Report_" + cleanTitle + ".pdf");
        java.io.File file = fc.showSaveDialog(btnSavePdf.getScene().getWindow());
        if (file != null) {
            try {
                String cat = reportCategoryCombo.getValue();
                rs.saveAsPdf(current, file, cat);
                AlertHelper.showInfo(I18n.t("common.success"), "Report saved to PDF successfully!\nتم الحفظ بنجاح");
            } catch (Exception e) {
                AlertHelper.showError("PDF Error", "Failed to save PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
