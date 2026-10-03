package com.seafoodpos.controller;

import com.seafoodpos.model.User;
import com.seafoodpos.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Button orderBtn;
    @FXML private Button menuBtn;
    @FXML private Button usersBtn;
    @FXML private Button reportsBtn;
    @FXML private Button settingsBtn;
    @FXML private Button langToggleBtn;

    @FXML
    public void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            welcomeLabel.setText(I18n.t("dashboard.welcome") + " " + user.getFullName());
            roleLabel.setText(user.getRole().getBilingualName());
            boolean isAdmin = user.getRole().canManageUsers();
            boolean canMenu = user.getRole().canManageMenu();
            usersBtn.setVisible(isAdmin);
            usersBtn.setManaged(isAdmin);
            menuBtn.setVisible(canMenu);
            menuBtn.setManaged(canMenu);
            settingsBtn.setVisible(isAdmin);
            settingsBtn.setManaged(isAdmin);
        }
        if (langToggleBtn != null) langToggleBtn.setText(I18n.isArabic() ? "English" : "عربي");
    }

    @FXML private void onOpenOrder(ActionEvent e) { NavigationHelper.navigateTo("Order", I18n.t("app.title")); }
    @FXML private void onOpenMenu(ActionEvent e) { NavigationHelper.navigateTo("MenuManagement", I18n.t("app.title")); }
    @FXML private void onOpenUsers(ActionEvent e) { NavigationHelper.navigateTo("UserManagement", I18n.t("app.title")); }
    @FXML private void onOpenReports(ActionEvent e) { NavigationHelper.navigateTo("Reports", I18n.t("app.title")); }
    @FXML private void onOpenSettings(ActionEvent e) { NavigationHelper.navigateTo("Settings", I18n.t("app.title")); }

    @FXML
    private void onToggleLanguage(ActionEvent e) {
        String newLang = I18n.toggleLanguage();
        new com.seafoodpos.db.SettingsDAO().set("ui_language", newLang);
        NavigationHelper.navigateTo("Dashboard", I18n.t("app.title"));
    }

    @FXML
    private void onLogout(ActionEvent e) {
        SessionManager.getInstance().logout();
        NavigationHelper.navigateTo("Login", I18n.t("app.title"));
    }
}
