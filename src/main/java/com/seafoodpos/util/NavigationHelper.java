package com.seafoodpos.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class NavigationHelper {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) { primaryStage = stage; }
    public static Stage getPrimaryStage()            { return primaryStage; }

    public static void navigateTo(String fxmlName, String title) {
        try {
            URL url = NavigationHelper.class.getResource(
                "/com/seafoodpos/fxml/" + fxmlName + ".fxml");
            if (url == null) {
                AlertHelper.showError("Navigation Error", "FXML not found: " + fxmlName);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root);
            } else {
                scene = new Scene(root, scene.getWidth(), scene.getHeight());
            }
            URL cssUrl = NavigationHelper.class.getResource("/com/seafoodpos/css/base.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            I18n.applyOrientation(scene);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError("Navigation Error",
                "Could not load screen: " + fxmlName + "\n" + e.getMessage());
        }
    }

    public static <T> T navigateToWithController(String fxmlName, String title) {
        try {
            URL url = NavigationHelper.class.getResource(
                "/com/seafoodpos/fxml/" + fxmlName + ".fxml");
            if (url == null) return null;
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            URL cssUrl = NavigationHelper.class.getResource("/com/seafoodpos/css/base.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
            I18n.applyOrientation(scene);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError("Navigation Error",
                "Could not load screen: " + fxmlName + "\n" + e.getMessage());
            return null;
        }
    }
}
