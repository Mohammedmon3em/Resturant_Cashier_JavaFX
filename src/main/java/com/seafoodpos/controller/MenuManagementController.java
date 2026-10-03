package com.seafoodpos.controller;

import com.seafoodpos.db.MenuItemDAO;
import com.seafoodpos.model.ItemType;
import com.seafoodpos.model.MenuItem;
import com.seafoodpos.util.AlertHelper;
import com.seafoodpos.util.I18n;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuManagementController implements Initializable {
    @FXML private Label                        titleLabel;
    @FXML private TableView<MenuItem>          menuTable;
    @FXML private TableColumn<MenuItem,Integer> colId;
    @FXML private TableColumn<MenuItem,String>  colName;
    @FXML private TableColumn<MenuItem,String>  colType;
    @FXML private TableColumn<MenuItem,String>  colCategory;
    @FXML private TableColumn<MenuItem,Double>  colPriceKg;
    @FXML private TableColumn<MenuItem,Double>  colPriceSmall;
    @FXML private TableColumn<MenuItem,Double>  colPriceLarge;
    @FXML private TableColumn<MenuItem,Boolean> colAvailable;
    @FXML private Button btnAdd, btnEdit, btnDelete, btnToggleAvailable;

    private final MenuItemDAO              dao   = new MenuItemDAO();
    private final ObservableList<MenuItem> items = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        applyI18n(); setupTable(); loadData();
    }

    private void applyI18n() {
        titleLabel.setText(I18n.t("menu.title"));
        btnAdd.setText(I18n.t("menu.add"));
        btnEdit.setText(I18n.t("menu.edit"));
        btnDelete.setText(I18n.t("menu.delete"));
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getItemType() == ItemType.FISH_MEAL ? I18n.t("menu.fish_meal") : I18n.t("menu.juice")));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPriceKg.setCellValueFactory(new PropertyValueFactory<>("pricePerKg"));
        colPriceSmall.setCellValueFactory(new PropertyValueFactory<>("priceSmall"));
        colPriceLarge.setCellValueFactory(new PropertyValueFactory<>("priceLarge"));
        colAvailable.setCellValueFactory(new PropertyValueFactory<>("available"));

        for (var col : new TableColumn[]{colPriceKg, colPriceSmall, colPriceLarge}) {
            @SuppressWarnings("unchecked")
            TableColumn<MenuItem,Double> c = (TableColumn<MenuItem,Double>) col;
            c.setCellFactory(cc -> new TableCell<>() {
                @Override protected void updateItem(Double v, boolean e) {
                    super.updateItem(v, e); setText(e || v == null ? null : String.format("%.2f", v));
                }
            });
        }
        colAvailable.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Boolean v, boolean e) {
                super.updateItem(v, e);
                if (e || v == null) { setText(null); setStyle(""); }
                else { setText(v ? "\u2714" : "\u2718");
                       setStyle(v ? "-fx-text-fill:#06d6a0;-fx-font-size:16px;"
                                  : "-fx-text-fill:#ef5350;-fx-font-size:16px;"); }
            }
        });
        menuTable.setItems(items);
        menuTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            boolean has = sel != null;
            btnEdit.setDisable(!has); btnDelete.setDisable(!has); btnToggleAvailable.setDisable(!has);
            if (sel != null)
                btnToggleAvailable.setText(sel.isAvailable() ? I18n.t("menu.disable") : I18n.t("menu.enable"));
        });
        btnEdit.setDisable(true); btnDelete.setDisable(true); btnToggleAvailable.setDisable(true);
    }

    private void loadData() { items.setAll(dao.findAll()); }

    @FXML private void handleAdd()  { showDialog(null); }
    @FXML private void handleEdit() {
        MenuItem s = menuTable.getSelectionModel().getSelectedItem();
        if (s != null) showDialog(s);
    }
    @FXML private void handleDelete() {
        MenuItem s = menuTable.getSelectionModel().getSelectedItem();
        if (s != null && AlertHelper.showConfirm(I18n.t("menu.delete"),
                I18n.t("menu.confirm_delete") + "\n" + s.getName())) {
            dao.delete(s.getId()); loadData();
        }
    }
    @FXML private void handleToggleAvailable() {
        MenuItem s = menuTable.getSelectionModel().getSelectedItem();
        if (s != null) { dao.setAvailable(s.getId(), !s.isAvailable()); loadData(); }
    }

    private void showDialog(MenuItem existing) {
        Dialog<MenuItem> dlg = new Dialog<>();
        dlg.setTitle(existing == null ? I18n.t("menu.add") : I18n.t("menu.edit"));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(I18n.t("menu.fish_meal"), I18n.t("menu.juice"));
        typeCombo.setValue(I18n.t("menu.fish_meal"));

        TextField nameField    = tf(I18n.t("menu.name"));
        TextField catField     = tf(I18n.t("menu.category"));
        TextField priceKgField = tf(I18n.t("menu.price_kg"));
        TextField priceSmField = tf(I18n.t("menu.price_small"));
        TextField priceLgField = tf(I18n.t("menu.price_large"));
        CheckBox  availCb      = new CheckBox(I18n.t("menu.available")); availCb.setSelected(true);

        HBox fishRow = row(I18n.t("menu.price_kg"),    priceKgField);
        HBox smRow   = row(I18n.t("menu.price_small"), priceSmField);
        HBox lgRow   = row(I18n.t("menu.price_large"), priceLgField);

        Runnable updateVis = () -> {
            boolean fish = typeCombo.getValue().equals(I18n.t("menu.fish_meal"));
            fishRow.setVisible(fish);  fishRow.setManaged(fish);
            smRow.setVisible(!fish);   smRow.setManaged(!fish);
            lgRow.setVisible(!fish);   lgRow.setManaged(!fish);
        };
        typeCombo.valueProperty().addListener((o, old, nv) -> {
            updateVis.run();
            if (dlg.getDialogPane().getScene() != null && dlg.getDialogPane().getScene().getWindow() != null) {
                dlg.getDialogPane().getScene().getWindow().sizeToScene();
            }
        });
        updateVis.run();

        if (existing != null) {
            nameField.setText(existing.getName());
            catField.setText(existing.getCategory() != null ? existing.getCategory() : "");
            availCb.setSelected(existing.isAvailable());
            boolean isFish = existing.getItemType() == ItemType.FISH_MEAL;
            typeCombo.setValue(isFish ? I18n.t("menu.fish_meal") : I18n.t("menu.juice"));
            if (isFish && existing.getPricePerKg() != null)  priceKgField.setText(String.valueOf(existing.getPricePerKg()));
            if (!isFish && existing.getPriceSmall() != null) priceSmField.setText(String.valueOf(existing.getPriceSmall()));
            if (!isFish && existing.getPriceLarge() != null) priceLgField.setText(String.valueOf(existing.getPriceLarge()));
        }

        VBox content = new VBox(10,
            lbl(I18n.t("menu.type")), typeCombo,
            lbl(I18n.t("menu.name")), nameField,
            lbl(I18n.t("menu.category")), catField,
            fishRow, smRow, lgRow, availCb);
        content.setPadding(new Insets(20));
        dlg.getDialogPane().setContent(content);

        ButtonType saveBtn = new ButtonType(existing == null ? I18n.t("menu.add") : I18n.t("menu.save"), ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
        
        dlg.getDialogPane().addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == javafx.scene.input.KeyCode.ENTER) {
                ev.consume();
                javafx.scene.Node btn = dlg.getDialogPane().lookupButton(saveBtn);
                if (btn instanceof Button && !btn.isDisabled()) {
                    ((Button) btn).fire();
                }
            }
        });
        
        dlg.setResultConverter(b -> {
            if (b != saveBtn) return null;
            MenuItem item = existing != null ? existing : new MenuItem();
            item.setName(nameField.getText().trim());
            item.setCategory(catField.getText().trim().isEmpty() ? "General" : catField.getText().trim());
            item.setAvailable(availCb.isSelected());
            boolean fish = typeCombo.getValue().equals(I18n.t("menu.fish_meal"));
            item.setItemType(fish ? ItemType.FISH_MEAL : ItemType.JUICE);
            try {
                if (fish) { item.setPricePerKg(Double.parseDouble(priceKgField.getText())); }
                else {
                    item.setPriceSmall(Double.parseDouble(priceSmField.getText()));
                    item.setPriceLarge(Double.parseDouble(priceLgField.getText()));
                }
            } catch (NumberFormatException e) {
                AlertHelper.showError(I18n.t("common.error"), "Invalid price values"); return null;
            }
            return item;
        });
        dlg.showAndWait().ifPresent(item -> {
            if (item.getName().isEmpty()) return;
            if (existing == null) dao.insert(item); else dao.update(item);
            loadData();
        });
    }

    private TextField tf(String prompt) { TextField f = new TextField(); f.setPromptText(prompt); return f; }
    private Label lbl(String text) { Label l = new Label(text); l.setStyle("-fx-text-fill:#9ab0c0;"); return l; }
    private HBox row(String label, TextField f) {
        Label l = new Label(label); l.setMinWidth(120); l.setStyle("-fx-text-fill:#9ab0c0;");
        return new HBox(10, l, f);
    }
}
