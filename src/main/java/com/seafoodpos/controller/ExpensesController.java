package com.seafoodpos.controller;

import com.seafoodpos.db.ExpenseDAO;
import com.seafoodpos.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class ExpensesController implements Initializable {
    @FXML private TableView<Map<String, Object>> table;
    @FXML private TableColumn<Map<String, Object>, String> colName;
    @FXML private TableColumn<Map<String, Object>, String> colAmount;
    @FXML private TableColumn<Map<String, Object>, String> colDate;

    private ExpenseDAO dao = new ExpenseDAO();
    private ObservableList<Map<String, Object>> expenses = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colName.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("name"))));
        colAmount.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", (Double) d.getValue().get("amount"))));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("date"))));
        table.setItems(expenses);
    }

    private void loadData() {
        expenses.setAll(dao.getExpenses());
    }

    @FXML private void handleAddExpense() {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Add Expense");
        TextField txtName = new TextField(); txtName.setPromptText("Expense Name");
        TextField txtAmount = new TextField(); txtAmount.setPromptText("Amount");
        
        VBox box = new VBox(10, new Label("Expense Name:"), txtName, new Label("Amount:"), txtAmount);
        box.setPadding(new Insets(20));
        dlg.getDialogPane().setContent(box);
        
        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        
        dlg.setResultConverter(b -> {
            if (b == save) {
                try {
                    dao.addExpense(txtName.getText().trim(), Double.parseDouble(txtAmount.getText()));
                    loadData();
                } catch (Exception e) {
                    AlertHelper.showError("Error", "Invalid input");
                }
            }
            return null;
        });
        dlg.showAndWait();
    }

    @FXML private void handleAddDepreciation() {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Add Depreciation / إضافة إهلاك");
        
        ComboBox<String> comboItems = new ComboBox<>();
        comboItems.getItems().add("Manual Entry (No Item)");
        com.seafoodpos.db.InventoryDAO invDao = new com.seafoodpos.db.InventoryDAO();
        for (Map<String, Object> item : invDao.getInventory()) {
            comboItems.getItems().add((String) item.get("itemName"));
        }
        comboItems.getSelectionModel().selectFirst();
        
        TextField txtWeight = new TextField(); txtWeight.setPromptText("Weight (kg)");
        TextField txtAmount = new TextField(); txtAmount.setPromptText("Amount");
        
        // Auto-calculate amount when weight or item changes
        Runnable calc = () -> {
            String sel = comboItems.getValue();
            if (sel != null && !sel.startsWith("Manual")) {
                try {
                    double w = Double.parseDouble(txtWeight.getText());
                    double costPerKg = invDao.getLatestCostPerKg(sel);
                    txtAmount.setText(String.format("%.2f", w * costPerKg));
                } catch (Exception ignored) {}
            }
        };
        comboItems.setOnAction(e -> calc.run());
        txtWeight.textProperty().addListener((obs, old, nw) -> calc.run());
        
        VBox box = new VBox(10, 
            new Label("Item / الصنف:"), comboItems,
            new Label("Weight (kg) / الوزن:"), txtWeight,
            new Label("Depreciation Amount / قيمة الإهلاك:"), txtAmount);
        box.setPadding(new Insets(20));
        dlg.getDialogPane().setContent(box);
        
        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        
        dlg.setResultConverter(b -> {
            if (b == save) {
                try {
                    dao.addDepreciation(Double.parseDouble(txtAmount.getText()));
                    AlertHelper.showInfo("Success", "Depreciation added successfully!");
                } catch (Exception e) {
                    AlertHelper.showError("Error", "Invalid amount");
                }
            }
            return null;
        });
        dlg.showAndWait();
    }
}
