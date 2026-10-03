package com.seafoodpos.util;

import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    private static ResourceBundle bundle;
    private static String currentLanguage = "en";

    public static void initialize(String lang) { setLanguage(lang); }

    public static void setLanguage(String lang) {
        currentLanguage = lang;
        Locale locale = "ar".equals(lang) ? Locale.of("ar") : Locale.ENGLISH;
        bundle = ResourceBundle.getBundle("com/seafoodpos/i18n/messages", locale);
    }

    public static String getCurrentLanguage() { return currentLanguage; }

    public static String t(String key) {
        try { return bundle.getString(key); }
        catch (Exception e) { return key; }
    }

    public static boolean isArabic() { return "ar".equals(currentLanguage); }

    public static NodeOrientation getOrientation() {
        return isArabic() ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT;
    }

    public static void applyOrientation(Scene scene) {
        if (scene != null && scene.getRoot() != null)
            scene.getRoot().setNodeOrientation(getOrientation());
    }

    public static void applyOrientation(Node node) {
        if (node != null) node.setNodeOrientation(getOrientation());
    }

    public static String toggleLanguage() {
        String newLang = isArabic() ? "en" : "ar";
        setLanguage(newLang);
        return newLang;
    }

    public static ResourceBundle getBundle() { return bundle; }
}
