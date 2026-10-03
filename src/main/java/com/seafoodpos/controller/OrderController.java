package com.seafoodpos.controller;

import com.seafoodpos.db.MenuItemDAO;
import com.seafoodpos.model.ItemType;
import com.seafoodpos.model.JuiceSize;
import com.seafoodpos.model.Order;
import com.seafoodpos.model.OrderItem;
import com.seafoodpos.model.MenuItem;          // our model class – explicit import wins
import com.seafoodpos.service.OrderService;
import com.seafoodpos.service.PrintService;
import com.seafoodpos.util.AlertHelper;
import com.seafoodpos.util.I18n;
// Explicit JavaFX control imports (no wildcard) so javafx.scene.control.MenuItem is never pulled in
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class OrderController implements Initializable {
    @FXML private Label                         titleLabel;
    @FXML private ScrollPane                    menuScrollPane;
    @FXML private VBox                          menuContainer;
    @FXML private TableView<OrderItem>          cartTable;
    @FXML private TableColumn<OrderItem,String> colItem;
    @FXML private TableColumn<OrderItem,String> colDetail;
    @FXML private TableColumn<OrderItem,Double> colPrice;
    @FXML private TableColumn<OrderItem,Double> colTotal;
    @FXML private Label                         grandTotalLabel;
    @FXML private Button                        confirmButton;
    @FXML private Button                        clearButton;
    @FXML private Button                        removeItemButton;

    private final MenuItemDAO   menuItemDAO  = new MenuItemDAO();
    private final OrderService  orderService = new OrderService();
    private final PrintService  printService = new PrintService();
    private final ObservableList<OrderItem> cartItems = FXCollections.observableArrayList();

    private static final Map<String,String> CAT_COLOR = Map.of(
        "Grilled",  "#7a3010",
        "Fried",    "#8a4a08",
        "Steamed",  "#0e4a6e",
        "Tropical", "#145e38",
        "Fresh",    "#0a5858",
        "General",  "#3a2a5e"
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        applyI18n();
        setupTable();
        loadMenuButtons();
    }

    private void applyI18n() {
        titleLabel.setText("\uD83C\uDFEA " + I18n.t("order.title"));
        colItem.setText(I18n.t("order.item"));
        colDetail.setText(I18n.t("order.detail"));
        colPrice.setText(I18n.t("order.price"));
        colTotal.setText(I18n.t("order.total"));
        confirmButton.setText("\u2714 " + I18n.t("order.confirm"));
        clearButton.setText("\uD83D\uDDD1 " + I18n.t("order.clear"));
        grandTotalLabel.setText(String.format("0.00 %s", I18n.t("common.currency")));
    }

    private void setupTable() {
        colItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colDetail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDetailDisplay()));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));

        colPrice.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean e) {
                super.updateItem(v, e);
                setText(e || v == null ? null : String.format("%.2f", v));
            }
        });
        colTotal.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean e) {
                super.updateItem(v, e);
                if (!e && v != null) {
                    setText(String.format("%.2f", v));
                    setStyle("-fx-text-fill:#06d6a0;-fx-font-weight:bold;");
                } else { setText(null); setStyle(""); }
            }
        });
        cartTable.setItems(cartItems);
        cartTable.setPlaceholder(new Label(I18n.t("order.empty")));
    }

    private void loadMenuButtons() {
        menuContainer.getChildren().clear();
        List<MenuItem> all    = menuItemDAO.findAllAvailable();
        List<MenuItem> fish   = all.stream().filter(i -> i.getItemType() == ItemType.FISH_MEAL).toList();
        List<MenuItem> juices = all.stream().filter(i -> i.getItemType() == ItemType.JUICE).toList();

        if (!fish.isEmpty()) {
            menuContainer.getChildren().add(sectionHeader("\uD83D\uDC1F " + I18n.t("order.fish.section")));
            menuContainer.getChildren().add(buildGrid(fish, true));
        }
        if (!juices.isEmpty()) {
            menuContainer.getChildren().add(sectionHeader("\uD83E\uDD64 " + I18n.t("order.juice.section")));
            menuContainer.getChildren().add(buildGrid(juices, false));
        }
    }

    private Label sectionHeader(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#00b4d8;-fx-padding:12 5 4 5;");
        return lbl;
    }

    private FlowPane buildGrid(List<MenuItem> items, boolean isFish) {
        FlowPane fp = new FlowPane();
        fp.setHgap(10); fp.setVgap(10);
        fp.setPadding(new Insets(6));
        fp.setPrefWrapLength(700);
        fp.setStyle("-fx-background-color:transparent;");
        for (MenuItem m : items) fp.getChildren().add(makeButton(m, isFish));
        return fp;
    }

    private Button makeButton(MenuItem m, boolean isFish) {
        String color = CAT_COLOR.getOrDefault(m.getCategory(), "#445566");
        StringBuilder txt = new StringBuilder(m.getName());
        if (isFish && m.getPricePerKg() != null)
            txt.append("\n").append(String.format("%.0f %s/kg", m.getPricePerKg(), I18n.t("common.currency")));
        else if (!isFish) {
            if (m.getPriceSmall() != null) txt.append("\nS:").append(String.format("%.0f", m.getPriceSmall()));
            if (m.getPriceLarge() != null) txt.append("  L:").append(String.format("%.0f", m.getPriceLarge()));
        }
        Button btn = new Button(txt.toString());
        btn.setPrefSize(148, 90); btn.setMaxSize(168, 110);
        btn.setWrapText(true); btn.setTextAlignment(TextAlignment.CENTER);
        btn.setAlignment(Pos.CENTER);
        String baseStyle = String.format(
            "-fx-background-color:%s;-fx-text-fill:white;-fx-font-size:12px;" +
            "-fx-font-weight:bold;-fx-background-radius:12;-fx-cursor:hand;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.45),7,0,0,2);", color);
        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(baseStyle +
            "-fx-opacity:0.88;-fx-scale-x:1.04;-fx-scale-y:1.04;"));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        btn.setOnAction(e -> {
            if (isFish) showWeightDialog(m);
            else        showJuiceSizeDialog(m);
        });
        return btn;
    }

    private record FishResult(double weight, String method) {}

    private void showWeightDialog(MenuItem m) {
        Dialog<FishResult> dlg = new Dialog<>();
        dlg.setTitle(m.getName());
        dlg.setHeaderText(I18n.t("order.enter_weight") + "\n"
            + I18n.t("order.price_per_kg") + " "
            + String.format("%.2f %s", m.getPricePerKg(), I18n.t("common.currency")));
        
        ToggleGroup tg = new ToggleGroup();
        ToggleButton fried = makeToggle("مقلي\nFried", tg);
        ToggleButton grilled = makeToggle("مشوي\nGrilled", tg);
        HBox methodBox = new HBox(10, fried, grilled);
        methodBox.setAlignment(Pos.CENTER);
        fried.setSelected(true); // Default

        TextField wf = new TextField(); wf.setPromptText("0.000");
        wf.setStyle("-fx-font-size:16px;-fx-padding:10;-fx-background-color:#0d2035;" +
            "-fx-text-fill:white;-fx-border-color:#1a3a5c;" +
            "-fx-border-radius:6;-fx-background-radius:6;");
        Label preview = new Label(String.format("0.00 %s", I18n.t("common.currency")));
        preview.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#06d6a0;");
        wf.textProperty().addListener((o, old, nv) -> {
            try { preview.setText(String.format("%.2f %s",
                m.calculateFishPrice(Double.parseDouble(nv)), I18n.t("common.currency")));
            } catch (NumberFormatException ex) { preview.setText(String.format("0.00 %s", I18n.t("common.currency"))); }
        });
        Label wLabel = new Label(I18n.t("order.weight_prompt"));
        wLabel.setStyle("-fx-text-fill:#9ab0c0;");
        Label pLabel = new Label(I18n.t("order.calculated_price"));
        pLabel.setStyle("-fx-text-fill:#9ab0c0;");
        
        VBox content = new VBox(10, methodBox, wLabel, wf, pLabel, preview);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color:#122233;");
        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().setStyle("-fx-background-color:#122233;");
        ButtonType addBtn = new ButtonType(I18n.t("common.add"), ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);
        dlg.setResultConverter(b -> {
            if (b == addBtn) {
                try { 
                    double w = Double.parseDouble(wf.getText()); 
                    String method = fried.isSelected() ? "مقلي" : "مشوي";
                    return new FishResult(w, method);
                }
                catch (NumberFormatException e) { return null; }
            }
            return null;
        });
        dlg.showAndWait().ifPresent(res -> {
            if (res != null && res.weight() > 0)
                addToCart(OrderItem.forFishMeal(m.getId(), m.getName(), res.weight(), m.getPricePerKg(), res.method()));
            else AlertHelper.showWarning(I18n.t("common.error"), "Enter a valid weight > 0");
        });
    }

    private record JuiceResult(JuiceSize size, int qty) {}

    private void showJuiceSizeDialog(MenuItem m) {
        Dialog<JuiceResult> dlg = new Dialog<>();
        dlg.setTitle(m.getName());
        dlg.setHeaderText(I18n.t("order.select_size"));
        ToggleGroup tg = new ToggleGroup();
        ToggleButton small = makeToggle(
            I18n.t("order.size_small") + "\n" +
            String.format("%.2f %s", m.getPriceSmall() != null ? m.getPriceSmall() : 0, I18n.t("common.currency")), tg);
        ToggleButton large = makeToggle(
            I18n.t("order.size_large") + "\n" +
            String.format("%.2f %s", m.getPriceLarge() != null ? m.getPriceLarge() : 0, I18n.t("common.currency")), tg);
        HBox box = new HBox(20, small, large);
        box.setAlignment(Pos.CENTER);
        
        TextField qtyField = new TextField("1");
        qtyField.setStyle("-fx-font-size:16px;-fx-padding:10;-fx-background-color:#0d2035;" +
            "-fx-text-fill:white;-fx-border-color:#1a3a5c;-fx-border-radius:6;-fx-background-radius:6;");
        Label qtyLabel = new Label("Quantity / الكمية");
        qtyLabel.setStyle("-fx-text-fill:#9ab0c0;");
        VBox qtyBox = new VBox(5, qtyLabel, qtyField);
        qtyBox.setAlignment(Pos.CENTER);

        VBox content = new VBox(20, box, qtyBox);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color:#122233;");
        dlg.getDialogPane().setContent(content);
        dlg.getDialogPane().setStyle("-fx-background-color:#122233;");
        ButtonType addBtn = new ButtonType(I18n.t("common.add"), ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);
        dlg.setResultConverter(b -> {
            if (b == addBtn) {
                int qty = 1;
                try { qty = Integer.parseInt(qtyField.getText().trim()); } catch(NumberFormatException ignored) {}
                if (large.isSelected()) return new JuiceResult(JuiceSize.LARGE, qty);
                if (small.isSelected()) return new JuiceResult(JuiceSize.SMALL, qty);
            }
            return null;
        });
        dlg.showAndWait().ifPresent(res -> {
            if (res != null)
                addToCart(OrderItem.forJuice(m.getId(), m.getName(), res.size(), res.qty(),
                    m.calculateJuicePrice(res.size())));
        });
    }

    private ToggleButton makeToggle(String text, ToggleGroup tg) {
        ToggleButton tb = new ToggleButton(text);
        tb.setToggleGroup(tg);
        tb.setPrefSize(145, 90);
        tb.setWrapText(true);
        tb.setTextAlignment(TextAlignment.CENTER);
        String base = "-fx-font-size:14px;-fx-font-weight:bold;-fx-background-radius:10;" +
            "-fx-background-color:#0d2035;-fx-text-fill:white;-fx-border-color:#1a3a5c;" +
            "-fx-border-radius:10;-fx-cursor:hand;";
        tb.setStyle(base);
        tb.selectedProperty().addListener((o, old, sel) ->
            tb.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-background-radius:10;" +
                (sel ? "-fx-background-color:#00b4d8;-fx-text-fill:white;-fx-border-color:#00b4d8;"
                     : "-fx-background-color:#0d2035;-fx-text-fill:white;-fx-border-color:#1a3a5c;") +
                "-fx-border-radius:10;-fx-cursor:hand;"));
        return tb;
    }

    private void addToCart(OrderItem item) { cartItems.add(item); updateTotal(); }

    private void updateTotal() {
        double total = cartItems.stream().mapToDouble(OrderItem::getLineTotal).sum();
        grandTotalLabel.setText(String.format("%.2f %s", total, I18n.t("common.currency")));
    }

    @FXML private void handleRemoveItem() {
        OrderItem sel = cartTable.getSelectionModel().getSelectedItem();
        if (sel != null) { cartItems.remove(sel); updateTotal(); }
    }

    @FXML private void handleClear() {
        if (cartItems.isEmpty()) return;
        if (AlertHelper.showConfirm(I18n.t("order.clear"),
                "Clear all items?\n\u0645\u0633\u062d \u062c\u0645\u064a\u0639 \u0627\u0644\u0639\u0646\u0627\u0635\u0631\u061f")) {
            cartItems.clear(); updateTotal();
        }
    }

    @FXML private void handleConfirmOrder() {
        if (cartItems.isEmpty()) {
            AlertHelper.showWarning(I18n.t("common.error"), I18n.t("order.empty")); return;
        }
        try {
            Order order = orderService.createOrder(new ArrayList<>(cartItems));
            printService.printOrder(order);
            AlertHelper.showInfo(I18n.t("common.success"),
                "Order #" + order.getOrderNumber() + " completed!\n"
                + "\u0637\u0644\u0628 \u0631\u0642\u0645 " + order.getOrderNumber()
                + " \u062a\u0645 \u0628\u0646\u062c\u0627\u062d!");
            cartItems.clear(); updateTotal();
        } catch (Exception e) {
            AlertHelper.showError(I18n.t("common.error"), "Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
