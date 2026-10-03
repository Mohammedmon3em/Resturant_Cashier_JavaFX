package com.seafoodpos.controller;

import com.seafoodpos.db.SettingsDAO;
import com.seafoodpos.util.AlertHelper;
import com.seafoodpos.util.I18n;
import com.seafoodpos.util.NavigationHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javax.print.PrintServiceLookup;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Settings controller.
 * Uses fully-qualified javax.print.PrintService where needed to avoid
 * collision with com.seafoodpos.service.PrintService.
 */
public class SettingsController implements Initializable {
    @FXML private Label        titleLabel;
    @FXML private TextField    restaurantNameField;
    @FXML private TextField    restaurantNameArField;
    @FXML private TextField    currencyField;
    @FXML private TextField    footerField;
    @FXML private TextField    footerArField;
    @FXML private ComboBox<String> printerCustomerCombo;
    @FXML private ComboBox<String> printerKitchenCombo;
    @FXML private ComboBox<String> printerJuiceCombo;
    @FXML private ComboBox<String> printerReportsCombo;
    @FXML private ComboBox<String> languageCombo;
    @FXML private Button       btnSave, btnTestPrint, btnRefresh;

    private final SettingsDAO settingsDAO = new SettingsDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        applyI18n();
        loadPrinters();
        loadSettings();
    }

    private void applyI18n() {
        titleLabel.setText(I18n.t("settings.title"));
        btnSave.setText(I18n.t("settings.save"));
        btnTestPrint.setText(I18n.t("settings.test_print"));
        btnRefresh.setText(I18n.t("settings.refresh"));
        languageCombo.getItems().addAll("English", "Arabic / \u0639\u0631\u0628\u064a");
        languageCombo.setValue(I18n.isArabic() ? "Arabic / \u0639\u0631\u0628\u064a" : "English");
    }

    private void loadPrinters() {
        // Use fully-qualified javax.print.PrintService to avoid collision
        javax.print.PrintService[] printers = PrintServiceLookup.lookupPrintServices(null, null);
        String none = "-- None --";
        for (ComboBox<String> cb : java.util.List.of(
                printerCustomerCombo, printerKitchenCombo, printerJuiceCombo, printerReportsCombo)) {
            cb.getItems().clear();
            cb.getItems().add(none);
            for (javax.print.PrintService ps : printers) cb.getItems().add(ps.getName());
            cb.setValue(none);
        }
    }

    private void loadSettings() {
        restaurantNameField.setText(settingsDAO.get("restaurant_name",    "AL-Muaid Fish"));
        restaurantNameArField.setText(settingsDAO.get("restaurant_name_ar", ""));
        currencyField.setText(settingsDAO.get("currency_symbol",          "SDG"));
        footerField.setText(settingsDAO.get("receipt_footer",             "Thank you for dining with us!"));
        footerArField.setText(settingsDAO.get("receipt_footer_ar",        ""));

        String pc = settingsDAO.get("printer_customer", "");
        String pk = settingsDAO.get("printer_kitchen",  "");
        String pj = settingsDAO.get("printer_juice",    "");
        String pr = settingsDAO.get("printer_reports",  "");
        if (!pc.isBlank() && printerCustomerCombo.getItems().contains(pc))
            printerCustomerCombo.setValue(pc);
        if (!pk.isBlank() && printerKitchenCombo.getItems().contains(pk))
            printerKitchenCombo.setValue(pk);
        if (!pj.isBlank() && printerJuiceCombo.getItems().contains(pj))
            printerJuiceCombo.setValue(pj);
        if (!pr.isBlank() && printerReportsCombo.getItems().contains(pr))
            printerReportsCombo.setValue(pr);
    }

    @FXML private void handleSave() {
        settingsDAO.set("restaurant_name",    restaurantNameField.getText());
        settingsDAO.set("restaurant_name_ar", restaurantNameArField.getText());
        settingsDAO.set("currency_symbol",    currencyField.getText());
        settingsDAO.set("receipt_footer",     footerField.getText());
        settingsDAO.set("receipt_footer_ar",  footerArField.getText());

        String none = "-- None --";
        String pc = printerCustomerCombo.getValue();
        String pk = printerKitchenCombo.getValue();
        String pj = printerJuiceCombo.getValue();
        String pr = printerReportsCombo.getValue();
        settingsDAO.set("printer_customer", none.equals(pc) ? "" : pc);
        settingsDAO.set("printer_kitchen",  none.equals(pk) ? "" : pk);
        settingsDAO.set("printer_juice",    none.equals(pj) ? "" : pj);
        settingsDAO.set("printer_reports",  none.equals(pr) ? "" : pr);

        boolean arabic = languageCombo.getValue().startsWith("Arabic");
        String lang    = arabic ? "ar" : "en";
        settingsDAO.set("ui_language", lang);
        I18n.setLanguage(lang);

        AlertHelper.showInfo(I18n.t("common.success"),
            "Settings saved / \u062a\u0645 \u062d\u0641\u0638 \u0627\u0644\u0625\u0639\u062f\u0627\u062f\u0627\u062a");
        NavigationHelper.navigateTo("MainLayout", I18n.t("app.title"));
    }

    @FXML private void handleRefresh() { loadPrinters(); loadSettings(); }

    @FXML private void handleTestPrint() {
        System.out.println("=== TEST PRINT ===");
        System.out.println("Customer Printer: " + printerCustomerCombo.getValue());
        System.out.println("Kitchen  Printer: " + printerKitchenCombo.getValue());
        System.out.println("Juice    Printer: " + printerJuiceCombo.getValue());
        System.out.println("==================");
        AlertHelper.showInfo("Test Print",
            "Check console for output.\nCustomer: " + printerCustomerCombo.getValue() +
            "\nKitchen: "  + printerKitchenCombo.getValue() +
            "\nJuice: "    + printerJuiceCombo.getValue());
    }
}
