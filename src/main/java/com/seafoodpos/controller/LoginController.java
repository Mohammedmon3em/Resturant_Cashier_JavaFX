package com.seafoodpos.controller;

import com.seafoodpos.service.AuthService;
import com.seafoodpos.util.I18n;
import com.seafoodpos.util.NavigationHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML private Label         titleLabel;
    @FXML private Label         subtitleLabel;
    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     passwordVisibleField;
    @FXML private ToggleButton  togglePasswordButton;
    @FXML private Button        loginButton;
    @FXML private Label         errorLabel;
    @FXML private Button        langToggleButton;

    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        applyI18n();
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        passwordField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleLogin(); });
        passwordVisibleField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleLogin(); });
        usernameField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) passwordField.requestFocus(); });
        
        togglePasswordButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordVisibleField.setManaged(true);
                passwordVisibleField.setVisible(true);
                passwordField.setManaged(false);
                passwordField.setVisible(false);
                togglePasswordButton.setText("✖");
            } else {
                passwordVisibleField.setManaged(false);
                passwordVisibleField.setVisible(false);
                passwordField.setManaged(true);
                passwordField.setVisible(true);
                togglePasswordButton.setText("👁");
            }
        });
    }

    private void applyI18n() {
        titleLabel.setText(I18n.t("login.title"));
        subtitleLabel.setText(I18n.t("login.subtitle"));
        usernameField.setPromptText(I18n.t("login.username"));
        passwordField.setPromptText(I18n.t("login.password"));
        loginButton.setText(I18n.t("login.button"));
        langToggleButton.setText(I18n.t("common.language_toggle"));
        errorLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        errorLabel.setText("");
        try {
            authService.login(
                usernameField.getText(), passwordField.getText());
            NavigationHelper.navigateTo("MainLayout", I18n.t("app.title"));
        } catch (AuthService.AuthException e) {
            errorLabel.setText(e.getMessage());
            passwordField.clear();
            passwordField.requestFocus();
        }
    }

    @FXML
    private void handleLanguageToggle() {
        I18n.toggleLanguage();
        NavigationHelper.navigateTo("Login", I18n.t("app.title"));
    }
}
