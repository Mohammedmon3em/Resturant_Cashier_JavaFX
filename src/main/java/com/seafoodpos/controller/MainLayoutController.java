package com.seafoodpos.controller;

import com.seafoodpos.model.Role;
import com.seafoodpos.model.User;
import com.seafoodpos.util.AlertHelper;
import com.seafoodpos.util.I18n;
import com.seafoodpos.util.NavigationHelper;
import com.seafoodpos.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainLayoutController implements Initializable {
    @FXML private BorderPane mainPane;
    @FXML private VBox       sidebar;
    @FXML private StackPane  contentArea;
    @FXML private Label      userNameLabel;
    @FXML private Label      userRoleLabel;
    @FXML private Label      restaurantNameLabel;
    @FXML private Button     btnOrders;
    @FXML private Button     btnMenu;
    @FXML private Button     btnInventory;
    @FXML private Button     btnExpenses;
    @FXML private Button     btnUsers;
    @FXML private Button     btnReports;
    @FXML private Button     btnSettings;
    @FXML private Button     btnLogout;
    @FXML private Button     btnLanguage;
    @FXML private Button     btnChangePassword;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        applyI18n();
        setupRoleAccess();
        loadContent("Order");
        setActive(btnOrders);
    }

    private void applyI18n() {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u != null) {
            userNameLabel.setText(u.getFullName());
            userRoleLabel.setText(u.getRole().getDisplayName() + " / " + u.getRole().getArabicName());
        }
        btnOrders.setText("\uD83D\uDCCB " + I18n.t("nav.orders"));
        btnMenu.setText("\uD83D\uDCDD "   + I18n.t("nav.menu"));
        btnInventory.setText("\uD83D\uDCE6 " + (I18n.isArabic() ? "\u0627\u0644\u0645\u062e\u0632\u0646" : "Inventory"));
        btnExpenses.setText("\uD83D\uDCB0 " + (I18n.isArabic() ? "\u0627\u0644\u0645\u0635\u0631\u0648\u0641\u0627\u062a" : "Expenses"));
        btnUsers.setText("\uD83D\uDC65 "  + I18n.t("nav.users"));
        btnReports.setText("\uD83D\uDCCA " + I18n.t("nav.reports"));
        btnSettings.setText("\u2699 "      + I18n.t("nav.settings"));
        btnLogout.setText("\u2190 "        + I18n.t("nav.logout"));
        btnLanguage.setText(I18n.t("common.language_toggle"));
        btnChangePassword.setText("\uD83D\uDD12 " + (I18n.isArabic() ? "\u062a\u063a\u064a\u064a\u0631 \u0643\u0644\u0645\u0629 \u0627\u0644\u0645\u0631\u0648\u0631" : "Change Password"));
    }

    private void setupRoleAccess() {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u == null) return;
        Role r = u.getRole();
        setVisible(btnMenu,     r.canManageMenu());
        setVisible(btnInventory, r == Role.ADMIN || r == Role.MODERATOR);
        setVisible(btnExpenses, r == Role.ADMIN || r == Role.MODERATOR);
        setVisible(btnUsers,    r.canManageUsers());
        setVisible(btnSettings, r.canAccessSettings());
    }

    private void setVisible(Button btn, boolean visible) {
        btn.setVisible(visible); btn.setManaged(visible);
    }

    @FXML private void handleOrders()   { loadContent("Order");         setActive(btnOrders); }
    @FXML private void handleMenu()     { loadContent("MenuManagement"); setActive(btnMenu); }
    @FXML private void handleInventory() { loadContent("Inventory"); setActive(btnInventory); }
    @FXML private void handleExpenses() { loadContent("Expenses"); setActive(btnExpenses); }
    @FXML private void handleUsers()    { loadContent("UserManagement"); setActive(btnUsers); }
    @FXML private void handleReports()  { loadContent("Reports");        setActive(btnReports); }
    @FXML private void handleSettings() { loadContent("Settings");       setActive(btnSettings); }

    @FXML
    private void handleLogout() {
        if (AlertHelper.showConfirm(I18n.t("nav.logout"),
                "Are you sure you want to logout?\n\u0647\u0644 \u0623\u0646\u062a \u0645\u062a\u0623\u0643\u062f \u0645\u0646 \u062a\u0633\u062c\u064a\u0644 \u0627\u0644\u062e\u0631\u0648\u062c\u061f")) {
            SessionManager.getInstance().logout();
            NavigationHelper.navigateTo("Login", I18n.t("app.title"));
        }
    }

    @FXML
    private void handleLanguage() {
        I18n.toggleLanguage();
        NavigationHelper.navigateTo("MainLayout", I18n.t("app.title"));
    }

    @FXML
    private void handleChangePassword() {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u == null) return;
        
        javafx.scene.control.Dialog<String> dlg = new javafx.scene.control.Dialog<>();
        dlg.setTitle(I18n.isArabic() ? "\u062a\u063a\u064a\u064a\u0631 \u0643\u0644\u0645\u0629 \u0627\u0644\u0645\u0631\u0648\u0631" : "Change Password");
        dlg.setHeaderText(I18n.isArabic() ? "\u0623\u062f\u062e\u0644 \u0643\u0644\u0645\u0629 \u0627\u0644\u0645\u0631\u0648\u0631 \u0627\u0644\u062c\u062f\u064a\u062f\u0629" : "Enter new password");
        
        javafx.scene.control.PasswordField passField = new javafx.scene.control.PasswordField();
        javafx.scene.control.TextField passVisible = new javafx.scene.control.TextField();
        passVisible.setManaged(false); passVisible.setVisible(false);
        passVisible.textProperty().bindBidirectional(passField.textProperty());
        javafx.scene.control.ToggleButton togglePass = new javafx.scene.control.ToggleButton("👁");
        togglePass.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #9ab0c0;");
        togglePass.selectedProperty().addListener((obs, o, n) -> {
            passVisible.setManaged(n); passVisible.setVisible(n);
            passField.setManaged(!n); passField.setVisible(!n);
            togglePass.setText(n ? "✖" : "👁");
        });
        StackPane stackPass = new StackPane(passVisible, passField, togglePass);
        stackPass.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        javafx.scene.control.PasswordField confirmField = new javafx.scene.control.PasswordField();
        javafx.scene.control.TextField confirmVisible = new javafx.scene.control.TextField();
        confirmVisible.setManaged(false); confirmVisible.setVisible(false);
        confirmVisible.textProperty().bindBidirectional(confirmField.textProperty());
        javafx.scene.control.ToggleButton toggleConfirm = new javafx.scene.control.ToggleButton("👁");
        toggleConfirm.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #9ab0c0;");
        toggleConfirm.selectedProperty().addListener((obs, o, n) -> {
            confirmVisible.setManaged(n); confirmVisible.setVisible(n);
            confirmField.setManaged(!n); confirmField.setVisible(!n);
            toggleConfirm.setText(n ? "✖" : "👁");
        });
        StackPane stackConfirm = new StackPane(confirmVisible, confirmField, toggleConfirm);
        stackConfirm.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        passField.setPromptText(I18n.isArabic() ? "\u0643\u0644\u0645\u0629 \u0627\u0644\u0645\u0631\u0648\u0631 \u0627\u0644\u062c\u062f\u064a\u062f\u0629" : "New Password");
        passVisible.setPromptText(passField.getPromptText());
        confirmField.setPromptText(I18n.isArabic() ? "\u062a\u0623\u0643\u064a\u062f \u0643\u0644\u0645\u0629 \u0627\u0644\u0645\u0631\u0648\u0631" : "Confirm Password");
        confirmVisible.setPromptText(confirmField.getPromptText());
        
        VBox content = new VBox(10, 
            new Label(I18n.isArabic() ? "\u0643\u0644\u0645\u0629 \u0627\u0644\u0645\u0631\u0648\u0631 \u0627\u0644\u062c\u062f\u064a\u062f\u0629:" : "New Password:"), stackPass, 
            new Label(I18n.isArabic() ? "\u062a\u0623\u0643\u064a\u062f:" : "Confirm:"), stackConfirm);
        content.setPadding(new javafx.geometry.Insets(20));
        dlg.getDialogPane().setContent(content);
        
        javafx.scene.control.ButtonType save = new javafx.scene.control.ButtonType("Save", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(save, javafx.scene.control.ButtonType.CANCEL);
        
        dlg.setResultConverter(b -> {
            if (b == save) {
                if (passField.getText().isBlank()) return null;
                if (!passField.getText().equals(confirmField.getText())) {
                    AlertHelper.showError("Error", "Passwords do not match");
                    return null;
                }
                return passField.getText();
            }
            return null;
        });
        
        dlg.showAndWait().ifPresent(newPass -> {
            String hash = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(12, newPass.toCharArray());
            if (new com.seafoodpos.db.UserDAO().updatePassword(u.getId(), hash)) {
                AlertHelper.showInfo("Success", "Password updated successfully");
            } else {
                AlertHelper.showError("Error", "Could not update password");
            }
        });
    }

    public void loadContent(String fxmlName) {
        try {
            URL fxmlUrl = getClass().getResource("/com/seafoodpos/fxml/" + fxmlName + ".fxml");
            if (fxmlUrl == null) { AlertHelper.showError("Error", "FXML not found: " + fxmlName); return; }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node content = loader.load();
            if (mainPane != null) mainPane.setCenter(content);
            else if (contentArea != null) contentArea.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError("Navigation Error", "Failed to load: " + fxmlName + "\n" + e.getMessage());
        }
    }

    private void setActive(Button active) {
        for (Node n : sidebar.getChildren())
            if (n instanceof Button b) b.getStyleClass().remove("nav-btn-active");
        active.getStyleClass().add("nav-btn-active");
    }
}
