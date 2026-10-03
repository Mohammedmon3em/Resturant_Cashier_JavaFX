package com.seafoodpos.controller;

import com.seafoodpos.db.InventoryDAO;
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
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class InventoryController implements Initializable {
    @FXML private TableView<Map<String, Object>> table;
    @FXML private TableColumn<Map<String, Object>, String> colItem;
    @FXML private TableColumn<Map<String, Object>, String> colW1;
    @FXML private TableColumn<Map<String, Object>, String> colW2;

    private InventoryDAO dao = new InventoryDAO();
    private ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colItem.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("itemName"))));
        colW1.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f kg", (Double) d.getValue().get("w1Qty"))));
        colW2.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f kg", (Double) d.getValue().get("w2Qty"))));
        table.setItems(items);
    }

    private void loadData() {
        items.setAll(dao.getInventory());
    }

    @FXML private void handlePurchase() {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Add Purchase");
        ComboBox<String> cmbName = new ComboBox<>();
        cmbName.setPromptText("Select Item...");
        com.seafoodpos.db.MenuItemDAO menuDao = new com.seafoodpos.db.MenuItemDAO();
        List<String> menuItems = menuDao.findAll().stream().map(com.seafoodpos.model.MenuItem::getName).collect(Collectors.toList());
        cmbName.getItems().addAll(menuItems);
        
        TextField txtQty = new TextField(); txtQty.setPromptText("Quantity (kg)");
        TextField txtCost = new TextField(); txtCost.setPromptText("Total Cost");
        
        VBox box = new VBox(10, new Label("Item Name:"), cmbName, new Label("Quantity (kg):"), txtQty, new Label("Total Cost:"), txtCost);
        box.setPadding(new Insets(20));
        dlg.getDialogPane().setContent(box);
        
        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        
        dlg.setResultConverter(b -> {
            if (b == save) {
                if (cmbName.getValue() == null) {
                    AlertHelper.showError("Error", "Please select an item");
                    return null;
                }
                try {
                    dao.addPurchase(cmbName.getValue().trim(), Double.parseDouble(txtQty.getText()), Double.parseDouble(txtCost.getText()));
                    loadData();
                } catch (Exception e) {
                    AlertHelper.showError("Error", "Invalid input");
                }
            }
            return null;
        });
        dlg.showAndWait();
    }

    @FXML private void handleCleaning() {
        Map<String, Object> sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertHelper.showError("Error", "Please select an item first");
            return;
        }
        
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Cleaning Entry");
        TextField txtW1 = new TextField(); txtW1.setPromptText("Weight taken from Warehouse 1 (kg)");
        TextField txtW2 = new TextField(); txtW2.setPromptText("Weight stored in Warehouse 2 (kg)");
        
        VBox box = new VBox(10, new Label("Item: " + sel.get("itemName")), new Label("Warehouse 1 (Before clean):"), txtW1, new Label("Warehouse 2 (After clean):"), txtW2);
        box.setPadding(new Insets(20));
        dlg.getDialogPane().setContent(box);
        
        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        
        dlg.setResultConverter(b -> {
            if (b == save) {
                try {
                    dao.cleanItem((Integer) sel.get("id"), Double.parseDouble(txtW1.getText()), Double.parseDouble(txtW2.getText()));
                    loadData();
                } catch (Exception e) {
                    AlertHelper.showError("Error", "Invalid input");
                }
            }
            return null;
        });
        dlg.showAndWait();
    }
}
