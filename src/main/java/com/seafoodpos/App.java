package com.seafoodpos;

import com.seafoodpos.db.DatabaseManager;
import com.seafoodpos.db.SettingsDAO;
import com.seafoodpos.util.I18n;
import com.seafoodpos.util.NavigationHelper;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseManager.getInstance().initialize();
        String lang = new SettingsDAO().get("ui_language", "en");
        I18n.initialize(lang);
        NavigationHelper.setPrimaryStage(primaryStage);
        NavigationHelper.navigateTo("Login", I18n.t("app.title"));
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(750);
        primaryStage.setMaximized(true);
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/seafoodpos/images/logo.png")));
        } catch (Exception e) {
            System.err.println("Could not load app icon");
        }
        primaryStage.show();
    }

    @Override
    public void stop() {
        DatabaseManager.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
