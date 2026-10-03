package com.seafoodpos.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.seafoodpos.db.UserDAO;
import com.seafoodpos.model.Role;
import com.seafoodpos.model.User;
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
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {
    @FXML private Label                     titleLabel;
    @FXML private TableView<User>           userTable;
    @FXML private TableColumn<User,Integer> colId;
    @FXML private TableColumn<User,String>  colFullName;
    @FXML private TableColumn<User,String>  colUsername;
    @FXML private TableColumn<User,String>  colRole;
    @FXML private TableColumn<User,Boolean> colActive;
    @FXML private TableColumn<User,String>  colCreated;
    @FXML private Button                    btnAdd, btnToggleActive, btnDelete, btnChangePassword;

    private final UserDAO              dao   = new UserDAO();
    private final ObservableList<User> users = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        applyI18n(); setupTable(); loadData();
    }

    private void applyI18n() {
        titleLabel.setText(I18n.t("user.title"));
        btnAdd.setText(I18n.t("user.add"));
        btnToggleActive.setText(I18n.t("user.activate"));
        btnDelete.setText(I18n.isArabic() ? "حذف" : "Delete");
        btnChangePassword.setText(I18n.isArabic() ? "كلمة المرور" : "Password");
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole().getBilingualName()));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        colCreated.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        colActive.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Boolean v, boolean e) {
                super.updateItem(v, e);
                if (e || v == null) { setText(null); setStyle(""); }
                else { setText(v ? "\u2714 Active" : "\u2718 Inactive");
                       setStyle(v ? "-fx-text-fill:#06d6a0;" : "-fx-text-fill:#ef5350;"); }
            }
        });
        userTable.setItems(users);
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            boolean isAdmin = sel != null && sel.getRole() == Role.ADMIN;
            btnToggleActive.setDisable(sel == null || isAdmin);
            
            // Delete button is only visible/enabled if logged in user is admin and selected user is not admin
            boolean isCurrentUserAdmin = com.seafoodpos.util.SessionManager.getInstance().getCurrentUser().getRole() == Role.ADMIN;
            btnDelete.setDisable(sel == null || isAdmin || !isCurrentUserAdmin);
            btnDelete.setVisible(isCurrentUserAdmin);
            
            if (sel != null && !isAdmin)
                btnToggleActive.setText(sel.isActive() ? I18n.t("user.deactivate") : I18n.t("user.activate"));
                
            btnChangePassword.setDisable(sel == null || (!isCurrentUserAdmin && sel.getId() != com.seafoodpos.util.SessionManager.getInstance().getCurrentUser().getId()));
        });
        btnToggleActive.setDisable(true);
        btnChangePassword.setDisable(true);
        
        boolean isCurrentUserAdmin = com.seafoodpos.util.SessionManager.getInstance().getCurrentUser().getRole() == Role.ADMIN;
        btnDelete.setDisable(true);
        btnDelete.setVisible(isCurrentUserAdmin);
    }

    private void loadData() { users.setAll(dao.findAll()); }

    @FXML private void handleAdd() {
        Dialog<User> dlg = new Dialog<>();
        dlg.setTitle(I18n.t("user.add"));

        TextField     fullNameField = new TextField(); fullNameField.setPromptText(I18n.t("user.fullname"));
        TextField     usernameField = new TextField(); usernameField.setPromptText(I18n.t("user.username"));
        PasswordField passField     = new PasswordField(); passField.setPromptText(I18n.t("user.password"));
        TextField passVisible = new TextField(); passVisible.setPromptText(passField.getPromptText());
        passVisible.setManaged(false); passVisible.setVisible(false);
        passVisible.textProperty().bindBidirectional(passField.textProperty());
        ToggleButton togglePass = new ToggleButton("👁");
        togglePass.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #9ab0c0;");
        togglePass.selectedProperty().addListener((obs, o, n) -> {
            passVisible.setManaged(n); passVisible.setVisible(n);
            passField.setManaged(!n); passField.setVisible(!n);
            togglePass.setText(n ? "✖" : "👁");
        });
        javafx.scene.layout.StackPane stackPass = new javafx.scene.layout.StackPane(passVisible, passField, togglePass);
        stackPass.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        ComboBox<String> roleCombo  = new ComboBox<>();
        roleCombo.getItems().addAll(Role.MODERATOR.getDisplayName(), Role.CASHIER.getDisplayName());
        roleCombo.setValue(Role.CASHIER.getDisplayName());

        VBox content = new VBox(10,
            lbl(I18n.t("user.fullname")), fullNameField,
            lbl(I18n.t("user.username")), usernameField,
            lbl(I18n.t("user.password")), stackPass,
            lbl(I18n.t("user.role")), roleCombo);
        content.setPadding(new Insets(20));
        dlg.getDialogPane().setContent(content);

        ButtonType save = new ButtonType(I18n.t("user.save"), ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        dlg.setResultConverter(b -> {
            if (b != save) return null;
            if (fullNameField.getText().isBlank() || usernameField.getText().isBlank() || passField.getText().isBlank()) {
                AlertHelper.showError(I18n.t("common.error"), "All fields required"); return null;
            }
            String hash = BCrypt.withDefaults().hashToString(12, passField.getText().toCharArray());
            Role role = roleCombo.getValue().equals(Role.MODERATOR.getDisplayName()) ? Role.MODERATOR : Role.CASHIER;
            return new User(0, usernameField.getText().trim(), hash, fullNameField.getText().trim(), role, true, null);
        });
        dlg.showAndWait().ifPresent(u -> {
            if (!dao.insert(u)) AlertHelper.showError(I18n.t("common.error"), "Username already exists");
            else loadData();
        });
    }

    @FXML private void handleToggleActive() {
        User s = userTable.getSelectionModel().getSelectedItem();
        if (s != null && s.getRole() != Role.ADMIN) { dao.setActive(s.getId(), !s.isActive()); loadData(); }
    }

    @FXML private void handleDelete() {
        User s = userTable.getSelectionModel().getSelectedItem();
        if (s != null && s.getRole() != Role.ADMIN) {
            if (AlertHelper.showConfirm("Delete User", "Are you sure you want to delete user " + s.getUsername() + "?")) {
                if (dao.delete(s.getId())) loadData();
                else AlertHelper.showError("Error", "Could not delete user.");
            }
        }
    }

    @FXML private void handleChangePassword() {
        User s = userTable.getSelectionModel().getSelectedItem();
        if (s != null) {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Change Password");
            dialog.setHeaderText("Change Password for " + s.getUsername());

            PasswordField passField = new PasswordField();
            TextField passVisible = new TextField();
            passVisible.setManaged(false);
            passVisible.setVisible(false);
            passVisible.textProperty().bindBidirectional(passField.textProperty());

            ToggleButton toggleBtn = new ToggleButton("👁");
            toggleBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #9ab0c0;");
            toggleBtn.selectedProperty().addListener((obs, old, isSelected) -> {
                passVisible.setManaged(isSelected);
                passVisible.setVisible(isSelected);
                passField.setManaged(!isSelected);
                passField.setVisible(!isSelected);
                toggleBtn.setText(isSelected ? "✖" : "👁");
            });

            javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane(passVisible, passField, toggleBtn);
            stack.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

            VBox content = new VBox(10, new Label("Enter new password:"), stack);
            content.setPadding(new Insets(20));
            dialog.getDialogPane().setContent(content);

            ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

            dialog.setResultConverter(b -> {
                if (b == saveBtn) return passField.getText();
                return null;
            });

            dialog.showAndWait().ifPresent(newPassword -> {
                if (!newPassword.trim().isEmpty()) {
                    String hash = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
                    if (dao.updatePassword(s.getId(), hash)) {
                        AlertHelper.showInfo("Success", "Password updated successfully.");
                    } else {
                        AlertHelper.showError("Error", "Failed to update password.");
                    }
                } else {
                    AlertHelper.showError("Error", "Password cannot be empty.");
                }
            });
        }
    }

    private Label lbl(String text) { Label l = new Label(text); l.setStyle("-fx-text-fill:#9ab0c0;"); return l; }
}
